package com.example.blindassistcompanion.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PiTelemetry(
    val batteryPercent: Int,
    val batteryVoltage: Float,
    val estimatedRuntimeMinutes: Int,
    val cpuTemperature: Float,
    val uptimeSeconds: Long,
    val cameraStatus: ComponentStatus,
    val tofStatus: ComponentStatus,
    val earbudStatus: ComponentStatus,
    val aiEngineStatus: ComponentStatus,
    val faceRecognitionStatus: ComponentStatus,
    val inferenceFps: Float,
    val firmwareVersion: String,
    val lastAlert: String?,
    val events: List<String> = emptyList(),
    val tofDistanceMm: Float = 0f
)

@Serializable
enum class ComponentStatus {
    ACTIVE,
    ERROR,
    OFF
}
