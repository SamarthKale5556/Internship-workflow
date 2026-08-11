package com.example.blindassistcompanion.data.sarvam

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

// --- TTS Models ---

@Serializable
data class SarvamTtsRequest(
    val inputs: List<String>,
    @SerialName("target_language_code") val targetLanguageCode: String,
    val speaker: String = "meera",
    val pitch: Double = 0.0,
    val pace: Double = 1.0,
    val loudness: Double = 1.5,
    @SerialName("speech_sample_rate") val speechSampleRate: Int = 8000,
    @SerialName("enable_preprocessing") val enablePreprocessing: Boolean = true,
    val model: String = "sarvam-1"
)

@Serializable
data class SarvamTtsResponse(
    val audios: List<String> // Base64 encoded audio strings
)

// --- STT Models ---

@Serializable
data class SarvamSttResponse(
    val transcript: String,
    @SerialName("language_code") val languageCode: String? = null
)

// --- Retrofit Interface ---

interface SarvamApiService {

    @POST("text-to-speech")
    suspend fun generateSpeech(
        @Body request: SarvamTtsRequest
    ): SarvamTtsResponse

    @Multipart
    @POST("speech-to-text-translate")
    suspend fun translateSpeechToText(
        @Part file: MultipartBody.Part,
        @Part("model") model: RequestBody,
        @Part("prompt") prompt: RequestBody? = null,
        @Part("language-code") languageCode: RequestBody? = null
    ): SarvamSttResponse

    @Multipart
    @POST("speech-to-text")
    suspend fun transcribeSpeech(
        @Part file: MultipartBody.Part,
        @Part("model") model: RequestBody,
        @Part("prompt") prompt: RequestBody? = null,
        @Part("language-code") languageCode: RequestBody? = null,
        @Part("mode") mode: RequestBody? = null
    ): SarvamSttResponse
}
