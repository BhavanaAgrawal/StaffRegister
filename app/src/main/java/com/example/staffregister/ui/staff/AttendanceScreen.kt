package com.example.staffregister.ui.staff

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.staffregister.data.Attendance
import com.example.staffregister.data.MonthlyStats
import com.example.staffregister.data.Staff
import com.example.staffregister.ui.theme.StaffRegisterTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AttendanceScreen(
    viewModel: AttendanceViewModel,
    onBack: () -> Unit
) {
    val staff by viewModel.staff.collectAsState()
    val absenceList by viewModel.absenceList.collectAsState()
    val monthlyStats by viewModel.monthlyStats.collectAsState()

    AttendanceScreenContent(
        staff = staff,
        absenceList = absenceList,
        monthlyStats = monthlyStats,
        absentReason = viewModel.absentReason,
        onAbsentReasonChange = { viewModel.absentReason = it },
        absentDaysInput = viewModel.absentDaysInput,
        onAbsentDaysInputChange = { viewModel.absentDaysInput = it },
        onRecordAbsence = { viewModel.recordAbsence() },
        onDeleteAbsence = { viewModel.deleteAbsence(it) },
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreenContent(
    staff: Staff?,
    absenceList: List<Attendance>,
    monthlyStats: MonthlyStats?,
    absentReason: String,
    onAbsentReasonChange: (String) -> Unit,
    absentDaysInput: String,
    onAbsentDaysInputChange: (String) -> Unit,
    onRecordAbsence: () -> Unit,
    onDeleteAbsence: (Attendance) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(staff?.name ?: "Attendance") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Monthly Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem("Present", "${monthlyStats?.daysPresent ?: 0} Days")
                    StatItem("Absent", "${monthlyStats?.daysAbsent ?: 0} Days")
                    StatItem("Wages", "₹${String.format(Locale.getDefault(), "%.2f", monthlyStats?.totalSalary ?: 0.0)}")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Absence Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Record Absence",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = absentReason,
                        onValueChange = onAbsentReasonChange,
                        label = { Text("Reason for Absence") },
                        placeholder = { Text("e.g. Sick Leave, Personal") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = absentDaysInput,
                        onValueChange = onAbsentDaysInputChange,
                        label = { Text("Number of Absent Days") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Button(
                        onClick = onRecordAbsence,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Rounded.EventBusy, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("RECORD ABSENCE", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // History Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Absence Records", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (absenceList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No absence records.", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(absenceList) { record ->
                        AbsenceRecordItem(record, onDelete = { onDeleteAbsence(record) })
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun AbsenceRecordItem(
    record: Attendance,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = dateFormat.format(Date(record.date)),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${record.daysCount} Day${if (record.daysCount > 1) "s" else ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Reason: ${record.reason.ifEmpty { "Not specified" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Rounded.Delete,
                    contentDescription = "Delete record",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AttendanceScreenPreview() {
    StaffRegisterTheme {
        AttendanceScreenContent(
            staff = Staff(
                id = 1L,
                name = "John Doe",
                role = "Developer",
                contact = "john.doe@example.com",
                credentials = "johnd",
                dailyRate = 120.0
            ),
            absenceList = listOf(
                Attendance(
                    id = 1L,
                    staffId = 1L,
                    date = System.currentTimeMillis(),
                    isPresent = false,
                    reason = "Sick Leave",
                    daysCount = 1
                ),
                Attendance(
                    id = 2L,
                    staffId = 1L,
                    date = System.currentTimeMillis() - 86400000 * 3,
                    isPresent = false,
                    reason = "Personal Emergency",
                    daysCount = 2
                )
            ),
            monthlyStats = MonthlyStats(
                daysPresent = 15,
                daysAbsent = 3,
                totalSalary = 1800.0
            ),
            absentReason = "Family function",
            onAbsentReasonChange = {},
            absentDaysInput = "1",
            onAbsentDaysInputChange = {},
            onRecordAbsence = {},
            onDeleteAbsence = {},
            onBack = {}
        )
    }
}
