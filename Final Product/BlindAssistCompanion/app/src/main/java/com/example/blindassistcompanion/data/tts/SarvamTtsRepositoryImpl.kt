package com.example.blindassistcompanion.data.tts

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Base64
import com.example.blindassistcompanion.data.sarvam.SarvamApiService
import com.example.blindassistcompanion.data.sarvam.SarvamTtsRequest
import com.example.blindassistcompanion.domain.repository.TtsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class SarvamTtsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sarvamApi: SarvamApiService
) : TtsRepository {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var mediaPlayer: MediaPlayer? = null

    // By default, assuming Hindi. This can be dynamically updated from a Settings Repository later.
    var currentLanguageCode = "hi-IN" 

    override fun speak(text: String) {
        scope.launch {
            try {
                // 1. Call Sarvam API
                val request = SarvamTtsRequest(
                    inputs = listOf(text),
                    targetLanguageCode = currentLanguageCode
                )
                val response = sarvamApi.generateSpeech(request)

                // 2. Decode Base64 audio
                if (response.audios.isNotEmpty()) {
                    val base64Audio = response.audios.first()
                    val audioBytes = Base64.decode(base64Audio, Base64.DEFAULT)

                    // 3. Save to temp file (MediaPlayer requires a file descriptor or URI)
                    val tempAudioFile = File(context.cacheDir, "sarvam_tts_temp.wav")
                    FileOutputStream(tempAudioFile).use { it.write(audioBytes) }

                    // 4. Play the audio using MediaPlayer
                    playAudioFile(tempAudioFile)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun playAudioFile(file: File) {
        stop() // Stop any currently playing audio

        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    // Usage media is appropriate, but VOICE_COMMUNICATION can help route to BT headsets
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION) 
                    .build()
            )
            setDataSource(file.absolutePath)
            prepare()
            start()

            setOnCompletionListener {
                it.release()
                mediaPlayer = null
                // Clean up temp file
                if (file.exists()) file.delete()
            }
        }
    }

    override fun stop() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaPlayer = null
        }
    }

    override fun shutdown() {
        stop()
    }
}
