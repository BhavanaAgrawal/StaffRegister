package com.example.staffregister.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

class StaffRepository(
    private val staffDao: StaffDao,
    private val attendanceDao: AttendanceDao
) {
    val allStaff: Flow<List<Staff>> = staffDao.getAllStaff()

    fun getStaffById(id: Long): Flow<Staff?> = staffDao.getStaffById(id)

    suspend fun insertStaff(staff: Staff) = staffDao.insertStaff(staff)

    suspend fun updateStaff(staff: Staff) = staffDao.updateStaff(staff)

    suspend fun deleteStaff(staff: Staff) = staffDao.deleteStaff(staff)

    fun getAttendanceForStaff(staffId: Long): Flow<List<Attendance>> = 
        attendanceDao.getAttendanceForStaff(staffId)

    suspend fun getAttendanceForToday(staffId: Long): Attendance? {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.timeInMillis
        
        return attendanceDao.getAttendanceForToday(staffId, startOfDay, endOfDay)
    }

    suspend fun recordAbsence(staffId: Long, reason: String, daysCount: Int) {
        attendanceDao.insertAttendance(
            Attendance(
                staffId = staffId,
                date = System.currentTimeMillis(),
                isPresent = false,
                reason = reason,
                daysCount = daysCount
            )
        )
    }

    suspend fun deleteAttendance(attendance: Attendance) {
        attendanceDao.deleteAttendance(attendance)
    }

    fun getMonthlyStats(staffId: Long, dailyRate: Double): Flow<MonthlyStats> {
        return attendanceDao.getAttendanceForStaff(staffId).map { list ->
            val calendar = Calendar.getInstance()
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentYear = calendar.get(Calendar.YEAR)
            val today = calendar.get(Calendar.DAY_OF_MONTH)

            // Filter for current month's absent records
            val thisMonthAbsentRecords = list.filter {
                calendar.timeInMillis = it.date
                !it.isPresent &&
                        calendar.get(Calendar.MONTH) == currentMonth &&
                        calendar.get(Calendar.YEAR) == currentYear
            }

            val daysAbsent = thisMonthAbsentRecords.sumOf { it.daysCount }
            val daysPresent = maxOf(0, today - daysAbsent)
            val totalSalary = daysPresent * dailyRate

            MonthlyStats(
                daysPresent = daysPresent,
                daysAbsent = daysAbsent,
                totalSalary = totalSalary
            )
        }
    }
}

data class MonthlyStats(
    val daysPresent: Int,
    val daysAbsent: Int,
    val totalSalary: Double
)
