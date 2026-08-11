package com.example.blindassistcompanion.presentation.faces

import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blindassistcompanion.presentation.faces.camera.FaceAnalyzer
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnrollmentProgressScreen(
    viewModel: EnrollmentProgressViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onEnrollmentFinished: () -> Unit
) {
    val currentStep by viewModel.currentAngleIndex.collectAsState()
    val isComplete by viewModel.enrollmentComplete.collectAsState()
    val feedbackMessage by viewModel.feedbackMessage.collectAsState()

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(isComplete) {
        if (isComplete) {
            delay(1500)
            onEnrollmentFinished()
        }
    }

    Scaffold(
        containerColor = Color(0xFF0B0B0F), // Consistent space black background
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Face Enrollment", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Rotate your face slowly", color = Color(0xFF7E8494), fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Cancel", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B0B0F))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            
            // Camera Viewfinder Box
            var isFrontCamera by remember { mutableStateOf(true) }
            val cameraSelector = if (isFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA

            Box(
                modifier = Modifier
                    .size(280.dp)
                    .clip(CircleShape)
                    .border(
                        width = 3.dp,
                        brush = Brush.linearGradient(listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))),
                        shape = CircleShape
                    )
                    .background(Color(0xFF13131D)),
                contentAlignment = Alignment.Center
            ) {
                if (isComplete) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Complete",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(80.dp)
                    )
                } else {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            previewView
                        },
                        update = { previewView ->
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                            val executor = ContextCompat.getMainExecutor(context)
                            val analysisExecutor = Executors.newSingleThreadExecutor()

                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()

                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }

                                val imageAnalyzer = ImageAnalysis.Builder()
                                    .setTargetResolution(Size(480, 640))
                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                    .build()
                                    .also { analysis ->
                                        analysis.setAnalyzer(
                                            analysisExecutor,
                                            FaceAnalyzer(
                                                onFaceDetected = { face, proxy, msg ->
                                                    viewModel.processFace(face, proxy, msg)
                                                },
                                                onError = { err ->
                                                    viewModel.handleDetectionError(err)
                                                }
                                            )
                                        )
                                    }

                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner, cameraSelector, preview, imageAnalyzer
                                    )
                                } catch (e: Exception) {
                                    // Handle exception
                                }
                            }, executor)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (!isComplete) {
                IconButton(
                    onClick = { isFrontCamera = !isFrontCamera },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)))
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Flip Camera",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = feedbackMessage,
                color = if (feedbackMessage == "Capture successful!") Color(0xFF4CAF50) else Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Step ${currentStep + 1} of 5",
                color = Color(0xFF7E8494),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (i in 0 until 5) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    i < currentStep -> Color(0xFF4CAF50)
                                    i == currentStep -> Color(0xFF8E2DE2)
                                    else -> Color(0xFF2C2C3C)
                                }
                            )
                    )
                }
            }
        }
    }
}
