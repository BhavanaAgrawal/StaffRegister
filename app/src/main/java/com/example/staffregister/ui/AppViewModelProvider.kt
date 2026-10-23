package com.example.staffregister.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.staffregister.StaffApplication
import com.example.staffregister.ui.staff.AttendanceViewModel
import com.example.staffregister.ui.staff.StaffDetailViewModel
import com.example.staffregister.ui.staff.StaffListViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            StaffListViewModel(staffApplication().container.repository)
        }
    }

    fun detailFactory(staffId: Long?) = viewModelFactory {
        initializer {
            StaffDetailViewModel(staffApplication().container.repository, staffId)
        }
    }

    fun attendanceFactory(staffId: Long) = viewModelFactory {
        initializer {
            AttendanceViewModel(staffApplication().container.repository, staffId)
        }
    }
}

fun CreationExtras.staffApplication(): StaffApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as StaffApplication)
