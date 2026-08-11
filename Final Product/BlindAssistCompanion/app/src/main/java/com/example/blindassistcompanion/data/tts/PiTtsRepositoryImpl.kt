package com.example.blindassistcompanion.data.tts

import com.example.blindassistcompanion.domain.repository.TtsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

class PiTtsRepositoryImpl @Inject constructor() : TtsRepository {

    // Replace this IP with the actual IP address of the Raspberry Pi on your phone's hotspot
    // Make sure it matches the one in PiCameraClientImpl.kt
    private val piTtsUrl = "http://10.72.59.7:5000/api/v1/speaker/speak"
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun speak(text: String) {
        scope.launch {
            try {
                val url = URL(piTtsUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json; utf-8")
                connection.setRequestProperty("Accept", "application/json")
                connection.doOutput = true

                val jsonInputString = JSONObject().apply {
                    put("text", text)
                }.toString()

                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(jsonInputString)
                    writer.flush()
                }

                val responseCode = connection.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    println("Failed to send TTS to Pi: HTTP $responseCode")
                }
            } catch (e: Exception) {
                println("Error sending TTS to Pi: ${e.message}")
            }
        }
    }

    override fun stop() {
        // Optional: Implement a stop endpoint on the Pi if needed
    }

    override fun shutdown() {
        // Optional: Implement shutdown logic
    }
}
