package com.example.blindassistcompanion.data.audio

import android.content.Context
import android.media.AudioManager
import android.media.MediaRecorder
import android.os.Build
import com.example.blindassistcompanion.domain.repository.AudioRecorder
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext

class BluetoothAudioRecorderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AudioRecorder {

    private var recorder: MediaRecorder? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    override fun startRecording(outputFile: File) {
        // 1. Force audio routing to Bluetooth Headset (SCO)
        if (audioManager.isBluetoothScoAvailableOffCall) {
            audioManager.startBluetoothSco()
            audioManager.isBluetoothScoOn = true
        }

        // 2. Initialize the recorder
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            // VOICE_COMMUNICATION is best for picking up Bluetooth Headset mics
            setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(FileOutputStream(outputFile).fd)

            try {
                prepare()
                start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun stopRecording() {
        try {
            recorder?.apply {
                stop()
                reset()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        recorder = null

        // Turn off Bluetooth SCO routing so normal audio playback can resume
        if (audioManager.isBluetoothScoOn) {
            audioManager.stopBluetoothSco()
            audioManager.isBluetoothScoOn = false
        }
    }
}
