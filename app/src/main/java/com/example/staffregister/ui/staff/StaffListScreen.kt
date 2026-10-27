package com.example.staffregister.ui.staff

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.staffregister.data.Staff
import com.example.staffregister.ui.theme.StaffRegisterTheme

@Composable
fun StaffListScreen(
    viewModel: StaffListViewModel,
    onAddStaff: () -> Unit,
    onStaffClick: (Staff) -> Unit,
    onEditStaff: (Staff) -> Unit
) {
    val staffList by viewModel.staffList.collectAsState()

    StaffListContent(
        staffList = staffList,
        onAddStaff = onAddStaff,
        onStaffClick = onStaffClick,
        onEditStaff = onEditStaff
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffListContent(
    staffList: List<Staff>,
    onAddStaff: () -> Unit,
    onStaffClick: (Staff) -> Unit,
    onEditStaff: (Staff) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Staff Directory") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            LargeFloatingActionButton(onClick = onAddStaff) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Staff")
            }
        }
    ) { padding ->
        if (staffList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No staff registered yet.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(staffList) { staff ->
                    StaffItem(
                        staff = staff,
                        onClick = { onStaffClick(staff) },
                        onEditClick = { onEditStaff(staff) }
                    )
                }
            }
        }
    }
}

@Composable
fun StaffItem(
    staff: Staff,
    onClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Rounded.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = staff.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = staff.role,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            IconButton(onClick = onEditClick) {
                Icon(
                    Icons.Rounded.Edit,
                    contentDescription = "Edit Staff",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StaffItemPreview() {
    StaffRegisterTheme {
        StaffItem(
            staff = Staff(
                id = 1,
                name = "John Doe",
                role = "Software Engineer",
                contact = "1234567890",
                credentials = "BS Computer Science",
                dailyRate = 25.0
            ),
            onClick = {},
            onEditClick = {}
        )
    }
}

@Preview(name = "Light Mode", showBackground = true, showSystemUi = true)
@Preview(name = "Dark Mode", showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun StaffListScreenPreview() {
    StaffRegisterTheme {
        StaffListContent(
            staffList = listOf(
                Staff(id = 1, name = "Alice Smith", role = "Manager", contact = "1234567890", credentials = "MBA", dailyRate = 100.0),
                Staff(id = 2, name = "Bob Jones", role = "Developer", contact = "0987654321", credentials = "BSc CS", dailyRate = 80.0)
            ),
            onAddStaff = {},
            onStaffClick = {},
            onEditStaff = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun StaffListScreenEmptyPreview() {
    StaffRegisterTheme {
        StaffListContent(
            staffList = emptyList(),
            onAddStaff = {},
            onStaffClick = {},
            onEditStaff = {}
        )
    }
}
