package com.example.blindassistcompanion.data.repository

import com.example.blindassistcompanion.domain.model.PiTelemetry
import com.example.blindassistcompanion.domain.repository.DeviceRepository
import com.example.blindassistcompanion.domain.repository.HotspotRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

import com.example.blindassistcompanion.domain.repository.PiCameraClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DeviceRepositoryImpl @Inject constructor(
    private val piCameraClient: PiCameraClient
) : DeviceRepository {
    private val _telemetry = MutableStateFlow<PiTelemetry?>(null)
    override val telemetry: StateFlow<PiTelemetry?> = _telemetry.asStateFlow()

    init {
        // Start polling Pi status every 2 seconds
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                val result = piCameraClient.fetchTelemetry()
                if (result.isSuccess) {
                    _telemetry.value = result.getOrNull()
                } else {
                    _telemetry.value = null
                }
                delay(1000)
            }
        }
    }

    override suspend fun syncFaceEmbeddings(embeddingsJson: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun requestSceneDescription(): Result<String> {
        return Result.success("Stub description")
    }
}

class HotspotRepositoryImpl @Inject constructor() : HotspotRepository {
    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    override suspend fun connectToHotspot(ssid: String, password: String?): Result<Unit> {
        _isConnected.value = true
        return Result.success(Unit)
    }

    override suspend fun disconnectFromHotspot(): Result<Unit> {
        _isConnected.value = false
        return Result.success(Unit)
    }
}
