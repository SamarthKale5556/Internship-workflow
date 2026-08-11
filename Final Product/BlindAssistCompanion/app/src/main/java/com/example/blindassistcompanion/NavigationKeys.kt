package com.example.blindassistcompanion

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Main : NavKey
@Serializable data object DeviceStatus : NavKey
@Serializable data object Faces : NavKey
@Serializable data object AddFamily : NavKey
@Serializable data class Sos(val autoTrigger: Boolean = false) : NavKey
@Serializable data class Enrollment(val sessionId: String, val name: String, val relationship: String, val phoneNumber: String, val emergencyPriority: Boolean) : NavKey
@Serializable data class FamilyDetails(val uuid: String) : NavKey
