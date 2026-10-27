package com.example.staffregister.ui.staff

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.staffregister.data.Advance
import com.example.staffregister.data.Attendance
import com.example.staffregister.data.MonthlySalaryRecord
import com.example.staffregister.data.MonthlyStats
import com.example.staffregister.data.Staff
import com.example.staffregister.data.StaffRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class AttendanceViewModel(
    private val repository: StaffRepository,
    private val staffId: Long
) : ViewModel() {

    val staff: StateFlow<Staff?> = repository.getStaffById(staffId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val absenceList: StateFlow<List<Attendance>> = repository.getAttendanceForStaff(staffId)
        .map { list -> list.filter { !it.isPresent } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val advanceList: StateFlow<List<Advance>> = repository.getAdvanceForStaff(staffId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlyStats: StateFlow<MonthlyStats?> = staff.flatMapLatest { s ->
        if (s == null) flowOf(null)
        else {
            val rate = if (s.dailyRate > 0) s.dailyRate else if (s.monthlyRate > 0) s.monthlyRate / 30.0 else 0.0
            repository.getMonthlyStats(staffId, rate)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val monthlySalaryRecords: StateFlow<List<MonthlySalaryRecord>> = repository.getMonthlySalaryRecords(staffId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var absentReason by mutableStateOf("")
    var absentDaysInput by mutableStateOf("1")
    var selectedDate by mutableStateOf(System.currentTimeMillis())

    var advanceAmountInput by mutableStateOf("")
    var advanceNotesInput by mutableStateOf("")
    var selectedAdvanceDate by mutableStateOf(System.currentTimeMillis())

    fun recordAbsence() {
        val reason = absentReason.trim().ifEmpty { "Absent" }
        val days = absentDaysInput.toIntOrNull()?.coerceAtLeast(1) ?: 1
        viewModelScope.launch {
            repository.recordAbsence(staffId, reason, days, selectedDate)
            absentReason = ""
            absentDaysInput = "1"
            selectedDate = System.currentTimeMillis()
        }
    }

    fun recordAdvance() {
        val amount = advanceAmountInput.toDoubleOrNull() ?: return
        if (amount <= 0) return
        val notes = advanceNotesInput.trim()
        viewModelScope.launch {
            repository.recordAdvance(staffId, amount, selectedAdvanceDate, notes)
            advanceAmountInput = ""
            advanceNotesInput = ""
            selectedAdvanceDate = System.currentTimeMillis()
        }
    }

    fun markPresentForDate(dateInMillis: Long) {
        viewModelScope.launch {
            repository.markPresentForDate(staffId, dateInMillis)
        }
    }

    fun recordAbsenceForDate(dateInMillis: Long, reason: String, daysCount: Int = 1) {
        val r = reason.trim().ifEmpty { "Absent" }
        viewModelScope.launch {
            repository.markPresentForDate(staffId, dateInMillis)
            repository.recordAbsence(staffId, r, daysCount, dateInMillis)
        }
    }

    fun deleteAbsence(attendance: Attendance) {
        viewModelScope.launch {
            repository.deleteAttendance(attendance)
        }
    }

    fun deleteAdvance(advance: Advance) {
        viewModelScope.launch {
            repository.deleteAdvance(advance)
        }
    }

    fun markSalaryPaid(record: MonthlySalaryRecord, amountPaid: Double, notes: String = "") {
        viewModelScope.launch {
            repository.markSalaryPaid(staffId, record.year, record.month, amountPaid, notes)
        }
    }

    fun markSalaryUnpaid(record: MonthlySalaryRecord) {
        viewModelScope.launch {
            repository.markSalaryUnpaid(staffId, record.year, record.month)
        }
    }
}
