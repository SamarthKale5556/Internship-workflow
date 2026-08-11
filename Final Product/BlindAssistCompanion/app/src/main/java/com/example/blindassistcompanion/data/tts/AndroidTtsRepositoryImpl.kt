package com.example.blindassistcompanion.data.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import com.example.blindassistcompanion.domain.repository.TtsRepository
import java.util.Locale
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext

class AndroidTtsRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context
) : TtsRepository, TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            
            // Route TTS audio through the Voice Call stream for Bluetooth Headset compatibility
            val attrs = android.media.AudioAttributes.Builder()
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SPEECH)
                .setUsage(android.media.AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .build()
            tts?.setAudioAttributes(attrs)
            
            isInitialized = true
        }
    }

    override fun speak(text: String) {
        if (isInitialized) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    override fun stop() {
        if (isInitialized) {
            tts?.stop()
        }
    }

    override fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
