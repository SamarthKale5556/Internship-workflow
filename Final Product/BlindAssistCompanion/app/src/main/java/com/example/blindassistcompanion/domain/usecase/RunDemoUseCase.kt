package com.example.blindassistcompanion.domain.usecase

import com.example.blindassistcompanion.domain.repository.TtsRepository
import com.example.blindassistcompanion.domain.scene.SceneDescriptionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class RunDemoUseCase @Inject constructor(
    private val ttsRepository: TtsRepository
) {
    operator fun invoke(): Flow<SceneDescriptionState> = flow {
        // T = 0s
        emit(SceneDescriptionState.ConnectingToCamera)
        delay(1000)

        // T = 1s: Simulate Scene Fetch
        emit(SceneDescriptionState.FetchingImage)
        delay(1000)
        
        // T = 2s: Simulate Family Recognition
        ttsRepository.speak("Mother detected ahead")
        delay(2500) // Wait for TTS to finish

        // T = 4.5s: Simulate Scene Analyze
        emit(SceneDescriptionState.AnalyzingScene)
        delay(1500)

        // T = 6s: Simulate Scene Description
        emit(SceneDescriptionState.Speaking)
        val demoDescription = "A large gate is ahead. Two people are standing near the entrance."
        ttsRepository.speak(demoDescription)
        
        delay(4000)

        // Complete
        emit(SceneDescriptionState.Success(demoDescription))
    }
}
