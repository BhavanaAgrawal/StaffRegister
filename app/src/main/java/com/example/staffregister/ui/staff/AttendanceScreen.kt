package com.example.staffregister.ui.staff

import android.app.DatePickerDialog
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.staffregister.data.Advance
import com.example.staffregister.data.Attendance
import com.example.staffregister.data.MonthlySalaryRecord
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
    val advanceList by viewModel.advanceList.collectAsState()
    val monthlyStats by viewModel.monthlyStats.collectAsState()
    val monthlySalaryRecords by viewModel.monthlySalaryRecords.collectAsState()

    AttendanceScreenContent(
        staff = staff,
        absenceList = absenceList,
        advanceList = advanceList,
        monthlyStats = monthlyStats,
        monthlySalaryRecords = monthlySalaryRecords,
        absentReason = viewModel.absentReason,
        onAbsentReasonChange = { viewModel.absentReason = it },
        absentDaysInput = viewModel.absentDaysInput,
        onAbsentDaysInputChange = { viewModel.absentDaysInput = it },
        selectedDate = viewModel.selectedDate,
        onSelectedDateChange = { viewModel.selectedDate = it },
        onRecordAbsence = { viewModel.recordAbsence() },
        onMarkPresentForDate = { viewModel.markPresentForDate(it) },
        onRecordAbsenceForDate = { date, reason, days -> viewModel.recordAbsenceForDate(date, reason, days) },
        onDeleteAbsence = { viewModel.deleteAbsence(it) },
        advanceAmountInput = viewModel.advanceAmountInput,
        onAdvanceAmountInputChange = { viewModel.advanceAmountInput = it },
        advanceNotesInput = viewModel.advanceNotesInput,
        onAdvanceNotesInputChange = { viewModel.advanceNotesInput = it },
        selectedAdvanceDate = viewModel.selectedAdvanceDate,
        onSelectedAdvanceDateChange = { viewModel.selectedAdvanceDate = it },
        onRecordAdvance = { viewModel.recordAdvance() },
        onDeleteAdvance = { viewModel.deleteAdvance(it) },
        onMarkSalaryPaid = { record, amount, notes -> viewModel.markSalaryPaid(record, amount, notes) },
        onMarkSalaryUnpaid = { record -> viewModel.markSalaryUnpaid(record) },
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreenContent(
    staff: Staff?,
    absenceList: List<Attendance>,
    advanceList: List<Advance>,
    monthlyStats: MonthlyStats?,
    monthlySalaryRecords: List<MonthlySalaryRecord>,
    absentReason: String,
    onAbsentReasonChange: (String) -> Unit,
    absentDaysInput: String,
    onAbsentDaysInputChange: (String) -> Unit,
    selectedDate: Long,
    onSelectedDateChange: (Long) -> Unit,
    onRecordAbsence: () -> Unit,
    onMarkPresentForDate: (Long) -> Unit,
    onRecordAbsenceForDate: (Long, String, Int) -> Unit,
    onDeleteAbsence: (Attendance) -> Unit,
    advanceAmountInput: String,
    onAdvanceAmountInputChange: (String) -> Unit,
    advanceNotesInput: String,
    onAdvanceNotesInputChange: (String) -> Unit,
    selectedAdvanceDate: Long,
    onSelectedAdvanceDateChange: (Long) -> Unit,
    onRecordAdvance: () -> Unit,
    onDeleteAdvance: (Advance) -> Unit,
    onMarkSalaryPaid: (MonthlySalaryRecord, Double, String) -> Unit,
    onMarkSalaryUnpaid: (MonthlySalaryRecord) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    var selectedTabIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(staff?.name ?: "Staff Management") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Current Month Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem("Present", "${monthlyStats?.daysPresent ?: 0} Days")
                        StatItem("Absent", "${monthlyStats?.daysAbsent ?: 0} Days")
                        //StatItem("Gross Wages", "₹${String.format(Locale.getDefault(), "%.2f", monthlyStats?.totalSalary ?: 0.0)}")
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val netVal = monthlyStats?.netSalary ?: 0.0
                        val netColor = if (netVal < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer
                        StatItem("Advance Taken", "₹${String.format(Locale.getDefault(), "%.2f", monthlyStats?.totalAdvance ?: 0.0)}")
                        StatItem("Net Amount", "₹${String.format(Locale.getDefault(), "%.2f", netVal)}", valueColor = netColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation Tabs (3 Tabs)
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Attendance", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Advance", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = { Text("Salary & Payments", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTabIndex == 0) {
                // Monthly Calendar View
                MonthlyCalendarView(
                    absenceList = absenceList,
                    onMarkPresentForDate = onMarkPresentForDate,
                    onRecordAbsenceForDate = onRecordAbsenceForDate
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Absence Form Card
            /*    Card(
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

                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val cal = Calendar.getInstance().apply { timeInMillis = selectedDate }
                                    DatePickerDialog(
                                        context,
                                        { _, year, month, dayOfMonth ->
                                            val newCal = Calendar.getInstance().apply {
                                                set(Calendar.YEAR, year)
                                                set(Calendar.MONTH, month)
                                                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                            }
                                            onSelectedDateChange(newCal.timeInMillis)
                                        },
                                        cal.get(Calendar.YEAR),
                                        cal.get(Calendar.MONTH),
                                        cal.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Selected Date", style = MaterialTheme.typography.labelSmall)
                                        Text(
                                            text = dateFormat.format(Date(selectedDate)),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = "CHANGE",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }*/

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

                // Absence History Header
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
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No absence records.", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        absenceList.forEach { record ->
                            AbsenceRecordItem(record, onDelete = { onDeleteAbsence(record) })
                        }
                    }
                }
            } else if (selectedTabIndex == 1) {
                // Record Advance Form Card
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
                            text = "Record Advance Taken",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val cal = Calendar.getInstance().apply { timeInMillis = selectedAdvanceDate }
                                    DatePickerDialog(
                                        context,
                                        { _, year, month, dayOfMonth ->
                                            val newCal = Calendar.getInstance().apply {
                                                set(Calendar.YEAR, year)
                                                set(Calendar.MONTH, month)
                                                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                            }
                                            onSelectedAdvanceDateChange(newCal.timeInMillis)
                                        },
                                        cal.get(Calendar.YEAR),
                                        cal.get(Calendar.MONTH),
                                        cal.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Advance Date", style = MaterialTheme.typography.labelSmall)
                                        Text(
                                            text = dateFormat.format(Date(selectedAdvanceDate)),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = "CHANGE",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        OutlinedTextField(
                            value = advanceAmountInput,
                            onValueChange = onAdvanceAmountInputChange,
                            label = { Text("Advance Amount (₹)") },
                            placeholder = { Text("e.g. 2000") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = advanceNotesInput,
                            onValueChange = onAdvanceNotesInputChange,
                            label = { Text("Notes / Reason") },
                            placeholder = { Text("e.g. Personal Emergency") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Button(
                            onClick = onRecordAdvance,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("RECORD ADVANCE", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Advance History Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Advance History", style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (advanceList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No advance records taken.", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        advanceList.forEach { record ->
                            AdvanceRecordItem(record, onDelete = { onDeleteAdvance(record) })
                        }
                    }
                }
            } else {
                // Tab 2: Salary & Payments
                SalaryAndPaymentsView(
                    staff = staff,
                    monthlySalaryRecords = monthlySalaryRecords,
                    onMarkSalaryPaid = onMarkSalaryPaid,
                    onMarkSalaryUnpaid = onMarkSalaryUnpaid
                )
            }
        }
    }
}

@Composable
fun SalaryAndPaymentsView(
    staff: Staff?,
    monthlySalaryRecords: List<MonthlySalaryRecord>,
    onMarkSalaryPaid: (MonthlySalaryRecord, Double, String) -> Unit,
    onMarkSalaryUnpaid: (MonthlySalaryRecord) -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val joiningDateStr = staff?.let { dateFormat.format(Date(it.joiningDate)) } ?: "N/A"

    val totalPending = monthlySalaryRecords.filter { !it.isPaid }.sumOf { it.netPayable }
    val totalPaid = monthlySalaryRecords.filter { it.isPaid }.sumOf { it.amountPaid }

    var selectedRecordForPayment by remember { mutableStateOf<MonthlySalaryRecord?>(null) }

    if (selectedRecordForPayment != null) {
        MarkPaymentDoneDialog(
            record = selectedRecordForPayment!!,
            onDismiss = { selectedRecordForPayment = null },
            onConfirm = { amount, notes ->
                onMarkSalaryPaid(selectedRecordForPayment!!, amount, notes)
                selectedRecordForPayment = null
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Joining Date",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = joiningDateStr,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Surface(
                        color = if (totalPending > 0) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ) {
                        Text(
                            text = if (totalPending > 0) "Pending Dues" else "All Paid",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (totalPending > 0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val pendingColor = if (totalPending < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSecondaryContainer
                    StatItem("Total Unpaid Dues", "₹${String.format(Locale.getDefault(), "%.2f", totalPending)}", valueColor = pendingColor)
                    StatItem("Total Paid", "₹${String.format(Locale.getDefault(), "%.2f", totalPaid)}")
                }
            }
        }

        // Section Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.Payments,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Monthly Payment Records",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (monthlySalaryRecords.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No payment records found starting from joining date.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                monthlySalaryRecords.forEach { record ->
                    MonthlySalaryCard(
                        record = record,
                        onMarkDoneClick = { selectedRecordForPayment = record },
                        onMarkUnpaidClick = { onMarkSalaryUnpaid(record) }
                    )
                }
            }
        }
    }
}

@Composable
fun MonthlySalaryCard(
    record: MonthlySalaryRecord,
    onMarkDoneClick: () -> Unit,
    onMarkUnpaidClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (record.isPaid) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (record.isPaid) 1.dp else 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Month Header & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = record.monthName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (record.isPaid) {
                    Surface(
                        color = Color(0xFF4CAF50),
                        shape = CircleShape
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "DONE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    Surface(
                        color = MaterialTheme.colorScheme.error,
                        shape = CircleShape
                    ) {
                        Text(
                            text = "PENDING",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

            // Calculation Breakdown Rows
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Gross Salary (${record.daysWorked}/${record.daysInMonth} days)",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "₹${String.format(Locale.getDefault(), "%.2f", record.grossSalary)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            if (record.advanceDeducted > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Advance Deducted",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "-₹${String.format(Locale.getDefault(), "%.2f", record.advanceDeducted)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Carry Forward from Previous Month
            if (record.carryForwardFromPrevious > 0) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Previous Pending Carry Forward",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "+₹${String.format(Locale.getDefault(), "%.2f", record.carryForwardFromPrevious)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

            // Net Payable
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Net Payable",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                val netPayableColor = if (record.netPayable < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                Text(
                    text = "₹${String.format(Locale.getDefault(), "%.2f", record.netPayable)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = netPayableColor
                )
            }

            // Action / Status Details
            if (record.isPaid) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Paid: ₹${String.format(Locale.getDefault(), "%.2f", record.amountPaid)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                        record.paidDate?.let { date ->
                            Text(
                                text = "Paid on ${dateFormat.format(Date(date))}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    TextButton(onClick = onMarkUnpaidClick) {
                        Text("Mark as Unpaid", style = MaterialTheme.typography.labelSmall)
                    }
                }
            } else {
                Button(
                    onClick = onMarkDoneClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Rounded.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("MARK PAYMENT DONE", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MarkPaymentDoneDialog(
    record: MonthlySalaryRecord,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit
) {
    var amountInput by remember { mutableStateOf(String.format(Locale.US, "%.2f", record.netPayable)) }
    var notesInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Confirm Salary Payment",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = record.monthName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text("Net Calculated Due", style = MaterialTheme.typography.labelSmall)
                        val netDueColor = if (record.netPayable < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer
                        Text(
                            text = "₹${String.format(Locale.getDefault(), "%.2f", record.netPayable)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = netDueColor
                        )
                        if (record.carryForwardFromPrevious > 0) {
                            Text(
                                text = "(Includes ₹${String.format(Locale.getDefault(), "%.2f", record.carryForwardFromPrevious)} carried forward)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Amount Paid (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Payment Notes / Mode") },
                    placeholder = { Text("e.g. Bank Transfer, Cash") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountInput.toDoubleOrNull() ?: record.netPayable
                    onConfirm(amount, notesInput.trim())
                }
            ) {
                Text("Confirm Paid", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun MonthlyCalendarView(
    absenceList: List<Attendance>,
    onMarkPresentForDate: (Long) -> Unit,
    onRecordAbsenceForDate: (Long, String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var calendarState by remember { mutableStateOf(Calendar.getInstance()) }
    val currentMonth = calendarState.get(Calendar.MONTH)
    val currentYear = calendarState.get(Calendar.YEAR)

    var editingDateInMillis by remember { mutableStateOf<Long?>(null) }

    val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val monthTitle = monthYearFormat.format(calendarState.time)

    val tempCal = (calendarState.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) - 1

    val todayCal = Calendar.getInstance()
    val isCurrentMonth = todayCal.get(Calendar.MONTH) == currentMonth && todayCal.get(Calendar.YEAR) == currentYear
    val todayDay = if (isCurrentMonth) todayCal.get(Calendar.DAY_OF_MONTH) else if (calendarState.before(todayCal)) 31 else 0

    if (editingDateInMillis != null) {
        EditAttendanceDialog(
            dateInMillis = editingDateInMillis!!,
            absenceList = absenceList,
            onDismiss = { editingDateInMillis = null },
            onMarkPresent = {
                onMarkPresentForDate(editingDateInMillis!!)
            },
            onMarkAbsent = { reason, days ->
                onRecordAbsenceForDate(editingDateInMillis!!, reason, days)
            }
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    val prev = (calendarState.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                    calendarState = prev
                }) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Previous Month")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = monthTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = {
                    val next = (calendarState.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                    calendarState = next
                }) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = "Next Month")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val totalCells = daysInMonth + firstDayOfWeek
            val rows = (totalCells + 6) / 7

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (row in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (col in 0..6) {
                            val cellIndex = row * 7 + col
                            val dayNumber = cellIndex - firstDayOfWeek + 1

                            if (cellIndex < firstDayOfWeek || dayNumber > daysInMonth) {
                                Spacer(modifier = Modifier.weight(1f))
                            } else {
                                val isAbsent = isDayAbsent(dayNumber, currentMonth, currentYear, absenceList)
                                val isFuture = if (isCurrentMonth) dayNumber > todayDay else calendarState.after(todayCal)
                                val isPresent = !isAbsent && !isFuture

                                val bgColor = when {
                                    isAbsent -> Color(0xFFF44336)
                                    isPresent -> Color(0xFF4CAF50)
                                    else -> MaterialTheme.colorScheme.surface
                                }

                                val textColor = when {
                                    isAbsent || isPresent -> Color.White
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp)
                                        .background(bgColor, shape = CircleShape)
                                        .then(
                                            if (!isFuture) {
                                                Modifier.clickable {
                                                    val cellCal = (calendarState.clone() as Calendar).apply {
                                                        set(Calendar.DAY_OF_MONTH, dayNumber)
                                                        set(Calendar.HOUR_OF_DAY, 12)
                                                        set(Calendar.MINUTE, 0)
                                                        set(Calendar.SECOND, 0)
                                                        set(Calendar.MILLISECOND, 0)
                                                    }
                                                    editingDateInMillis = cellCal.timeInMillis
                                                }
                                            } else Modifier
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$dayNumber",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isAbsent || isPresent) FontWeight.Bold else FontWeight.Normal,
                                        color = textColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = Color(0xFF4CAF50), label = "Present")
                LegendItem(color = Color(0xFFF44336), label = "Absent")
               // LegendItem(color = MaterialTheme.colorScheme.surface, label = "Future")
            }
        }
    }
}

@Composable
fun EditAttendanceDialog(
    dateInMillis: Long,
    absenceList: List<Attendance>,
    onDismiss: () -> Unit,
    onMarkPresent: () -> Unit,
    onMarkAbsent: (String, Int) -> Unit
) {
    val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(dateInMillis))

    val existingRecord = findAbsenceRecordForDate(dateInMillis, absenceList)
    val isCurrentlyAbsent = existingRecord != null

    var selectedStatusIsPresent by remember { mutableStateOf(!isCurrentlyAbsent) }
    var reasonText by remember { mutableStateOf(existingRecord?.reason ?: "") }
    var daysText by remember { mutableStateOf(existingRecord?.daysCount?.toString() ?: "1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Edit Attendance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedStatusIsPresent,
                        onClick = { selectedStatusIsPresent = true },
                        label = { Text("Present") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF4CAF50),
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = !selectedStatusIsPresent,
                        onClick = { selectedStatusIsPresent = false },
                        label = { Text("Absent") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFF44336),
                            selectedLabelColor = Color.White
                        )
                    )
                }

                if (!selectedStatusIsPresent) {
                    OutlinedTextField(
                        value = reasonText,
                        onValueChange = { reasonText = it },
                        label = { Text("Reason for Absence") },
                        placeholder = { Text("e.g. Sick Leave, Personal") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = daysText,
                        onValueChange = { daysText = it },
                        label = { Text("Number of Days") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedStatusIsPresent) {
                        onMarkPresent()
                    } else {
                        val days = daysText.toIntOrNull()?.coerceAtLeast(1) ?: 1
                        val reason = reasonText.trim().ifEmpty { "Absent" }
                        onMarkAbsent(reason, days)
                    }
                    onDismiss()
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun findAbsenceRecordForDate(dateInMillis: Long, absenceList: List<Attendance>): Attendance? {
    val calTarget = Calendar.getInstance().apply { timeInMillis = dateInMillis }
    val targetYear = calTarget.get(Calendar.YEAR)
    val targetDay = calTarget.get(Calendar.DAY_OF_YEAR)

    val calRecord = Calendar.getInstance()
    return absenceList.find { record ->
        calRecord.timeInMillis = record.date
        val recordYear = calRecord.get(Calendar.YEAR)
        val recordStartDay = calRecord.get(Calendar.DAY_OF_YEAR)
        if (recordYear == targetYear) {
            val recordEndDay = recordStartDay + record.daysCount - 1
            targetDay in recordStartDay..recordEndDay
        } else false
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}

fun isDayAbsent(day: Int, month: Int, year: Int, absenceList: List<Attendance>): Boolean {
    val cal = Calendar.getInstance()
    return absenceList.any { record ->
        cal.timeInMillis = record.date
        val recordMonth = cal.get(Calendar.MONTH)
        val recordYear = cal.get(Calendar.YEAR)
        if (recordMonth == month && recordYear == year) {
            val startDay = cal.get(Calendar.DAY_OF_MONTH)
            val endDay = startDay + record.daysCount - 1
            day in startDay..endDay
        } else false
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
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

@Composable
fun AdvanceRecordItem(
    record: Advance,
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
                        text = "₹${String.format(Locale.getDefault(), "%.2f", record.amount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Notes: ${record.notes.ifEmpty { "None" }}",
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

@Preview(name = "Light Mode", showBackground = true, showSystemUi = true)
@Preview(name = "Dark Mode", showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
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
                )
            ),
            advanceList = listOf(
                Advance(
                    id = 1L,
                    staffId = 1L,
                    amount = 2000.0,
                    date = System.currentTimeMillis(),
                    notes = "Festival Advance"
                )
            ),
            monthlyStats = MonthlyStats(
                daysPresent = 15,
                daysAbsent = 3,
                totalSalary = 1800.0,
                totalAdvance = 2000.0,
                netSalary = 0.0
            ),
            monthlySalaryRecords = listOf(
                MonthlySalaryRecord(
                    year = 2025,
                    month = 1,
                    monthName = "February 2025",
                    daysInMonth = 28,
                    daysWorked = 25,
                    daysAbsent = 3,
                    grossSalary = 25000.0,
                    advanceDeducted = 2000.0,
                    carryForwardFromPrevious = 5000.0,
                    netPayable = 28000.0,
                    amountPaid = 0.0,
                    isPaid = false
                ),
                MonthlySalaryRecord(
                    year = 2025,
                    month = 0,
                    monthName = "January 2025",
                    daysInMonth = 31,
                    daysWorked = 30,
                    daysAbsent = 1,
                    grossSalary = 30000.0,
                    advanceDeducted = 0.0,
                    carryForwardFromPrevious = 0.0,
                    netPayable = 30000.0,
                    amountPaid = 30000.0,
                    isPaid = true,
                    paidDate = System.currentTimeMillis()
                )
            ),
            absentReason = "Family function",
            onAbsentReasonChange = {},
            absentDaysInput = "1",
            onAbsentDaysInputChange = {},
            selectedDate = System.currentTimeMillis(),
            onSelectedDateChange = {},
            onRecordAbsence = {},
            onMarkPresentForDate = {},
            onRecordAbsenceForDate = { _, _, _ -> },
            onDeleteAbsence = {},
            advanceAmountInput = "2000",
            onAdvanceAmountInputChange = {},
            advanceNotesInput = "Festival Advance",
            onAdvanceNotesInputChange = {},
            selectedAdvanceDate = System.currentTimeMillis(),
            onSelectedAdvanceDateChange = {},
            onRecordAdvance = {},
            onDeleteAdvance = {},
            onMarkSalaryPaid = { _, _, _ -> },
            onMarkSalaryUnpaid = {},
            onBack = {}
        )
    }
}
