package com.example.staffregister.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "staff")
data class Staff(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val role: String,
    val contact: String,
    val credentials: String,
    val photoUri: String? = null,
    val dailyRate: Double = 0.0,
    val monthlyRate: Double = 0.0,
    val joiningDate: Long = System.currentTimeMillis()
)
