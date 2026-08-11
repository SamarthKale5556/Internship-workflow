package com.example.blindassistcompanion.presentation.faces

import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blindassistcompanion.domain.model.FaceAngle
import com.google.mlkit.vision.face.Face
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel
import com.example.blindassistcompanion.domain.model.FamilyMember
import com.example.blindassistcompanion.domain.usecase.EnrollFaceUseCase
import com.example.blindassistcompanion.domain.usecase.SyncFamilyDatabaseUseCase
import com.example.blindassistcompanion.data.ml.FaceEmbeddingPipeline

@HiltViewModel
class EnrollmentProgressViewModel @Inject constructor(
    private val faceEmbeddingPipeline: FaceEmbeddingPipeline,
    private val enrollFaceUseCase: EnrollFaceUseCase,
    private val syncFamilyDatabaseUseCase: SyncFamilyDatabaseUseCase
) : ViewModel() {
    private val requiredAngles = listOf(
        FaceAngle.FRONT,
        FaceAngle.LEFT,
        FaceAngle.RIGHT,
        FaceAngle.UP,
        FaceAngle.DOWN
    )
    
    private val _currentAngleIndex = MutableStateFlow(0)
    val currentAngleIndex = _currentAngleIndex.asStateFlow()

    private val _isEnrolling = MutableStateFlow(true)
    val isEnrolling = _isEnrolling.asStateFlow()

    private val _enrollmentComplete = MutableStateFlow(false)
    val enrollmentComplete = _enrollmentComplete.asStateFlow()

    private val _feedbackMessage = MutableStateFlow("Look straight ahead")
    val feedbackMessage = _feedbackMessage.asStateFlow()

    private var captureLock = false
    
    // Member details passed from previous screen
    private var memberName = ""
    private var memberRelationship = ""
    private var memberPhoneNumber = ""
    private var emergencyPriority = false

    private val collectedEmbeddings = mutableListOf<FloatArray>()

    fun initEnrollment(name: String, relationship: String, phoneNumber: String, priority: Boolean) {
        memberName = name
        memberRelationship = relationship
        memberPhoneNumber = phoneNumber
        emergencyPriority = priority
    }

    fun processFace(face: Face, imageProxy: ImageProxy, defaultMessage: String) {
        if (_enrollmentComplete.value || captureLock) return

        val currentRequiredAngle = requiredAngles[_currentAngleIndex.value]
        
        val headEulerY = face.headEulerAngleY
        val headEulerX = face.headEulerAngleX

        val isValid = when (currentRequiredAngle) {
            FaceAngle.FRONT -> Math.abs(headEulerY) < 10f && Math.abs(headEulerX) < 10f
            FaceAngle.LEFT -> headEulerY > 20f && Math.abs(headEulerX) < 15f
            FaceAngle.RIGHT -> headEulerY < -20f && Math.abs(headEulerX) < 15f
            FaceAngle.UP -> headEulerX > 15f && Math.abs(headEulerY) < 15f
            FaceAngle.DOWN -> headEulerX < -15f && Math.abs(headEulerY) < 15f
        }

        if (isValid) {
            _feedbackMessage.value = "Capture successful!"
            captureLock = true
            
            viewModelScope.launch {
                // Generate embedding for this angle
                val embedding = faceEmbeddingPipeline.generateEmbedding(imageProxy, face)
                collectedEmbeddings.add(embedding)
                
                delay(1000) 
                
                if (_currentAngleIndex.value < requiredAngles.size - 1) {
                    _currentAngleIndex.value += 1
                    updatePromptForNextAngle()
                    captureLock = false
                } else {
                    finalizeEnrollment()
                }
            }
        } else {
            _feedbackMessage.value = when (currentRequiredAngle) {
                FaceAngle.FRONT -> "Look straight ahead"
                FaceAngle.LEFT -> "Turn slightly left"
                FaceAngle.RIGHT -> "Turn slightly right"
                FaceAngle.UP -> "Look up"
                FaceAngle.DOWN -> "Look down"
            }
        }
    }

    private suspend fun finalizeEnrollment() {
        _isEnrolling.value = false
        _feedbackMessage.value = "Processing and Saving..."
        
        // 1. Average embeddings
        val masterEmbedding = faceEmbeddingPipeline.aggregateEmbeddings(collectedEmbeddings)
        
        // 2. Create FamilyMember domain model
        val member = FamilyMember(
            uuid = java.util.UUID.randomUUID().toString(),
            name = memberName,
            relationship = memberRelationship,
            phoneNumber = memberPhoneNumber,
            emergencyPriority = emergencyPriority,
            embedding = masterEmbedding,
            createdTimestamp = System.currentTimeMillis(),
            lastSeenTimestamp = 0L,
            recognitionCount = 0,
            averageConfidence = 0f,
            highestConfidence = 0f,
            lowestConfidence = 0f
        )
        
        // 3. Save to Room DB
        enrollFaceUseCase(member)
        
        // 4. Sync to Raspberry Pi
        syncFamilyDatabaseUseCase(member)
        
        // 5. Memory Cleanup
        collectedEmbeddings.clear()
        
        _feedbackMessage.value = "Synced to Raspberry Pi!"
        delay(1000)
        _enrollmentComplete.value = true
    }

    fun handleDetectionError(error: String) {
        if (!captureLock) {
            _feedbackMessage.value = error
        }
    }

    private fun updatePromptForNextAngle() {
        _feedbackMessage.value = when (requiredAngles[_currentAngleIndex.value]) {
            FaceAngle.LEFT -> "Please turn head slightly left"
            FaceAngle.RIGHT -> "Please turn head slightly right"
            FaceAngle.UP -> "Please look up"
            FaceAngle.DOWN -> "Please look down"
            else -> "Look straight ahead"
        }
    }
}
