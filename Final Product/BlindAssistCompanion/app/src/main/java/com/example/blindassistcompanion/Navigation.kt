package com.example.blindassistcompanion

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.blindassistcompanion.presentation.dashboard.DashboardScreen
import com.example.blindassistcompanion.presentation.device.DeviceStatusScreen
import com.example.blindassistcompanion.presentation.faces.AddFamilyScreen
import com.example.blindassistcompanion.presentation.faces.EnrollmentProgressScreen
import com.example.blindassistcompanion.presentation.faces.EnrollmentProgressViewModel
import com.example.blindassistcompanion.presentation.faces.FamilyDetailsScreen
import com.example.blindassistcompanion.presentation.faces.FamilyListScreen

@Composable
fun MainNavigation() {
  val backStack = rememberNavBackStack(Main)

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<Main> {
          DashboardScreen(
              viewModel = hiltViewModel(),
              onNavigateToDeviceStatus = { backStack.add(DeviceStatus) },
              onNavigateToSettings = { /* TODO */ },
              onNavigateToAssistant = { /* TODO */ },
              onNavigateToFaces = { backStack.add(Faces) },
              onNavigateToSos = { autoTrigger -> backStack.add(Sos(autoTrigger)) }
          )
        }
        entry<DeviceStatus> {
            DeviceStatusScreen(
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }
        entry<Sos> { arg ->
            val viewModel: com.example.blindassistcompanion.presentation.sos.SosViewModel = hiltViewModel()
            val dashboardViewModel: com.example.blindassistcompanion.presentation.dashboard.DashboardViewModel = hiltViewModel()
            com.example.blindassistcompanion.presentation.sos.SosScreen(
                viewModel = viewModel,
                autoTrigger = arg.autoTrigger,
                onNavigateBack = { backStack.removeLastOrNull() },
                onSceneDescribe = { 
                    dashboardViewModel.describeSurroundings()
                    backStack.removeLastOrNull() 
                },
                onAiAssistant = { 
                    dashboardViewModel.askAiAssistant()
                    backStack.removeLastOrNull() 
                }
            )
        }
        entry<Faces> {
            FamilyListScreen(
                viewModel = hiltViewModel(),
                onNavigateBack = { backStack.removeLastOrNull() },
                onAddFamilyMember = { backStack.add(AddFamily) },
                onFamilyMemberClick = { uuid -> backStack.add(FamilyDetails(uuid)) }
            )
        }
        entry<AddFamily> {
            AddFamilyScreen(
                viewModel = viewModel(),
                onNavigateBack = { backStack.removeLastOrNull() },
                onContinueToCamera = { sessionId, name, relationship, phoneNumber, emergencyPriority -> 
                    backStack.add(Enrollment(sessionId, name, relationship, phoneNumber, emergencyPriority))
                }
            )
        }
        entry<Enrollment> { arg ->
            val viewModel: EnrollmentProgressViewModel = hiltViewModel<EnrollmentProgressViewModel>()
            LaunchedEffect(arg) {
                viewModel.initEnrollment(arg.name, arg.relationship, arg.phoneNumber, arg.emergencyPriority)
            }
            EnrollmentProgressScreen(
                viewModel = viewModel,
                onNavigateBack = { backStack.removeLastOrNull() },
                onEnrollmentFinished = { 
                    while (backStack.last() != Faces && backStack.size > 1) {
                        backStack.removeLastOrNull()
                    }
                }
            )
        }
        entry<FamilyDetails> { arg ->
            FamilyDetailsScreen(
                uuid = arg.uuid,
                viewModel = hiltViewModel(),
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }
      },
  )
}
