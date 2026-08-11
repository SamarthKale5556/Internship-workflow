package com.example.blindassistcompanion.data.ble

import java.util.UUID

object GattServiceDefinitions {
    // Primary Service UUID for Blind Assist AI
    val BLIND_ASSIST_SERVICE_UUID: UUID = UUID.fromString("12345678-1234-5678-1234-56789abcdef0")

    // Characteristics
    val TELEMETRY_CHARACTERISTIC_UUID: UUID = UUID.fromString("12345678-1234-5678-1234-56789abcdef1")
    val COMMAND_CHARACTERISTIC_UUID: UUID = UUID.fromString("12345678-1234-5678-1234-56789abcdef2")
    val ALERT_CHARACTERISTIC_UUID: UUID = UUID.fromString("12345678-1234-5678-1234-56789abcdef3")
    val EMERGENCY_CHARACTERISTIC_UUID: UUID = UUID.fromString("12345678-1234-5678-1234-56789abcdef4")
    
    // Client Characteristic Configuration Descriptor (CCCD) for enabling Notifications
    val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
}
