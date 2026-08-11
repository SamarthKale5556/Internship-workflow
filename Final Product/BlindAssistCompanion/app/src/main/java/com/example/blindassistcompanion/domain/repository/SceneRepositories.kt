package com.example.blindassistcompanion.domain.repository

interface GenerativeAiRepository {
    suspend fun generateSceneDescription(imageBytes: ByteArray): Result<String>
    suspend fun askAssistant(imageBytes: ByteArray, textPrompt: String): Result<String>
    suspend fun askAssistantWithAudio(imageBytes: ByteArray, audioBytes: ByteArray, textPrompt: String): Result<String>
}

interface PiCameraClient {
    suspend fun fetchLatestSnapshot(): Result<ByteArray>
    suspend fun fetchTelemetry(): Result<com.example.blindassistcompanion.domain.model.PiTelemetry>
    suspend fun downloadAudio(): Result<ByteArray>
    suspend fun triggerAiRecording(): Result<Unit>
    suspend fun resumeBackgroundAi(): Result<Unit>
    suspend fun pauseBackgroundAi(): Result<Unit>
}
