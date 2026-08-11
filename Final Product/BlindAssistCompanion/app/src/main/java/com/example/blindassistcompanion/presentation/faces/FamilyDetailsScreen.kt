package com.example.blindassistcompanion.presentation.faces

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
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
import androidx.lifecycle.viewModelScope
import com.example.blindassistcompanion.domain.model.FamilyMember
import com.example.blindassistcompanion.domain.usecase.DeleteFamilyMemberUseCase
import com.example.blindassistcompanion.domain.usecase.GetFamilyMembersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FamilyDetailsViewModel @Inject constructor(
    private val getFamilyMembersUseCase: GetFamilyMembersUseCase,
    private val deleteFamilyMemberUseCase: DeleteFamilyMemberUseCase
) : ViewModel() {

    private val _memberUuid = MutableStateFlow<String?>(null)

    val familyMember: StateFlow<FamilyMember?> = combine(
        getFamilyMembersUseCase(),
        _memberUuid
    ) { members, uuid ->
        members.find { it.uuid == uuid }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun loadMember(uuid: String) {
        _memberUuid.value = uuid
    }

    fun deleteMember(onDeleted: () -> Unit) {
        val uuid = _memberUuid.value ?: return
        viewModelScope.launch {
            deleteFamilyMemberUseCase(uuid)
            onDeleted()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyDetailsScreen(
    uuid: String,
    viewModel: FamilyDetailsViewModel,
    onNavigateBack: () -> Unit
) {
    val member by viewModel.familyMember.collectAsState()

    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(uuid) {
        viewModel.loadMember(uuid)
    }

    Scaffold(
        containerColor = Color(0xFF0B0B0F), // Consistent space black background
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Family Details", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Analyze face recognition patterns", color = Color(0xFF7E8494), fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Handle Edit */ }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE57373))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B0B0F))
            )
        }
    ) { paddingValues ->
        if (member == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF8E2DE2))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Styled profile photo container
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))
                            )
                        )
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color(0xFF0B0B0F)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFFB39DDB),
                            modifier = Modifier.size(54.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = member!!.name, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp)
                Text(text = member!!.relationship, color = Color(0xFF7E8494), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                
                if (member!!.emergencyPriority) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x33FF5252))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "🚨 EMERGENCY PRIORITY",
                            color = Color(0xFFFF5252),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                Divider(color = Color(0xFF1E1E2C), thickness = 0.5.dp)

                AnalyticsRow("Recognition Count", "${member!!.recognitionCount}")
                AnalyticsRow("Average Confidence", "${(member!!.averageConfidence * 100).toInt()}%")
                AnalyticsRow("Highest Confidence", "${(member!!.highestConfidence * 100).toInt()}%")
            }
        }

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Delete ${member?.name}?", fontWeight = FontWeight.Bold) },
                text = { Text("This will permanently remove their face embedding from the device and the Raspberry Pi.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteMember(onDeleted = onNavigateBack)
                        }
                    ) {
                        Text("Delete", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text("Cancel", color = Color(0xFF7E8494))
                    }
                },
                containerColor = Color(0xFF13131D),
                titleContentColor = Color.White,
                textContentColor = Color(0xFF9095A6),
                modifier = Modifier.border(1.dp, Color(0xFF2C2C3C), RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp)
            )
        }
    }
}

@Composable
fun AnalyticsRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color(0xFF7E8494), fontSize = 15.sp, fontWeight = FontWeight.Medium)
        Text(text = value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
    Divider(color = Color(0xFF1E1E2C), thickness = 0.5.dp)
}
