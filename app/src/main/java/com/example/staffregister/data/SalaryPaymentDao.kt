package com.example.staffregister.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SalaryPaymentDao {
    @Query("SELECT * FROM salary_payment WHERE staffId = :staffId ORDER BY year DESC, month DESC")
    fun getPaymentsForStaff(staffId: Long): Flow<List<SalaryPayment>>

    @Query("SELECT * FROM salary_payment WHERE staffId = :staffId AND year = :year AND month = :month LIMIT 1")
    suspend fun getPaymentForMonth(staffId: Long, year: Int, month: Int): SalaryPayment?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePayment(payment: SalaryPayment): Long

    @Delete
    suspend fun deletePayment(payment: SalaryPayment)
}
