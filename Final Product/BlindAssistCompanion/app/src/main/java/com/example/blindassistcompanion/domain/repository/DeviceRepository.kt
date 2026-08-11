package com.example.blindassistcompanion.domain.repository

import com.example.blindassistcompanion.domain.model.PiTelemetry
import kotlinx.coroutines.flow.StateFlow

interface DeviceRepository {
    val telemetry: StateFlow<PiTelemetry?>
    
    suspend fun syncFaceEmbeddings(embeddingsJson: String): Result<Unit>
    suspend fun requestSceneDescription(): Result<String>
}
