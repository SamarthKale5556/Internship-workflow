package com.example.blindassistcompanion.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface HotspotRepository {
    val isConnected: StateFlow<Boolean>
    
    suspend fun connectToHotspot(ssid: String, password: String?): Result<Unit>
    suspend fun disconnectFromHotspot(): Result<Unit>
}
