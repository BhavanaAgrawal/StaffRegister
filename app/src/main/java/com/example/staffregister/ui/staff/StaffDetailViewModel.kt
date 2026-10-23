package com.example.staffregister.ui.staff

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

    fun updatePhoto(uri: android.net.Uri) {
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
                    dailyRate = staff.dailyRate.toString()
                }
            }
        }
    }

    fun saveStaff(onSuccess: () -> Unit) {
        val rate = dailyRate.toDoubleOrNull() ?: 0.0
        val staff = Staff(
            id = staffId ?: 0,
            name = name,
            role = role,
            contact = contact,
            credentials = credentials,
            photoUri = photoUri,
            dailyRate = rate
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
