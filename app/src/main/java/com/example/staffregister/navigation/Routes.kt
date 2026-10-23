package com.example.staffregister.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {
    @Serializable
    data object StaffList : Route

    @Serializable
    data class StaffDetail(val staffId: Long? = null) : Route

    @Serializable
    data class Attendance(val staffId: Long) : Route
}
