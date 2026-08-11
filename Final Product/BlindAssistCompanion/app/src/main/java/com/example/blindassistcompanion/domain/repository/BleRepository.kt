package com.example.blindassistcompanion.domain.repository

import android.bluetooth.BluetoothDevice
import com.example.blindassistcompanion.domain.model.ConnectionState
import com.example.blindassistcompanion.domain.model.DeviceCommand
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface BleRepository {
    val connectionState: StateFlow<ConnectionState>
    val telemetryFlow: Flow<ByteArray>

    fun startScan(): Flow<BluetoothDevice>
    fun connect(macAddress: String)
    fun disconnect()
    
    suspend fun writeCommand(command: DeviceCommand): Result<Unit>
}
