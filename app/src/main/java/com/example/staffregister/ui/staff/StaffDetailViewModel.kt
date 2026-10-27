package com.example.staffregister.ui.staff

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.staffregister.data.Staff
import com.example.staffregister.data.StaffRepository
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class StaffDetailViewModel(
    private val repository: StaffRepository,
    private val staffId: Long?
) : ViewModel() {

    var name by mutableStateOf("")
    var role by mutableStateOf("")
    var contact by mutableStateOf("")
    var credentials by mutableStateOf("")
    var photoUri by mutableStateOf<String?>(null)
    var dailyRate by mutableStateOf("")
    var monthlyRate by mutableStateOf("")
    var joiningDate by mutableStateOf(System.currentTimeMillis())

    fun updatePhoto(uri: Uri) {
        photoUri = uri.toString()
    }

    init {
        staffId?.let { id ->
            viewModelScope.launch {
                repository.getStaffById(id).filterNotNull().collect { staff ->
                    name = staff.name
                    role = staff.role
                    contact = staff.contact
                    credentials = staff.credentials
                    photoUri = staff.photoUri
                    monthlyRate = staff.monthlyRate.toString()
                    joiningDate = staff.joiningDate
                }
            }
        }
    }

    fun saveStaff(onSuccess: () -> Unit) {
        val rate = monthlyRate.toDoubleOrNull() ?: 0.0
        val computedDailyRate = if (rate > 0) rate / 30.0 else 0.0
        val staff = Staff(
            id = staffId ?: 0,
            name = name,
            role = role,
            contact = contact,
            credentials = credentials,
            photoUri = photoUri,
            dailyRate = computedDailyRate,
            monthlyRate = rate,
            joiningDate = joiningDate
        )
        viewModelScope.launch {
            if (staffId == null) {
                repository.insertStaff(staff)
            } else {
                repository.updateStaff(staff)
            }
            onSuccess()
        }
    }
}
