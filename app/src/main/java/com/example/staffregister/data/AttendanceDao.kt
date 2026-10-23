package com.example.staffregister.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance WHERE staffId = :staffId ORDER BY date DESC")
    fun getAttendanceForStaff(staffId: Long): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE id = :id")
    suspend fun getAttendanceById(id: Long): Attendance?

    @Query("SELECT * FROM attendance WHERE staffId = :staffId AND date >= :startOfDay AND date <= :endOfDay LIMIT 1")
    suspend fun getAttendanceForToday(staffId: Long, startOfDay: Long, endOfDay: Long): Attendance?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance): Long

    @Update
    suspend fun updateAttendance(attendance: Attendance)

    @Delete
    suspend fun deleteAttendance(attendance: Attendance)
}
