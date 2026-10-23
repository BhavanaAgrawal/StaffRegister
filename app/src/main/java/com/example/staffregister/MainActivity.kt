package com.example.staffregister

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.staffregister.data.Staff
import com.example.staffregister.navigation.Route
import com.example.staffregister.ui.AppViewModelProvider
import com.example.staffregister.ui.staff.AttendanceScreen
import com.example.staffregister.ui.staff.AttendanceViewModel
import com.example.staffregister.ui.staff.CameraCaptureScreen
import com.example.staffregister.ui.staff.StaffDetailScreen
import com.example.staffregister.ui.staff.StaffDetailViewModel
import com.example.staffregister.ui.staff.StaffListContent
import com.example.staffregister.ui.staff.StaffListScreen
import com.example.staffregister.ui.staff.StaffListViewModel
import com.example.staffregister.ui.theme.StaffRegisterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StaffRegisterTheme {
                MainContent()
            }
        }
    }
}

@Composable
fun MainContent() {
    val backStack = rememberNavBackStack(Route.StaffList as Route)
    val isPreview = LocalInspectionMode.current
    
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeAt(backStack.size - 1) },
        entryDecorators = if (isPreview) emptyList() else listOf(rememberViewModelStoreNavEntryDecorator()),
        entryProvider = entryProvider {
            entry<Route.StaffList> { _ ->
                if (isPreview) {
                    StaffListContent(
                        staffList = listOf(
                            Staff(id = 1, name = "Alice Smith", role = "Manager", contact = "1234567890", credentials = "MBA", dailyRate = 100.0),
                            Staff(id = 2, name = "Bob Jones", role = "Developer", contact = "0987654321", credentials = "BSc CS", dailyRate = 80.0)
                        ),
                        onAddStaff = {},
                        onStaffClick = {},
                        onEditStaff = {}
                    )
                } else {
                    val viewModel: StaffListViewModel = viewModel(factory = AppViewModelProvider.Factory)
                    StaffListScreen(
                        viewModel = viewModel,
                        onAddStaff = {
                            backStack.add(Route.StaffDetail())
                        },
                        onStaffClick = { staff ->
                            backStack.add(Route.Attendance(staff.id))
                        },
                        onEditStaff = { staff ->
                            backStack.add(Route.StaffDetail(staff.id))
                        }
                    )
                }
            }
            entry<Route.StaffDetail> { key ->
                val viewModel: StaffDetailViewModel = viewModel(
                    factory = AppViewModelProvider.detailFactory(key.staffId)
                )
                StaffDetailScreen(
                    viewModel = viewModel,
                    onBack = { backStack.removeAt(backStack.size - 1) },
                    onSaveSuccess = { backStack.removeAt(backStack.size - 1) }
                )
            }
            entry<Route.Attendance> { key ->
                val viewModel: AttendanceViewModel = viewModel(
                    factory = AppViewModelProvider.attendanceFactory(key.staffId)
                )
                AttendanceScreen(
                    viewModel = viewModel,
                    onBack = { backStack.removeAt(backStack.size - 1) }
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Preview(showBackground = true)
@Composable
fun MainContentPreview() {
    StaffRegisterTheme {
        MainContent()
    }
}
