package com.example.blindassistcompanion.domain.scene

import com.example.blindassistcompanion.domain.repository.GenerativeAiRepository
import com.example.blindassistcompanion.domain.repository.PiCameraClient
import com.example.blindassistcompanion.domain.repository.TtsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

import com.example.blindassistcompanion.domain.repository.AudioRecorder
import com.example.blindassistcompanion.data.sarvam.SarvamApiService
import com.example.blindassistcompanion.data.tts.SarvamTtsRepositoryImpl
import com.example.blindassistcompanion.data.tts.AndroidTtsRepositoryImpl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import dagger.hilt.android.qualifiers.ApplicationContext

class AiAssistantUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val piCameraClient: PiCameraClient,
    private val generativeAiRepository: GenerativeAiRepository,
    private val ttsRepository: TtsRepository,
    private val localTts: AndroidTtsRepositoryImpl,
    private val audioRecorder: AudioRecorder,
    private val sarvamApi: SarvamApiService
) {
    operator fun invoke(): Flow<SceneDescriptionState> = flow {
        try {
            // 0. Pause background AI on Pi
            piCameraClient.pauseBackgroundAi()

            emit(SceneDescriptionState.Listening)
            
            // 1. Record audio from Bluetooth Headset
            val audioFile = File(context.cacheDir, "user_prompt.m4a")
            
            // Step 1: Start the microphone. This immediately commands the Bluetooth headset to switch into Voice Call mode.
            // During this switch, audio is completely MUTED by the headset hardware for about 1 to 1.5 seconds.
            audioRecorder.startRecording(audioFile)
            
            // Step 2: Wait 1.5 seconds for the Bluetooth Voice Call channel to fully connect and stabilize.
            delay(1500)
            
            // Step 3: Now that the channel is open and unmuted, speak "Listening" directly into the earbuds.
            localTts.speak("Listening")
            
            // Step 4: Give the user 3.5 seconds to speak their question.
            delay(3500) 
            audioRecorder.stopRecording()

            // 2. Fetch latest image from Pi
            emit(SceneDescriptionState.FetchingImage)
            val imageResult = piCameraClient.fetchLatestSnapshot()
            val imageBytes = if (imageResult.isFailure) {
                ttsRepository.speak("Camera disconnected, answering from audio only.")
                ByteArray(0)
            } else {
                imageResult.getOrThrow()
            }

            // 3. Send Audio to Sarvam STT for Transcription (Solves 503 errors and perfectly handles Indian Languages)
            emit(SceneDescriptionState.AnalyzingScene)
            
            val requestFile = audioFile.asRequestBody("audio/mp4".toMediaType())
            val body = MultipartBody.Part.createFormData("file", audioFile.name, requestFile)
            val model = "saaras:v3".toRequestBody("text/plain".toMediaType())
            val languageCodeBody = "unknown".toRequestBody("text/plain".toMediaType())
            
            val sttResponse = try {
                sarvamApi.transcribeSpeech(body, model, languageCode = languageCodeBody)
            } catch (e: Exception) {
                ttsRepository.speak("Failed to connect to Sarvam translation service.")
                emit(SceneDescriptionState.Error("Sarvam STT failed: ${e.localizedMessage}"))
                return@flow
            }
            
            val transcriptText = sttResponse.transcript
            val detectedLanguageCode = sttResponse.languageCode ?: "en-IN"

            // Map BCP-47 code to explicit language name so Gemini Flash Lite doesn't get confused
            // We strictly limit this to English, Hindi, and Marathi.
            // If Sarvam hallucinates another language (like Gujarati 'gu-IN'), we force fallback to English.
            val languageName = when (detectedLanguageCode) {
                "hi-IN", "hi" -> "Hindi"
                "mr-IN", "mr" -> "Marathi"
                else -> "English"
            }

            // 4. Analyze with Gemini using ONLY text and image
            val promptForGemini = """
                You are an assistive AI for a visually impaired person. Describe the attached image to answer the user's question.
                
                The user asked: "$transcriptText"
                The user's language is: $languageName
                
                CRITICAL RULES:
                1. You MUST formulate your entire response in $languageName. Do not use any other language.
                2. Keep it EXTREMELY short (maximum 2 sentences).
                3. Prioritize obstacles, people, vehicles, doors, stairs, crossings, and entrances.
                4. Do not use conversational phrases or filler words.
                5. Do not use bullet points or lists.
                6. Do not explain what the image contains in detail, just answer the question directly.
            """.trimIndent()
            
            val descriptionResult = generativeAiRepository.askAssistant(imageBytes, promptForGemini)
            
            if (descriptionResult.isFailure) {
                val errorMsg = descriptionResult.exceptionOrNull()?.localizedMessage ?: ""
                val friendlyMessage = if (errorMsg.contains("quota", ignoreCase = true) || errorMsg.contains("429")) {
                    "The daily limit for the AI Assistant has been reached. Please try again later."
                } else {
                    "AI Assistant encountered an error."
                }
                ttsRepository.speak(friendlyMessage)
                emit(SceneDescriptionState.Error(friendlyMessage))
                return@flow
            }

            val description = descriptionResult.getOrThrow()

            // Set the detected language for the upcoming TTS answer
            (ttsRepository as? SarvamTtsRepositoryImpl)?.currentLanguageCode = detectedLanguageCode

            emit(SceneDescriptionState.Speaking)
            ttsRepository.speak(description)
            
            emit(SceneDescriptionState.Success(description))
        } finally {
            piCameraClient.resumeBackgroundAi()
        }
    }
}
