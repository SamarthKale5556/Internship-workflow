package com.example.blindassistcompanion.presentation.faces.camera

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.blindassistcompanion.domain.model.FaceAngle
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

class FaceAnalyzer(
    private val onFaceDetected: (Face, ImageProxy, String) -> Unit,
    private val onError: (String) -> Unit
) : ImageAnalysis.Analyzer {

    // High accuracy mode, evaluating Euler angles
    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
        .build()

    private val detector = FaceDetection.getClient(options)
    private var isProcessing = false

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        if (isProcessing) {
            imageProxy.close()
            return
        }
        
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        isProcessing = true
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        detector.process(image)
            .addOnSuccessListener { faces ->
                if (faces.isEmpty()) {
                    onError("No face detected")
                } else if (faces.size > 1) {
                    onError("Multiple faces detected")
                } else {
                    val face = faces.first()
                    // Basic bounding box validation to ensure they are close enough
                    val faceWidth = face.boundingBox.width()
                    val imageWidth = image.width
                    val faceRatio = faceWidth.toFloat() / imageWidth.toFloat()
                    
                    if (faceRatio < 0.3f) {
                        onError("Move closer")
                    } else {
                        onFaceDetected(face, imageProxy, "Face detected")
                    }
                }
            }
            .addOnFailureListener { e ->
                onError("Detection failed: ${e.localizedMessage}")
            }
            .addOnCompleteListener {
                isProcessing = false
                imageProxy.close()
            }
    }
}
