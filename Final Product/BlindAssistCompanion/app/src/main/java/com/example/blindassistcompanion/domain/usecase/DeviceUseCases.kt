package com.example.blindassistcompanion.domain.usecase

import android.bluetooth.BluetoothDevice
import com.example.blindassistcompanion.domain.model.ConnectionState
import com.example.blindassistcompanion.domain.model.DeviceCommand
import com.example.blindassistcompanion.domain.model.PiTelemetry
import com.example.blindassistcompanion.domain.repository.BleRepository
import com.example.blindassistcompanion.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ScanForDevicesUseCase @Inject constructor(
    private val bleRepository: BleRepository
) {
    operator fun invoke(): Flow<BluetoothDevice> = bleRepository.startScan()
}

class ConnectToPiUseCase @Inject constructor(
    private val bleRepository: BleRepository
) {
    operator fun invoke(macAddress: String) = bleRepository.connect(macAddress)
}

class DisconnectUseCase @Inject constructor(
    private val bleRepository: BleRepository
) {
    operator fun invoke() = bleRepository.disconnect()
}

class ObserveConnectionStateUseCase @Inject constructor(
    private val bleRepository: BleRepository
) {
    operator fun invoke(): StateFlow<ConnectionState> = bleRepository.connectionState
}

class ObservePiStatusUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository
) {
    operator fun invoke(): StateFlow<PiTelemetry?> = deviceRepository.telemetry
}

class SendCommandUseCase @Inject constructor(
    private val bleRepository: BleRepository
) {
    suspend operator fun invoke(command: DeviceCommand): Result<Unit> =
        bleRepository.writeCommand(command)
}
