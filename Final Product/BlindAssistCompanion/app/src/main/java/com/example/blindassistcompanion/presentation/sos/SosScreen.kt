package com.example.blindassistcompanion.presentation.sos

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SosScreen(
    viewModel: SosViewModel,
    autoTrigger: Boolean = false,
    onNavigateBack: () -> Unit,
    onSceneDescribe: () -> Unit,
    onAiAssistant: () -> Unit
) {
    val emergencyContacts by viewModel.emergencyContacts.collectAsState()
    val isSosTriggered by viewModel.isSosTriggered.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    var holdProgress by remember { mutableStateOf(0f) }
    var isHolding by remember { mutableStateOf(false) }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            viewModel.triggerSos()
        }
    }

    LaunchedEffect(autoTrigger) {
        if (autoTrigger && !isSosTriggered) {
            permissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(isHolding) {
        if (isHolding) {
            while (holdProgress < 1f) {
                delay(30)
                holdProgress += 0.01f
                if (holdProgress >= 1f) {
                    permissionsLauncher.launch(
                        arrayOf(
                            Manifest.permission.SEND_SMS,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            }
        } else {
            holdProgress = 0f
        }
    }

    Scaffold(
        containerColor = Color(0xFF0B0B0F), // Ultra dark background
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Emergency SOS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Alert your emergency contacts", color = Color(0xFF7E8494), fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B0B0F))
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                
                // Big Red SOS Button
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2C1313))
                        .border(2.dp, Color(0xFFE53935).copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (isSosTriggered) 200.dp else 180.dp + (holdProgress * 20).dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFFFF5252), Color(0xFFD32F2F))
                                )
                            )
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = { _ ->
                                        isHolding = true
                                        tryAwaitRelease()
                                        isHolding = false
                                    },
                                    onDoubleTap = { _ ->
                                        onAiAssistant()
                                    },
                                    onTap = { _ ->
                                        onSceneDescribe()
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "PUSH",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = if (isSosTriggered) statusMessage else "Tap to activate (will request permissions if needed)",
                    color = Color(0xFF7E8494),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action Buttons Grid
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(
                        onClick = { /* TODO */ },
                        modifier = Modifier.weight(1f).height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.horizontalGradient(listOf(Color(0xFF2C2C3C), Color(0xFF161622)))
                        )
                    ) {
                        Text("Call contact", fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedButton(
                        onClick = { /* TODO */ },
                        modifier = Modifier.weight(1f).height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.horizontalGradient(listOf(Color(0xFF2C2C3C), Color(0xFF161622)))
                        )
                    ) {
                        Text("Share location", fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(
                        onClick = { /* TODO */ },
                        modifier = Modifier.weight(1f).height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.horizontalGradient(listOf(Color(0xFF2C2C3C), Color(0xFF161622)))
                        )
                    ) {
                        Text("WhatsApp alert", fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedButton(
                        onClick = { /* TODO */ },
                        modifier = Modifier.weight(1f).height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.horizontalGradient(listOf(Color(0xFF2C2C3C), Color(0xFF161622)))
                        )
                    ) {
                        Text("Send SMS", fontWeight = FontWeight.SemiBold)
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "EMERGENCY CONTACTS",
                    color = Color(0xFF7E8494),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }

            items(emergencyContacts) { contact ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF2C2C3C), Color(0xFF161622))
                            ),
                            shape = RoundedCornerShape(22.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF13131D)),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            val initials = contact.name.split(" ").mapNotNull { it.firstOrNull() }.joinToString("").take(2).uppercase()
                            Text(initials, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column {
                            Text(contact.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("${contact.relationship} · ${contact.phoneNumber}", color = Color(0xFF7E8494), fontSize = 14.sp)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { /* Navigate to add emergency contact */ },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.horizontalGradient(listOf(Color(0xFF2C2C3C), Color(0xFF161622)))
                    )
                ) {
                    Text("Add emergency contact", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
