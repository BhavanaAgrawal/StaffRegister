package com.example.staffregister.ui.staff

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.staffregister.data.Attendance
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

    val monthlyStats: StateFlow<MonthlyStats?> = staff.flatMapLatest { s ->
        if (s == null) flowOf(null)
        else repository.getMonthlyStats(staffId, s.dailyRate)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    var absentReason by mutableStateOf("")
    var absentDaysInput by mutableStateOf("1")

    fun recordAbsence() {
        val reason = absentReason.trim().ifEmpty { "Absent" }
        val days = absentDaysInput.toIntOrNull()?.coerceAtLeast(1) ?: 1
        viewModelScope.launch {
            repository.recordAbsence(staffId, reason, days)
            absentReason = ""
            absentDaysInput = "1"
        }
    }

    fun deleteAbsence(attendance: Attendance) {
        viewModelScope.launch {
            repository.deleteAttendance(attendance)
        }
    }
}
