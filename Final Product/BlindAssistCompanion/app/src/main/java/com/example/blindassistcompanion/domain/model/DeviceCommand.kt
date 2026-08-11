package com.example.blindassistcompanion.domain.model

sealed class DeviceCommand {
    object StartWifi : DeviceCommand()
    object StopWifi : DeviceCommand()
    object StartSceneDescription : DeviceCommand()
    object StartFaceEnrollment : DeviceCommand()
    object RebootDevice : DeviceCommand()
    object ShutdownDevice : DeviceCommand()
    object TriggerSOS : DeviceCommand()
}
