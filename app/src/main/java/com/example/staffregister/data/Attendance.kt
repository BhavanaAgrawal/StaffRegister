package com.example.staffregister.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendance",
    foreignKeys = [
        ForeignKey(
            entity = Staff::class,
            parentColumns = ["id"],
            childColumns = ["staffId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("staffId")]
)
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val staffId: Long,
    val date: Long,
    val isPresent: Boolean = false,
    val reason: String = "",
    val daysCount: Int = 1
)
