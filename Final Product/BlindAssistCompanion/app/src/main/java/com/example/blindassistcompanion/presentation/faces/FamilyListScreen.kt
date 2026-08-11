package com.example.blindassistcompanion.presentation.faces

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
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
import dagger.hilt.android.lifecycle.HiltViewModel
import com.example.blindassistcompanion.domain.model.FamilyMember
import com.example.blindassistcompanion.domain.usecase.GetFamilyMembersUseCase
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class FamilyListViewModel @Inject constructor(
    getFamilyMembersUseCase: GetFamilyMembersUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val familyMembers: StateFlow<List<FamilyMember>> = combine(
        getFamilyMembersUseCase(),
        _searchQuery
    ) { members, query ->
        if (query.isBlank()) {
            members
        } else {
            members.filter { it.name.contains(query, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyListScreen(
    viewModel: FamilyListViewModel,
    onNavigateBack: () -> Unit,
    onAddFamilyMember: () -> Unit,
    onFamilyMemberClick: (String) -> Unit
) {
    val members by viewModel.familyMembers.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        containerColor = Color(0xFF0B0B0F), // Consistent background
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Family & Friends", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Manage recognized faces & priorities", color = Color(0xFF7E8494), fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B0B0F))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddFamilyMember,
                containerColor = Color.Transparent,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))),
                        shape = CircleShape
                    ),
                elevation = FloatingActionButtonDefaults.elevation(0.dp) // Avoid square shadow below gradient
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Family Member")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(listOf(Color(0xFF2C2C3C), Color(0xFF161622))),
                        shape = RoundedCornerShape(16.dp)
                    ),
                placeholder = { Text("Search by name", color = Color(0xFF7E8494)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF7E8494)) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF13131D),
                    unfocusedContainerColor = Color(0xFF13131D),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            if (members.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No family members found.", color = Color(0xFF7E8494))
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(members) { member ->
                        FamilyMemberCard(member = member, onClick = { onFamilyMemberClick(member.uuid) })
                    }
                }
            }
        }
    }
}

@Composable
fun FamilyMemberCard(member: FamilyMember, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
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
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rounded avatar placeholder
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF201D2D)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = member.name.firstOrNull()?.toString()?.uppercase() ?: "",
                    color = Color(0xFFB39DDB),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = member.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(text = member.relationship, color = Color(0xFF7E8494), fontSize = 13.sp)
            }
            
            if (member.emergencyPriority) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Emergency Priority",
                    tint = Color(0xFFE57373),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
