package com.example.blindassistcompanion.presentation.faces

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class AddFamilyViewModel : ViewModel() {
    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    private val _relationship = MutableStateFlow("")
    val relationship = _relationship.asStateFlow()

    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber = _phoneNumber.asStateFlow()

    private val _emergencyPriority = MutableStateFlow(false)
    val emergencyPriority = _emergencyPriority.asStateFlow()

    fun updateName(newName: String) { _name.value = newName }
    fun updateRelationship(newRel: String) { _relationship.value = newRel }
    fun updatePhoneNumber(newPhone: String) { _phoneNumber.value = newPhone }
    fun updateEmergencyPriority(isEmergency: Boolean) { _emergencyPriority.value = isEmergency }

    fun generateSessionId(): String = UUID.randomUUID().toString()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFamilyScreen(
    viewModel: AddFamilyViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onNavigateBack: () -> Unit,
    onContinueToCamera: (String, String, String, String, Boolean) -> Unit
) {
    val name by viewModel.name.collectAsState()
    val relationship by viewModel.relationship.collectAsState()
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val emergencyPriority by viewModel.emergencyPriority.collectAsState()

    Scaffold(
        containerColor = Color(0xFF0B0B0F), // Consistent space black background
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Add Family Member", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Enter contact & notification settings", color = Color(0xFF7E8494), fontSize = 12.sp)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Enter details before scanning face.",
                color = Color(0xFF7E8494),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = name,
                onValueChange = viewModel::updateName,
                label = { Text("Full Name", color = Color(0xFF7E8494)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF8E2DE2),
                    unfocusedBorderColor = Color(0xFF2C2C3C),
                    focusedContainerColor = Color(0xFF13131D),
                    unfocusedContainerColor = Color(0xFF13131D)
                ),
                shape = RoundedCornerShape(14.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = relationship,
                onValueChange = viewModel::updateRelationship,
                label = { Text("Relationship (e.g., Mother, Brother)", color = Color(0xFF7E8494)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF8E2DE2),
                    unfocusedBorderColor = Color(0xFF2C2C3C),
                    focusedContainerColor = Color(0xFF13131D),
                    unfocusedContainerColor = Color(0xFF13131D)
                ),
                shape = RoundedCornerShape(14.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = viewModel::updatePhoneNumber,
                label = { Text("Phone Number (Required for SOS)", color = Color(0xFF7E8494)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF8E2DE2),
                    unfocusedBorderColor = Color(0xFF2C2C3C),
                    focusedContainerColor = Color(0xFF13131D),
                    unfocusedContainerColor = Color(0xFF13131D)
                ),
                shape = RoundedCornerShape(14.dp)
            )
            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E1E2C), RoundedCornerShape(16.dp))
                    .background(Color(0xFF13131D), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Emergency Priority", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(text = "Will interrupt standard voice navigation alerts", color = Color(0xFF7E8494), fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = emergencyPriority,
                    onCheckedChange = viewModel::updateEmergencyPriority,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF7E57C2),
                        uncheckedThumbColor = Color(0xFF7E8494),
                        uncheckedTrackColor = Color(0xFF2C2C3C)
                    )
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))

            val cameraPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    val sessionId = viewModel.generateSessionId()
                    onContinueToCamera(sessionId, name, relationship, phoneNumber, emergencyPriority)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (name.isNotBlank() && relationship.isNotBlank() && phoneNumber.isNotBlank()) {
                            Brush.horizontalGradient(listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)))
                        } else {
                            Brush.horizontalGradient(listOf(Color(0xFF231A33), Color(0xFF181521)))
                        }
                    )
                    .clickable(enabled = name.isNotBlank() && relationship.isNotBlank() && phoneNumber.isNotBlank()) {
                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Continue to Face Scan",
                    color = if (name.isNotBlank() && relationship.isNotBlank() && phoneNumber.isNotBlank()) Color.White else Color(0xFF7E8494),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
