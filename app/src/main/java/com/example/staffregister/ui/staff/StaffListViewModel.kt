package com.example.staffregister.ui.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.staffregister.data.Staff
import com.example.staffregister.data.StaffRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.map

class StaffListViewModel(private val repository: StaffRepository) : ViewModel() {
    val activeStaffList: StateFlow<List<Staff>> = repository.allStaff
        .map { list -> list.filter { it.isActive } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val exStaffList: StateFlow<List<Staff>> = repository.allStaff
        .map { list -> list.filter { !it.isActive } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteStaff(staff: Staff) {
        viewModelScope.launch {
            repository.deleteStaff(staff)
        }
    }
}
