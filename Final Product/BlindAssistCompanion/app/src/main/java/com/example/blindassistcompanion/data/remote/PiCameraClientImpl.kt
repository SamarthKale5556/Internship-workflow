package com.example.blindassistcompanion.data.remote

import com.example.blindassistcompanion.domain.repository.PiCameraClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

class PiCameraClientImpl @Inject constructor() : PiCameraClient {
    
    // Replace this IP with the actual IP address of the Raspberry Pi on your phone's hotspot
    // You can find this using an app like Fing, or by running `hostname -I` on the Pi.
    private val piCameraUrl = "http://10.72.59.7:5000/api/v1/camera/snapshot"

    private val piStatusUrl = "http://10.72.59.7:5000/api/v1/status"
    private val piAudioUrl = "http://10.72.59.7:5000/api/v1/camera/audio"
    
    private val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }

    override suspend fun fetchLatestSnapshot(): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val url = URL(piCameraUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                val buffer = ByteArrayOutputStream()
                var nRead: Int
                val data = ByteArray(16384)
                while (inputStream.read(data, 0, data.size).also { nRead = it } != -1) {
                    buffer.write(data, 0, nRead)
                }
                buffer.flush()
                Result.success(buffer.toByteArray())
            } else {
                Result.failure(Exception("Failed to fetch image: HTTP ${connection.responseCode}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchTelemetry(): Result<com.example.blindassistcompanion.domain.model.PiTelemetry> = withContext(Dispatchers.IO) {
        try {
            val url = URL(piStatusUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 3000
            connection.readTimeout = 3000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
                val telemetry = json.decodeFromString<com.example.blindassistcompanion.domain.model.PiTelemetry>(jsonString)
                Result.success(telemetry)
            } else {
                Result.failure(Exception("Failed to fetch telemetry: HTTP ${connection.responseCode}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun downloadAudio(): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val url = URL(piAudioUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 10000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                val buffer = ByteArrayOutputStream()
                var nRead: Int
                val data = ByteArray(16384)
                while (inputStream.read(data, 0, data.size).also { nRead = it } != -1) {
                    buffer.write(data, 0, nRead)
                }
                buffer.flush()
                Result.success(buffer.toByteArray())
            } else {
                Result.failure(Exception("Failed to download audio: HTTP ${connection.responseCode}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun triggerAiRecording(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val url = URL("http://10.72.59.7:5000/api/v1/trigger/ai")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.connectTimeout = 3000
            connection.readTimeout = 3000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to trigger AI recording"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resumeBackgroundAi(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val url = URL("http://10.72.59.7:5000/api/v1/resume")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.connectTimeout = 3000
            connection.readTimeout = 3000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to resume background AI"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun pauseBackgroundAi(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val url = URL("http://10.72.59.7:5000/api/v1/pause")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.connectTimeout = 3000
            connection.readTimeout = 3000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to pause background AI"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
