package com.example.staffregister.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class StaffRepository(
    private val staffDao: StaffDao,
    private val attendanceDao: AttendanceDao,
    private val advanceDao: AdvanceDao,
    private val salaryPaymentDao: SalaryPaymentDao
) {
    val allStaff: Flow<List<Staff>> = staffDao.getAllStaff()

    fun getStaffById(id: Long): Flow<Staff?> = staffDao.getStaffById(id)

    suspend fun insertStaff(staff: Staff) = staffDao.insertStaff(staff)

    suspend fun updateStaff(staff: Staff) = staffDao.updateStaff(staff)

    suspend fun deleteStaff(staff: Staff) = staffDao.deleteStaff(staff)

    fun getAttendanceForStaff(staffId: Long): Flow<List<Attendance>> = 
        attendanceDao.getAttendanceForStaff(staffId)

    fun getAdvanceForStaff(staffId: Long): Flow<List<Advance>> =
        advanceDao.getAdvanceForStaff(staffId)

    suspend fun recordAdvance(staffId: Long, amount: Double, date: Long, notes: String = "") {
        advanceDao.insertAdvance(
            Advance(
                staffId = staffId,
                amount = amount,
                date = date,
                notes = notes
            )
        )
    }

    suspend fun deleteAdvance(advance: Advance) {
        advanceDao.deleteAdvance(advance)
    }

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

    suspend fun recordAbsence(staffId: Long, reason: String, daysCount: Int, date: Long = System.currentTimeMillis()) {
        attendanceDao.insertAttendance(
            Attendance(
                staffId = staffId,
                date = date,
                isPresent = false,
                reason = reason,
                daysCount = daysCount
            )
        )
    }

    suspend fun markPresentForDate(staffId: Long, date: Long, daysCount: Int = 1) {
        val targetCal = Calendar.getInstance()
        
        for (dayOffset in 0 until daysCount) {
            targetCal.timeInMillis = date
            targetCal.add(Calendar.DAY_OF_MONTH, dayOffset)
            val targetYear = targetCal.get(Calendar.YEAR)
            val targetDayOfYear = targetCal.get(Calendar.DAY_OF_YEAR)

            val records = attendanceDao.getAttendanceListForStaff(staffId)
            val recordCal = Calendar.getInstance()

            for (record in records) {
                if (!record.isPresent) {
                    var targetOffset = -1
                    for (i in 0 until record.daysCount) {
                        recordCal.timeInMillis = record.date
                        recordCal.add(Calendar.DAY_OF_MONTH, i)
                        
                        if (recordCal.get(Calendar.YEAR) == targetYear && 
                            recordCal.get(Calendar.DAY_OF_YEAR) == targetDayOfYear) {
                            targetOffset = i
                            break
                        }
                    }
                    
                    if (targetOffset != -1) {
                        attendanceDao.deleteAttendance(record)
                        
                        if (targetOffset > 0) {
                            attendanceDao.insertAttendance(
                                record.copy(
                                    id = 0,
                                    daysCount = targetOffset
                                )
                            )
                        }
                        if (targetOffset < record.daysCount - 1) {
                            recordCal.timeInMillis = record.date
                            recordCal.add(Calendar.DAY_OF_MONTH, targetOffset + 1)
                            attendanceDao.insertAttendance(
                                record.copy(
                                    id = 0,
                                    date = recordCal.timeInMillis,
                                    daysCount = record.daysCount - targetOffset - 1
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    suspend fun deleteAttendance(attendance: Attendance) {
        attendanceDao.deleteAttendance(attendance)
    }

    fun getSalaryPaymentsForStaff(staffId: Long): Flow<List<SalaryPayment>> =
        salaryPaymentDao.getPaymentsForStaff(staffId)

    suspend fun markSalaryPaid(staffId: Long, year: Int, month: Int, amountPaid: Double, notes: String = "") {
        val existing = salaryPaymentDao.getPaymentForMonth(staffId, year, month)
        val payment = existing?.copy(
            amountPaid = amountPaid,
            isPaid = true,
            paidDate = System.currentTimeMillis(),
            notes = notes
        ) ?: SalaryPayment(
            staffId = staffId,
            year = year,
            month = month,
            amountPaid = amountPaid,
            isPaid = true,
            paidDate = System.currentTimeMillis(),
            notes = notes
        )
        salaryPaymentDao.insertOrUpdatePayment(payment)
    }

    suspend fun markSalaryUnpaid(staffId: Long, year: Int, month: Int) {
        val existing = salaryPaymentDao.getPaymentForMonth(staffId, year, month)
        if (existing != null) {
            val payment = existing.copy(
                isPaid = false,
                amountPaid = 0.0,
                paidDate = null
            )
            salaryPaymentDao.insertOrUpdatePayment(payment)
        }
    }

    fun getMonthlySalaryRecords(staffId: Long): Flow<List<MonthlySalaryRecord>> {
        return combine(
            staffDao.getStaffById(staffId),
            attendanceDao.getAttendanceForStaff(staffId),
            advanceDao.getAdvanceForStaff(staffId),
            salaryPaymentDao.getPaymentsForStaff(staffId)
        ) { staff, attendanceList, advanceList, paymentList ->
            if (staff == null) return@combine emptyList()

            val records = mutableListOf<MonthlySalaryRecord>()
            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH)
            val today = calendar.get(Calendar.DAY_OF_MONTH)

            val effectiveEndYear = if (!staff.isActive && staff.leavingDate != null) {
                val endCal = Calendar.getInstance().apply { timeInMillis = staff.leavingDate }
                endCal.get(Calendar.YEAR)
            } else currentYear

            val effectiveEndMonth = if (!staff.isActive && staff.leavingDate != null) {
                val endCal = Calendar.getInstance().apply { timeInMillis = staff.leavingDate }
                endCal.get(Calendar.MONTH)
            } else currentMonth

            val joinDate = if (staff.joiningDate > 0) staff.joiningDate else System.currentTimeMillis()
            
            val earliestAdvance = advanceList.minOfOrNull { it.date }
            val earliestAttendance = attendanceList.minOfOrNull { it.date }
            
            val earliestDate = listOfNotNull(
                joinDate,
                earliestAdvance,
                earliestAttendance
            ).minOrNull() ?: joinDate

            val startCal = Calendar.getInstance().apply { timeInMillis = earliestDate }
            var startYear = startCal.get(Calendar.YEAR)
            var startMonth = startCal.get(Calendar.MONTH)

            if (startYear > currentYear || (startYear == currentYear && startMonth > currentMonth)) {
                startYear = currentYear
                startMonth = currentMonth
            }

            val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            val calIter = Calendar.getInstance()

            var accumulatedCarryForward = 0.0

            var iterYear = startYear
            var iterMonth = startMonth

            val tempCal = Calendar.getInstance()

            while (iterYear < effectiveEndYear || (iterYear == effectiveEndYear && iterMonth <= effectiveEndMonth)) {
                calIter.set(Calendar.YEAR, iterYear)
                calIter.set(Calendar.MONTH, iterMonth)
                calIter.set(Calendar.DAY_OF_MONTH, 1)

                val daysInMonth = calIter.getActualMaximum(Calendar.DAY_OF_MONTH)
                val isCurrentMonth = (iterYear == currentYear && iterMonth == currentMonth)
                val isLeavingMonth = (!staff.isActive && staff.leavingDate != null && iterYear == effectiveEndYear && iterMonth == effectiveEndMonth)

                val endDay = if (isLeavingMonth && staff.leavingDate != null) {
                    tempCal.apply { timeInMillis = staff.leavingDate }.get(Calendar.DAY_OF_MONTH)
                } else if (isCurrentMonth) {
                    today
                } else {
                    daysInMonth
                }
                
                val startDayOfMonth = if (iterYear == startYear && iterMonth == startMonth) {
                    tempCal.apply { timeInMillis = joinDate }.get(Calendar.DAY_OF_MONTH)
                } else {
                    1
                }
                
                val uniqueAbsentDaysInMonth = mutableSetOf<Int>()
                for (att in attendanceList) {
                    if (!att.isPresent) {
                        for (i in 0 until att.daysCount) {
                            val absentDayCal = Calendar.getInstance().apply {
                                timeInMillis = att.date
                                add(Calendar.DAY_OF_MONTH, i)
                            }
                            if (absentDayCal.get(Calendar.YEAR) == iterYear && absentDayCal.get(Calendar.MONTH) == iterMonth) {
                                val dayOfMonth = absentDayCal.get(Calendar.DAY_OF_MONTH)
                                if (dayOfMonth in startDayOfMonth..endDay) {
                                    uniqueAbsentDaysInMonth.add(dayOfMonth)
                                }
                            }
                        }
                    }
                }
                val daysAbsent = uniqueAbsentDaysInMonth.size

                val totalDaysInRange = maxOf(0, endDay - startDayOfMonth + 1)
                val daysWorked = maxOf(0, totalDaysInRange - daysAbsent)

                val dailyRate = if (staff.monthlyRate > 0) staff.monthlyRate / 30.0 else 0.0
                
                val calculatedGrossSalary = daysWorked * dailyRate

                val advanceRecords = advanceList.filter { adv ->
                    tempCal.apply { timeInMillis = adv.date }.run {
                        get(Calendar.YEAR) == iterYear && get(Calendar.MONTH) == iterMonth
                    }
                }
                val advanceDeducted = advanceRecords.sumOf { it.amount }

                val carryForward = accumulatedCarryForward
                val netPayable = calculatedGrossSalary - advanceDeducted + carryForward

                val payment = paymentList.find { it.year == iterYear && it.month == iterMonth }
                val isPaid = payment?.isPaid ?: false
                val amountPaid = payment?.amountPaid ?: 0.0
                val paidDate = payment?.paidDate
                val paymentId = payment?.id ?: 0L

                if (isPaid) {
                    val unpaid = netPayable - amountPaid
                    accumulatedCarryForward = unpaid
                } else {
                    accumulatedCarryForward = netPayable
                }

                val monthName = monthYearFormat.format(calIter.time)

                records.add(
                    MonthlySalaryRecord(
                        year = iterYear,
                        month = iterMonth,
                        monthName = monthName,
                        daysInMonth = daysInMonth,
                        daysWorked = daysWorked,
                        daysAbsent = daysAbsent,
                        grossSalary = calculatedGrossSalary,
                        advanceDeducted = advanceDeducted,
                        carryForwardFromPrevious = carryForward,
                        netPayable = netPayable,
                        amountPaid = if (isPaid) amountPaid else 0.0,
                        isPaid = isPaid,
                        paidDate = paidDate,
                        paymentId = paymentId
                    )
                )

                iterMonth++
                if (iterMonth > 11) {
                    iterMonth = 0
                    iterYear++
                }
            }

            records.reversed()
        }
    }

    fun getMonthlyStats(staffId: Long, dailyRate: Double): Flow<MonthlyStats> {
        return getMonthlySalaryRecords(staffId).map { records ->
            val currentRecord = records.firstOrNull()
            if (currentRecord != null) {
                val advanceTaken = if (currentRecord.advanceDeducted > 0) currentRecord.advanceDeducted else 0.0
                val carryForward = if (currentRecord.carryForwardFromPrevious < 0) currentRecord.carryForwardFromPrevious else 0.0
                
                MonthlyStats(
                    daysPresent = currentRecord.daysWorked,
                    daysAbsent = currentRecord.daysAbsent,
                    totalSalary = currentRecord.grossSalary,
                    totalAdvance = advanceTaken - carryForward,
                    netSalary = currentRecord.netPayable
                )
            } else {
                MonthlyStats(
                    daysPresent = 0,
                    daysAbsent = 0,
                    totalSalary = 0.0,
                    totalAdvance = 0.0,
                    netSalary = 0.0
                )
            }
        }
    }
}

data class MonthlyStats(
    val daysPresent: Int,
    val daysAbsent: Int,
    val totalSalary: Double,
    val totalAdvance: Double = 0.0,
    val netSalary: Double = totalSalary
)

data class MonthlySalaryRecord(
    val year: Int,
    val month: Int,
    val monthName: String,
    val daysInMonth: Int,
    val daysWorked: Int,
    val daysAbsent: Int,
    val grossSalary: Double,
    val advanceDeducted: Double,
    val carryForwardFromPrevious: Double,
    val netPayable: Double,
    val amountPaid: Double,
    val isPaid: Boolean,
    val paidDate: Long? = null,
    val paymentId: Long = 0L
)
