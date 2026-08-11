package com.example.blindassistcompanion.presentation.sos

import android.content.Context
import android.location.Location
import android.telephony.SmsManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blindassistcompanion.domain.model.FamilyMember
import com.example.blindassistcompanion.domain.repository.FamilyRepository
import com.example.blindassistcompanion.domain.repository.PiCameraClient
import com.example.blindassistcompanion.domain.repository.TtsRepository
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SosViewModel @Inject constructor(
    private val familyRepository: FamilyRepository,
    private val ttsRepository: TtsRepository,
    private val piCameraClient: PiCameraClient,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val emergencyContacts: StateFlow<List<FamilyMember>> = familyRepository.getAllFamilyMembers()
        .map { members -> members.filter { it.emergencyPriority } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _isSosTriggered = MutableStateFlow(false)
    val isSosTriggered: StateFlow<Boolean> = _isSosTriggered

    private val _statusMessage = MutableStateFlow("")
    val statusMessage: StateFlow<String> = _statusMessage

    fun triggerSos() {
        if (_isSosTriggered.value) return
        _isSosTriggered.value = true
        _statusMessage.value = "Activating SOS emergency protocol..."
        
        viewModelScope.launch {
            ttsRepository.speak("Activating SOS emergency protocol. Requesting location.")
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                
                // Assuming permissions are already granted by the UI before calling this
                val location: Location? = fusedLocationClient.lastLocation.await()
                
                val message = if (location != null) {
                    "EMERGENCY SOS: I need help! My current location is: https://maps.google.com/?q=${location.latitude},${location.longitude}"
                } else {
                    "EMERGENCY SOS: I need help! (Location unavailable)"
                }

                _statusMessage.value = "Sending alerts..."

                val contacts = emergencyContacts.value
                val smsManager = context.getSystemService(SmsManager::class.java)

                var sentCount = 0
                for (contact in contacts) {
                    if (contact.phoneNumber.isNotBlank()) {
                        smsManager.sendTextMessage(contact.phoneNumber, null, message, null, null)
                        sentCount++
                    }
                }

                if (sentCount == 0) {
                    _statusMessage.value = "No emergency contacts configured"
                    ttsRepository.speak("No emergency contacts have been configured. Please add contacts in the app settings.")
                } else {
                    _statusMessage.value = "Alerts sent to $sentCount contacts"
                    ttsRepository.speak("Emergency alerts have been sent to $sentCount contacts with your location.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _statusMessage.value = "Failed to send alerts: ${e.message}"
                ttsRepository.speak("Failed to send emergency alerts. Please check your signal and permissions.")
                _isSosTriggered.value = false
            } finally {
                piCameraClient.resumeBackgroundAi()
            }
        }
    }
}
