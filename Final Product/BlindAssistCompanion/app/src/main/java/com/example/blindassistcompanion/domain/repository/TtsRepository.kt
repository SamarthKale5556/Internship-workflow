package com.example.blindassistcompanion.domain.repository

interface TtsRepository {
    fun speak(text: String)
    fun stop()
    fun shutdown()
}
