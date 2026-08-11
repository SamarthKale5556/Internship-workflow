package com.example.blindassistcompanion.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blindassistcompanion.domain.scene.AiAssistantUseCase
import com.example.blindassistcompanion.domain.repository.PiCameraClient
import com.example.blindassistcompanion.domain.model.ConnectionState
import com.example.blindassistcompanion.domain.model.DeviceCommand
import com.example.blindassistcompanion.domain.model.PiTelemetry
import com.example.blindassistcompanion.domain.usecase.ObserveConnectionStateUseCase
import com.example.blindassistcompanion.domain.usecase.ObservePiStatusUseCase
import com.example.blindassistcompanion.domain.usecase.SendCommandUseCase
import com.example.blindassistcompanion.domain.scene.DescribeSurroundingsUseCase
import com.example.blindassistcompanion.domain.scene.SceneDescriptionState
import com.example.blindassistcompanion.domain.usecase.RunDemoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val connectionState: ConnectionState = ConnectionState.Disconnected,
    val telemetry: PiTelemetry? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    observeConnectionStateUseCase: ObserveConnectionStateUseCase,
    observePiStatusUseCase: ObservePiStatusUseCase,
    private val sendCommandUseCase: SendCommandUseCase,
    private val describeSurroundingsUseCase: DescribeSurroundingsUseCase,
    private val runDemoUseCase: RunDemoUseCase,
    private val aiAssistantUseCase: AiAssistantUseCase,
    private val piCameraClient: PiCameraClient
) : ViewModel() {

    private val _sceneState = MutableStateFlow<SceneDescriptionState>(SceneDescriptionState.Idle)
    val sceneState = _sceneState.asStateFlow()

    private val _demoActive = MutableStateFlow(false)

    private val _sosTriggerEvent = kotlinx.coroutines.flow.MutableSharedFlow<Unit>(
        replay = 1,
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
    )
    val sosTriggerEvent = _sosTriggerEvent.asSharedFlow()

    private var lastProcessedEvents: List<String> = emptyList()

    init {
        viewModelScope.launch {
            observePiStatusUseCase().collect { telemetry ->
                val currentEvents = telemetry?.events ?: emptyList()
                if (currentEvents != lastProcessedEvents) {
                    lastProcessedEvents = currentEvents
                    currentEvents.forEach { event ->
                        when (event) {
                            "SINGLE_CLICK" -> describeSurroundings()
                            "DOUBLE_CLICK" -> askAiAssistant()
                            "LONG_PRESS" -> triggerSos()
                            "OBSTACLE_CLOSE" -> handleObstacleClose()
                        }
                    }
                }
            }
        }
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        observeConnectionStateUseCase(),
        observePiStatusUseCase(),
        _demoActive
    ) { connection, telemetry, isDemo ->
        if (isDemo) {
            // Spoof connection for demo
            DashboardUiState(
                connectionState = ConnectionState.Connected,
                telemetry = PiTelemetry(
                    batteryPercent = 100,
                    batteryVoltage = 12.0f,
                    cameraStatus = com.example.blindassistcompanion.domain.model.ComponentStatus.ACTIVE,
                    tofStatus = com.example.blindassistcompanion.domain.model.ComponentStatus.ACTIVE,
                    earbudStatus = com.example.blindassistcompanion.domain.model.ComponentStatus.ACTIVE,
                    aiEngineStatus = com.example.blindassistcompanion.domain.model.ComponentStatus.ACTIVE,
                    faceRecognitionStatus = com.example.blindassistcompanion.domain.model.ComponentStatus.ACTIVE,
                    cpuTemperature = 45f,
                    estimatedRuntimeMinutes = 180,
                    uptimeSeconds = 3600L,
                    inferenceFps = 15f,
                    firmwareVersion = "v1.0-DEMO",
                    lastAlert = "System initialized"
                ),
                isLoading = false
            )
        } else {
            DashboardUiState(
                connectionState = connection,
                telemetry = telemetry,
                isLoading = connection is ConnectionState.Connecting || connection is ConnectionState.Scanning
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun sendCommand(command: DeviceCommand) {
        viewModelScope.launch {
            sendCommandUseCase(command)
        }
    }

    fun describeSurroundings() {
        if (_sceneState.value !is SceneDescriptionState.Idle && _sceneState.value !is SceneDescriptionState.Success && _sceneState.value !is SceneDescriptionState.Error) {
            return // Already processing
        }
        viewModelScope.launch {
            describeSurroundingsUseCase().collect { state ->
                _sceneState.value = state
            }
        }
    }

    private fun handleObstacleClose() {
        if (_sceneState.value !is SceneDescriptionState.Idle && _sceneState.value !is SceneDescriptionState.Success && _sceneState.value !is SceneDescriptionState.Error) {
            return // Already processing
        }
        viewModelScope.launch {
            describeSurroundingsUseCase("WARNING: The ToF sensor has detected an obstacle very close to the user (less than 1 meter away). What is this object? Keep your answer to 1 short sentence.").collect { state ->
                _sceneState.value = state
            }
        }
    }

    fun runDemo() {
        if (_sceneState.value !is SceneDescriptionState.Idle && _sceneState.value !is SceneDescriptionState.Success && _sceneState.value !is SceneDescriptionState.Error) {
            return // Already processing
        }
        viewModelScope.launch {
            _demoActive.value = true
            runDemoUseCase().collect { state ->
                _sceneState.value = state
            }
            _demoActive.value = false
        }
    }

    fun askAiAssistant() {
        if (_sceneState.value !is SceneDescriptionState.Idle && _sceneState.value !is SceneDescriptionState.Success && _sceneState.value !is SceneDescriptionState.Error) {
            return // Already processing
        }
        viewModelScope.launch {
            aiAssistantUseCase().collect { state ->
                _sceneState.value = state
            }
        }
    }

    fun triggerAiAssistantRemote() {
        viewModelScope.launch {
            piCameraClient.triggerAiRecording()
            // The Pi will record audio, queue a DOUBLE_CLICK event, and our polling loop will catch it automatically!
        }
    }

    private fun triggerSos() {
        viewModelScope.launch {
            _sosTriggerEvent.emit(Unit)
        }
    }

    fun resetSceneState() {
        _sceneState.value = SceneDescriptionState.Idle
    }
}

