package com.example.staffregister.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "salary_payment",
    foreignKeys = [
        ForeignKey(
            entity = Staff::class,
            parentColumns = ["id"],
            childColumns = ["staffId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["staffId", "year", "month"], unique = true)]
)
data class SalaryPayment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val staffId: Long,
    val year: Int,
    val month: Int,
    val amountPaid: Double = 0.0,
    val isPaid: Boolean = false,
    val paidDate: Long? = null,
    val notes: String = ""
)
