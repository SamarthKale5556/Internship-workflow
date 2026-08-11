package com.example.blindassistcompanion.data.repository

import android.bluetooth.BluetoothDevice
import com.example.blindassistcompanion.data.ble.BleConnectionManager
import com.example.blindassistcompanion.data.ble.BleScannerImpl
import com.example.blindassistcompanion.domain.model.ConnectionState
import com.example.blindassistcompanion.domain.model.DeviceCommand
import com.example.blindassistcompanion.domain.repository.BleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

class BleRepositoryImpl @Inject constructor(
    private val bleScanner: BleScannerImpl,
    private val connectionManager: BleConnectionManager
) : BleRepository {

    override val connectionState: StateFlow<ConnectionState>
        get() = connectionManager.connectionState

    // Exposed for raw telemetry parsing if needed outside of connection manager
    private val _telemetryFlow = MutableSharedFlow<ByteArray>(extraBufferCapacity = 10)
    override val telemetryFlow: Flow<ByteArray> = _telemetryFlow.asSharedFlow()

    override fun startScan(): Flow<BluetoothDevice> {
        return bleScanner.startScan()
    }

    override fun connect(macAddress: String) {
        connectionManager.connect(macAddress)
    }

    override fun disconnect() {
        connectionManager.disconnect()
    }

    override suspend fun writeCommand(command: DeviceCommand): Result<Unit> {
        return connectionManager.writeCommand(command)
    }
}
