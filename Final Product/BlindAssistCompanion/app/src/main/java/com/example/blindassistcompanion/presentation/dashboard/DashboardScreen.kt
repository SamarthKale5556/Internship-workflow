package com.example.blindassistcompanion.presentation.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.blindassistcompanion.domain.model.ConnectionState
import com.example.blindassistcompanion.domain.model.PiTelemetry
import kotlinx.coroutines.delay

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToDeviceStatus: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAssistant: () -> Unit,
    onNavigateToFaces: () -> Unit,
    onNavigateToSos: (Boolean) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val sceneState by viewModel.sceneState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.sosTriggerEvent.collect {
            onNavigateToSos(true)
        }
    }

    val permissionsLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val recordAudioGranted = permissions[android.Manifest.permission.RECORD_AUDIO] ?: false
        if (recordAudioGranted) {
            viewModel.askAiAssistant()
        }
    }

    // Page Entrance Animations
    val animatedOffset = remember { Animatable(40f) }
    val animatedAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animatedOffset.animateTo(0f, animationSpec = tween(800, easing = EaseOutCubic))
    }
    LaunchedEffect(Unit) {
        animatedAlpha.animateTo(1f, animationSpec = tween(600, easing = EaseOutQuad))
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0B0B0F) // Premium Ultra Dark Background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Subtle top-right and bottom-left radial gradient glow
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 100.dp, y = (-100).dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0x1A6A11CB), Color.Transparent)
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationY = animatedOffset.value
                        alpha = animatedAlpha.value
                    }
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                HeaderSection(uiState.connectionState)

                ConnectionSuccessCard()

                // Header title with Indigo glow vertical bar
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(16.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))
                                )
                            )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "QUICK ACTIONS",
                        color = Color(0xFF7E8494),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }

                QuickActionsGrid(
                    onNavigateToDeviceStatus = onNavigateToDeviceStatus,
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToFaces = onNavigateToFaces,
                    onNavigateToSos = { onNavigateToSos(false) },
                    onTriggerAiAssistant = { 
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                            permissionsLauncher.launch(arrayOf(android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.BLUETOOTH_CONNECT))
                        } else {
                            permissionsLauncher.launch(arrayOf(android.Manifest.permission.RECORD_AUDIO))
                        }
                    },
                    onDescribeSurroundings = { viewModel.describeSurroundings() }
                )

                if (uiState.telemetry?.lastAlert != null) {
                    Text(
                        text = "LAST ALERT",
                        color = Color(0xFF7E8494),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    LastAlertCard(alert = uiState.telemetry!!.lastAlert!!)
                }

                NeuroEdgeCreditsCard()
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (sceneState !is com.example.blindassistcompanion.domain.scene.SceneDescriptionState.Idle) {
                SceneDescriptionOverlay(sceneState = sceneState) {
                    viewModel.resetSceneState()
                }
            }
        }
    }
}

@Composable
fun SceneDescriptionOverlay(
    sceneState: com.example.blindassistcompanion.domain.scene.SceneDescriptionState,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x66000000))
            .clickable(enabled = sceneState is com.example.blindassistcompanion.domain.scene.SceneDescriptionState.Success || sceneState is com.example.blindassistcompanion.domain.scene.SceneDescriptionState.Error) { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF3B3B4F), Color(0xFF1E1E2C))
                    ),
                    shape = RoundedCornerShape(24.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = Color(0xE6161622)), // Glassmorphic look
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (sceneState) {
                    is com.example.blindassistcompanion.domain.scene.SceneDescriptionState.Listening -> {
                        Icon(Icons.Rounded.Mic, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Listening...", color = Color.White, fontWeight = FontWeight.Medium)
                    }
                    is com.example.blindassistcompanion.domain.scene.SceneDescriptionState.ConnectingToCamera -> {
                        CircularProgressIndicator(color = Color(0xFF8E2DE2))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Connecting to camera...", color = Color.White, fontWeight = FontWeight.Medium)
                    }
                    is com.example.blindassistcompanion.domain.scene.SceneDescriptionState.FetchingImage -> {
                        CircularProgressIndicator(color = Color(0xFF8E2DE2))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Capturing image...", color = Color.White, fontWeight = FontWeight.Medium)
                    }
                    is com.example.blindassistcompanion.domain.scene.SceneDescriptionState.AnalyzingScene -> {
                        CircularProgressIndicator(color = Color(0xFF8E2DE2))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Analyzing scene...", color = Color.White, fontWeight = FontWeight.Medium)
                    }
                    is com.example.blindassistcompanion.domain.scene.SceneDescriptionState.Speaking -> {
                        Icon(Icons.Rounded.VolumeUp, contentDescription = null, tint = Color(0xFF4FC3F7), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Speaking description...", color = Color.White, fontWeight = FontWeight.Medium)
                    }
                    is com.example.blindassistcompanion.domain.scene.SceneDescriptionState.Success -> {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(sceneState.description, color = Color.White, fontSize = 15.sp, lineHeight = 22.sp)
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Tap to dismiss", color = Color.Gray, fontSize = 11.sp)
                    }
                    is com.example.blindassistcompanion.domain.scene.SceneDescriptionState.Error -> {
                        Icon(Icons.Rounded.Error, contentDescription = null, tint = Color(0xFFE57373), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(sceneState.message, color = Color.White, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Tap to dismiss", color = Color.Gray, fontSize = 11.sp)
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
fun HeaderSection(connectionState: ConnectionState) {
    // Breathing/Pulse animation for the connection status dot
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Hello User! 🙌",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .graphicsLayer { alpha = dotAlpha }
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Device connected",
                    color = Color(0xFF7E8494),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Icon(
            imageVector = Icons.Rounded.Person,
            contentDescription = "Profile",
            tint = Color.White,
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))
                    )
                )
                .padding(10.dp)
        )
    }
}

@Composable
fun ConnectionSuccessCard() {
    // Breathing/Pulse animation for the checkmark icon and its shadows
    val infiniteTransition = rememberInfiniteTransition(label = "successGlow")
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Floating particles offset animations
    val particleYOffset1 by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = -15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "p1"
    )
    val particleYOffset2 by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "p2"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF2C2C3C), Color(0xFF161622))
                ),
                shape = RoundedCornerShape(24.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13131D)), // Glassmorphism dark background
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(80.dp)
            ) {
                // Floating particles behind the icon
                Box(
                    modifier = Modifier
                        .offset(x = (-30).dp, y = particleYOffset1.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0x994CAF50))
                )
                Box(
                    modifier = Modifier
                        .offset(x = 35.dp, y = particleYOffset2.dp)
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(Color(0x9926A69A))
                )

                // Soft background radial glow under checkmark
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .blur(15.dp)
                        .background(Color(0x334CAF50))
                )

                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier
                        .size(56.dp)
                        .scale(breathingScale)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Connection Successful",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Connected with raspi zero 2 w",
                color = Color(0xFF7E8494),
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color(0xFF1E1E2F),
                        shape = RoundedCornerShape(16.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F16)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Wifi,
                            contentDescription = null,
                            tint = Color(0xFF26A69A),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Strong Signal",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50))
                        )
                    }
                    
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFF1E1E2C)))
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Schedule,
                                contentDescription = null,
                                tint = Color(0xFF29B6F6),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "12 ms",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Latency",
                            color = Color(0xFF7E8494),
                            fontSize = 9.sp
                        )
                    }
                    
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFF1E1E2C)))
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Security,
                                contentDescription = null,
                                tint = Color(0xFF66BB6A),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Secure",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Encrypted",
                            color = Color(0xFF7E8494),
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionsGrid(
    onNavigateToDeviceStatus: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToFaces: () -> Unit,
    onNavigateToSos: () -> Unit,
    onTriggerAiAssistant: () -> Unit,
    onDescribeSurroundings: () -> Unit
) {
    // Exact 6 action cards for premium symmetry (3 rows of 2 columns)
    val actions = listOf(
        QuickActionData("Add family member", "Face recognition", Icons.Rounded.GroupAdd, Color(0xFF7986CB), onNavigateToFaces),
        QuickActionData("Describe surroundings", "AI vision", Icons.Rounded.Visibility, Color(0xFF4DB6AC), onDescribeSurroundings),
        QuickActionData("Emergency SOS", "Alert contacts", Icons.Rounded.Warning, Color(0xFFE57373), onNavigateToSos),
        QuickActionData("Device status", "Pi diagnostics", Icons.Rounded.Memory, Color(0xFFFFB74D), onNavigateToDeviceStatus),
        QuickActionData("AI assistant", "Ask anything", Icons.Rounded.SmartToy, Color(0xFF64B5F6), onTriggerAiAssistant),
        QuickActionData("Settings", "Preferences", Icons.Rounded.Settings, Color(0xFF90A4AE), onNavigateToSettings)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.height(264.dp), // Height adjusted perfectly for exactly 3 rows
        userScrollEnabled = false // Prevent inner scrolling issues
    ) {
        items(actions) { action ->
            QuickActionCard(action)
        }
    }
}

data class QuickActionData(val title: String, val subtitle: String, val icon: ImageVector, val color: Color, val onClick: () -> Unit)

@Composable
fun QuickActionCard(data: QuickActionData) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Smooth press and animation transitions
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )
    val glowAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.35f else 0.15f,
        animationSpec = tween(200),
        label = "glow"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = data.onClick
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF2C2C3C), Color(0xFF161622))
                ),
                shape = RoundedCornerShape(22.dp) // Premium rounded corners (22dp+)
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13131D)),
        shape = RoundedCornerShape(22.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon container with soft colored background gradient glow
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(data.color.copy(alpha = glowAlpha)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = data.icon,
                    contentDescription = data.title,
                    tint = data.color,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(10.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = data.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = (-0.2).sp
                )
                Text(
                    text = data.subtitle,
                    color = Color(0xFF7E8494),
                    fontSize = 11.sp
                )
            }
            
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF4E515C),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun LastAlertCard(alert: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color(0xFF2C2220),
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1513)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.WarningAmber,
                contentDescription = "Alert",
                tint = Color(0xFFFFB74D),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = alert, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = "Just now", color = Color.Gray, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun NeuroEdgeCreditsCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "meshGlow")
    
    // Animate glowing floating particles in the background of the credits card
    val particleOffset1 by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "p1"
    )
    val particleOffset2 by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "p2"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF3B2F50), Color(0xFF181124))
                ),
                shape = RoundedCornerShape(24.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent), // Use background gradient instead
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF180E29), Color(0xFF0C0715))
                    )
                )
                .padding(20.dp)
        ) {
            // Floating background glow particles
            Box(
                modifier = Modifier
                    .offset(x = 20.dp, y = particleOffset1.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(Color(0x339575CD))
            )
            Box(
                modifier = Modifier
                    .offset(x = 260.dp, y = particleOffset2.dp)
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(Color(0x225C6BC0))
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Psychology,
                        contentDescription = null,
                        tint = Color(0xFFB39DDB),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "NeuroEdge",
                        style = LocalTextStyle.current.copy(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color.White, Color(0xFFB39DDB))
                            )
                        ),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Connecting • Protecting • Empowering",
                    color = Color(0xFF7E8494),
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = Color(0xFF231E30), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 24.dp))
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Developed by NeuroEdge — LY 2026",
                    color = Color(0xFF9095A6),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Team Members Divider with Groups Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                ) {
                    Divider(modifier = Modifier.weight(1f), color = Color(0xFF231E30), thickness = 0.5.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Group,
                            contentDescription = null,
                            tint = Color(0xFF7E57C2),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Team members",
                            color = Color(0xFF7E8494),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Divider(modifier = Modifier.weight(1f), color = Color(0xFF231E30), thickness = 0.5.dp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                TeamMemberRow(
                    number = "1",
                    name = "Samarth Kale",
                    role = "Edge AI Engineer & Team Leader",
                    badgeColor = Color(0xFF3F51B5)
                )
                Spacer(modifier = Modifier.height(12.dp))
                TeamMemberRow(
                    number = "2",
                    name = "Shrushti Shinde",
                    role = "Hardware & System Integration Engineer",
                    badgeColor = Color(0xFF4CAF50)
                )
                Spacer(modifier = Modifier.height(12.dp))
                TeamMemberRow(
                    number = "3",
                    name = "Shrikant Kudale",
                    role = "Product Design & Software Quality Engineer",
                    badgeColor = Color(0xFFFF9800)
                )

                Spacer(modifier = Modifier.height(20.dp))
                Divider(color = Color(0xFF231E30), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 24.dp))
                Spacer(modifier = Modifier.height(16.dp))

                // Built with passion footer
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Favorite,
                        contentDescription = null,
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Built with passion for a smarter & safer world.",
                        color = Color(0xFF7E8494),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun TeamMemberRow(
    number: String,
    name: String,
    role: String,
    badgeColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(badgeColor.copy(alpha = 0.2f))
                .border(1.dp, badgeColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = badgeColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = (-0.1).sp
            )
            Text(
                text = role,
                color = Color(0xFF7E8494),
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
