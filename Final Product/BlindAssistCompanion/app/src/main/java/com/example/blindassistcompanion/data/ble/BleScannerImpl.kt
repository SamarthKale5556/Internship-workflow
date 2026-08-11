package com.example.blindassistcompanion.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.ParcelUuid
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

@SuppressLint("MissingPermission")
class BleScannerImpl @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter?
) {
    fun startScan(): Flow<BluetoothDevice> = callbackFlow {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            close(IllegalStateException("Bluetooth is disabled or not available"))
            return@callbackFlow
        }

        val scanner = bluetoothAdapter.bluetoothLeScanner
        if (scanner == null) {
            close(IllegalStateException("Bluetooth LE Scanner is not available"))
            return@callbackFlow
        }

        val scanFilter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(GattServiceDefinitions.BLIND_ASSIST_SERVICE_UUID))
            .build()

        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)
                trySend(result.device)
            }

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                close(RuntimeException("Scan failed with error code: $errorCode"))
            }
        }

        scanner.startScan(listOf(scanFilter), scanSettings, scanCallback)

        awaitClose {
            scanner.stopScan(scanCallback)
        }
    }
}
