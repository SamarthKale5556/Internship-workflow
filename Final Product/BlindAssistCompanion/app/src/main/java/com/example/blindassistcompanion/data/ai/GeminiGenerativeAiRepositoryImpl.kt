package com.example.blindassistcompanion.data.ai

import android.graphics.BitmapFactory
import com.example.blindassistcompanion.domain.repository.GenerativeAiRepository
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

import com.google.ai.client.generativeai.type.generationConfig

class GeminiGenerativeAiRepositoryImpl @Inject constructor() : GenerativeAiRepository {

    // Placeholder API key to satisfy GitHub Push Protection. In production, load from BuildConfig/environment variables.
    private val apiKey = "YOUR_GEMINI_API_KEY_HERE"

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash-lite",
        apiKey = apiKey,
        generationConfig = generationConfig {
            maxOutputTokens = 60
            temperature = 0.4f
        }
    )

    override suspend fun generateSceneDescription(imageBytes: ByteArray): Result<String> = withContext(Dispatchers.IO) {
        try {
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            
            val prompt = """
                You are an assistive AI for a visually impaired person.
                Describe only the most important information directly in front of the user.

                Rules:
                * Maximum 2 short sentences.
                * Prioritize obstacles, people, vehicles, doors, stairs, crossings, and entrances.
                * Mention left, center, or right when relevant.
                * Mention distance only if obvious.
                * Ignore decorative details.
                * Ignore colors unless important.
                * Do not use conversational phrases.
                * Do not say "I can see".
                * Do not explain what the image contains in detail.
            """.trimIndent()

            val inputContent = content {
                image(bitmap)
                text(prompt)
            }

            val response = generativeModel.generateContent(inputContent)
            val description = response.text ?: "Could not analyze the scene."
            
            Result.success(description.trim())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun askAssistant(imageBytes: ByteArray, textPrompt: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            
            val prompt = """
                You are an assistive AI for a visually impaired person.
                The current scene in front of the user is provided in the image attachment.
                
                $textPrompt
            """.trimIndent()

            val inputContent = content {
                image(bitmap)
                text(prompt)
            }

            val response = generativeModel.generateContent(inputContent)
            val description = response.text ?: "I'm sorry, I couldn't process your request."
            
            Result.success(description.trim())
        } catch (e: Exception) {
            val errorMsg = e.localizedMessage ?: ""
            if (errorMsg.contains("503") || errorMsg.contains("UNAVAILABLE") || errorMsg.contains("MissingFieldException")) {
                Result.failure(Exception("The AI server is currently overloaded due to high demand. Please try again in a few moments."))
            } else if (errorMsg.contains("429") || errorMsg.contains("Quota")) {
                Result.failure(Exception("You have reached the API speed limit. Please wait 10 seconds before asking another question."))
            } else {
                Result.failure(e)
            }
        }
    }
    override suspend fun askAssistantWithAudio(imageBytes: ByteArray, audioBytes: ByteArray, textPrompt: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val bitmap = try {
                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            } catch (e: Exception) {
                null
            }
            
            val prompt = """
                You are an assistive AI for a visually impaired person.
                The user's spoken question is provided in the audio attachment.
                
                CRITICAL INSTRUCTION:
                Check if an image was provided with this prompt.
                If NO image is attached, you MUST NOT guess or make up a scene. You must ONLY reply (in the user's language) with: "The camera is disconnected, so I cannot see anything right now." and answer any general questions they might have asked.
                If an image IS attached, describe the image to answer their question.
                
                $textPrompt
            """.trimIndent()

            val inputContent = content {
                bitmap?.let { image(it) }
                blob("audio/mp4", audioBytes)
                text(prompt)
            }

            val response = generativeModel.generateContent(inputContent)
            val description = response.text ?: "I'm sorry, I couldn't process your request."
            
            Result.success(description.trim())
        } catch (e: Exception) {
            val errorMsg = e.localizedMessage ?: ""
            if (errorMsg.contains("503") || errorMsg.contains("UNAVAILABLE") || errorMsg.contains("MissingFieldException")) {
                Result.failure(Exception("The AI server is currently overloaded due to high demand. Please try again in a few moments."))
            } else if (errorMsg.contains("429") || errorMsg.contains("Quota")) {
                Result.failure(Exception("You have reached the API speed limit. Please wait 10 seconds before asking another question."))
            } else {
                Result.failure(e)
            }
        }
    }
}
