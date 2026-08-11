package com.example.blindassistcompanion.domain.repository

import java.io.File

interface AudioRecorder {
    fun startRecording(outputFile: File)
    fun stopRecording()
}
