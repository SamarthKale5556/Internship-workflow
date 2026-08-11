package com.example.blindassistcompanion.data.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.util.Log
import com.example.blindassistcompanion.domain.model.ComponentStatus
import com.example.blindassistcompanion.domain.model.ConnectionState
import com.example.blindassistcompanion.domain.model.DeviceCommand
import com.example.blindassistcompanion.domain.model.PiTelemetry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject

import dagger.hilt.android.qualifiers.ApplicationContext

@SuppressLint("MissingPermission")
class BleConnectionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter?
) {
    private var bluetoothGatt: BluetoothGatt? = null

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _telemetry = MutableStateFlow<PiTelemetry?>(null)
    val telemetry: StateFlow<PiTelemetry?> = _telemetry.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    fun connect(macAddress: String) {
        if (bluetoothAdapter == null) {
            _connectionState.value = ConnectionState.Error("Bluetooth not available")
            return
        }

        val device = bluetoothAdapter.getRemoteDevice(macAddress)
        _connectionState.value = ConnectionState.Connecting
        bluetoothGatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
    }

    fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        _connectionState.value = ConnectionState.Disconnected
    }

    suspend fun writeCommand(command: DeviceCommand): Result<Unit> {
        val gatt = bluetoothGatt ?: return Result.failure(Exception("GATT is null"))
        val service = gatt.getService(GattServiceDefinitions.BLIND_ASSIST_SERVICE_UUID)
            ?: return Result.failure(Exception("Service not found"))
        val characteristic = service.getCharacteristic(GattServiceDefinitions.COMMAND_CHARACTERISTIC_UUID)
            ?: return Result.failure(Exception("Command characteristic not found"))

        val payload = encodeCommand(command)
        
        // Use writeType for fast execution (Write Without Response)
        characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        
        // For API 33+, use gatt.writeCharacteristic(characteristic, payload, writeType)
        // For older APIs, use characteristic.value = payload; gatt.writeCharacteristic(characteristic)
        characteristic.value = payload
        val success = gatt.writeCharacteristic(characteristic)
        
        return if (success) Result.success(Unit) else Result.failure(Exception("Failed to write command"))
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        _connectionState.value = ConnectionState.Connected
                        gatt.discoverServices()
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        _connectionState.value = ConnectionState.Disconnected
                        gatt.close()
                    }
                }
            } else {
                // Handle GATT 133 error by entering reconnecting state
                Log.e("BleConnectionManager", "GATT Error: $status")
                _connectionState.value = ConnectionState.Reconnecting
                gatt.close()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                enableNotifications(gatt, GattServiceDefinitions.TELEMETRY_CHARACTERISTIC_UUID)
                enableNotifications(gatt, GattServiceDefinitions.EMERGENCY_CHARACTERISTIC_UUID)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            when (characteristic.uuid) {
                GattServiceDefinitions.TELEMETRY_CHARACTERISTIC_UUID -> {
                    parseTelemetry(characteristic.value)
                }
                GattServiceDefinitions.EMERGENCY_CHARACTERISTIC_UUID -> {
                    // Handle high priority emergency alerts
                }
            }
        }
    }

    private fun enableNotifications(gatt: BluetoothGatt, characteristicUuid: java.util.UUID) {
        val service = gatt.getService(GattServiceDefinitions.BLIND_ASSIST_SERVICE_UUID) ?: return
        val characteristic = service.getCharacteristic(characteristicUuid) ?: return
        
        gatt.setCharacteristicNotification(characteristic, true)
        val descriptor = characteristic.getDescriptor(GattServiceDefinitions.CCCD_UUID)
        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        gatt.writeDescriptor(descriptor)
    }

    private fun parseTelemetry(bytes: ByteArray) {
        if (bytes.size < 16) return // Minimum payload size
        
        try {
            val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
            val parsed = PiTelemetry(
                batteryPercent = buffer.get().toInt(),
                batteryVoltage = buffer.get().toFloat() / 10f, // Example encoding
                estimatedRuntimeMinutes = buffer.short.toInt(),
                cpuTemperature = buffer.get().toFloat() / 2f,
                uptimeSeconds = buffer.int.toLong(),
                cameraStatus = ComponentStatus.values()[buffer.get().toInt()],
                tofStatus = ComponentStatus.values()[buffer.get().toInt()],
                earbudStatus = ComponentStatus.values()[buffer.get().toInt()],
                aiEngineStatus = ComponentStatus.values()[buffer.get().toInt()],
                faceRecognitionStatus = ComponentStatus.values()[buffer.get().toInt()],
                inferenceFps = buffer.get().toFloat(),
                firmwareVersion = "1.0.0", // Mock parsing
                lastAlert = null
            )
            _telemetry.value = parsed
        } catch (e: Exception) {
            Log.e("BleConnectionManager", "Corrupted telemetry payload", e)
        }
    }

    private fun encodeCommand(command: DeviceCommand): ByteArray {
        return when (command) {
            DeviceCommand.StartWifi -> byteArrayOf(0x01)
            DeviceCommand.StopWifi -> byteArrayOf(0x02)
            DeviceCommand.StartSceneDescription -> byteArrayOf(0x03)
            DeviceCommand.StartFaceEnrollment -> byteArrayOf(0x04)
            DeviceCommand.RebootDevice -> byteArrayOf(0x05)
            DeviceCommand.ShutdownDevice -> byteArrayOf(0x06)
            DeviceCommand.TriggerSOS -> byteArrayOf(0x07)
        }
    }
}
