package com.example.blindassistcompanion.domain.scene

import com.example.blindassistcompanion.domain.model.DeviceCommand
import com.example.blindassistcompanion.domain.repository.BleRepository
import com.example.blindassistcompanion.domain.repository.GenerativeAiRepository
import com.example.blindassistcompanion.domain.repository.HotspotRepository
import com.example.blindassistcompanion.domain.repository.PiCameraClient
import com.example.blindassistcompanion.domain.repository.TtsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

sealed class SceneDescriptionState {
    object Idle : SceneDescriptionState()
    object ConnectingToCamera : SceneDescriptionState()
    object FetchingImage : SceneDescriptionState()
    object AnalyzingScene : SceneDescriptionState()
    object Speaking : SceneDescriptionState()
    object Listening : SceneDescriptionState()
    data class Success(val description: String) : SceneDescriptionState()
    data class Error(val message: String) : SceneDescriptionState()
}

class DescribeSurroundingsUseCase @Inject constructor(
    private val bleRepository: BleRepository,
    private val hotspotRepository: HotspotRepository,
    private val piCameraClient: PiCameraClient,
    private val generativeAiRepository: GenerativeAiRepository,
    private val ttsRepository: TtsRepository
) {
    operator fun invoke(customPrompt: String? = null): Flow<SceneDescriptionState> = flow {
        try {
            // 1. Pause background AI on Pi
            piCameraClient.pauseBackgroundAi()

            // 2. Send START_WIFI command over BLE
            emit(SceneDescriptionState.ConnectingToCamera)
            bleRepository.writeCommand(DeviceCommand.StartWifi)

            // 3. Connect to Pi SoftAP
            hotspotRepository.connectToHotspot("BlindAssist_Pi", "blindassist123")

            // 4. Fetch Image
            emit(SceneDescriptionState.FetchingImage)
            val imageResult = piCameraClient.fetchLatestSnapshot()
            val imageBytes = imageResult.getOrThrow()

            // 4. Analyze with Gemini
            emit(SceneDescriptionState.AnalyzingScene)
            val descriptionResult = if (customPrompt != null) {
                generativeAiRepository.askAssistant(imageBytes, customPrompt)
            } else {
                generativeAiRepository.generateSceneDescription(imageBytes)
            }
            val description = descriptionResult.getOrThrow()

            // 5. Speak to User
            emit(SceneDescriptionState.Speaking)
            ttsRepository.speak(description)

            // 6. Resume Background AI on Pi
            piCameraClient.resumeBackgroundAi()

            // 7. Complete
            emit(SceneDescriptionState.Success(description))
            
        } catch (e: Exception) {
            piCameraClient.resumeBackgroundAi()
            val errorMsg = e.localizedMessage ?: ""
            val friendlyMessage = if (errorMsg.contains("quota", ignoreCase = true) || errorMsg.contains("429")) {
                "The daily limit for AI descriptions has been reached. Please try again later."
            } else {
                "Failed to describe surroundings."
            }
            ttsRepository.speak(friendlyMessage)
            emit(SceneDescriptionState.Error(friendlyMessage))
        }
    }
}
