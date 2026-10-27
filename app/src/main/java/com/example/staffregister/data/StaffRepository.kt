package com.example.staffregister.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
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

    suspend fun markPresentForDate(staffId: Long, date: Long) {
        val targetCal = Calendar.getInstance().apply { timeInMillis = date }
        val targetYear = targetCal.get(Calendar.YEAR)
        val targetDayOfYear = targetCal.get(Calendar.DAY_OF_YEAR)

        val records = attendanceDao.getAttendanceListForStaff(staffId)
        val recordCal = Calendar.getInstance()

        for (record in records) {
            if (!record.isPresent) {
                recordCal.timeInMillis = record.date
                val recordYear = recordCal.get(Calendar.YEAR)
                val recordStartDay = recordCal.get(Calendar.DAY_OF_YEAR)

                if (recordYear == targetYear) {
                    val recordEndDay = recordStartDay + record.daysCount - 1
                    if (targetDayOfYear in recordStartDay..recordEndDay) {
                        attendanceDao.deleteAttendance(record)
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

            val joinCal = Calendar.getInstance().apply {
                timeInMillis = if (staff.joiningDate > 0) staff.joiningDate else System.currentTimeMillis()
            }
            var startYear = joinCal.get(Calendar.YEAR)
            var startMonth = joinCal.get(Calendar.MONTH)

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

            while (iterYear < currentYear || (iterYear == currentYear && iterMonth <= currentMonth)) {
                calIter.set(Calendar.YEAR, iterYear)
                calIter.set(Calendar.MONTH, iterMonth)
                calIter.set(Calendar.DAY_OF_MONTH, 1)

                val daysInMonth = calIter.getActualMaximum(Calendar.DAY_OF_MONTH)
                val isCurrentMonth = (iterYear == currentYear && iterMonth == currentMonth)

                val absentRecords = attendanceList.filter { att ->
                    !att.isPresent && tempCal.apply { timeInMillis = att.date }.run {
                        get(Calendar.YEAR) == iterYear && get(Calendar.MONTH) == iterMonth
                    }
                }
                val daysAbsent = absentRecords.sumOf { it.daysCount }

                val elapsedDays = if (isCurrentMonth) today else daysInMonth
                val daysWorked = maxOf(0, elapsedDays - daysAbsent)

                val dailyRate = if (staff.dailyRate > 0) staff.dailyRate
                                else if (staff.monthlyRate > 0) staff.monthlyRate / 30.0
                                else 0.0
                val dailyrate1=staff.monthlyRate/30.0

                val grossSalary = daysWorked * dailyRate
                val grossSalary1 = if (daysAbsent == 0)staff.monthlyRate
                                    else if(daysAbsent > 0)(staff.monthlyRate - daysAbsent*dailyrate1)
                                        else 0.0

                val advanceRecords = advanceList.filter { adv ->
                    tempCal.apply { timeInMillis = adv.date }.run {
                        get(Calendar.YEAR) == iterYear && get(Calendar.MONTH) == iterMonth
                    }
                }
                val advanceDeducted = advanceRecords.sumOf { it.amount }

                val carryForward = accumulatedCarryForward
                val netPayable = grossSalary1 - advanceDeducted + carryForward

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
                        grossSalary = grossSalary1,
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
        return combine(
            attendanceDao.getAttendanceForStaff(staffId),
            advanceDao.getAdvanceForStaff(staffId)
        ) { attendanceList, advanceList ->
            val calendar = Calendar.getInstance()
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentYear = calendar.get(Calendar.YEAR)
            val today = calendar.get(Calendar.DAY_OF_MONTH)

            val thisMonthAbsentRecords = attendanceList.filter {
                calendar.timeInMillis = it.date
                !it.isPresent &&
                        calendar.get(Calendar.MONTH) == currentMonth &&
                        calendar.get(Calendar.YEAR) == currentYear
            }

            val thisMonthAdvanceRecords = advanceList.filter {
                calendar.timeInMillis = it.date
                calendar.get(Calendar.MONTH) == currentMonth &&
                        calendar.get(Calendar.YEAR) == currentYear
            }

            val daysAbsent = thisMonthAbsentRecords.sumOf { it.daysCount }
            val daysPresent = maxOf(0, today - daysAbsent)

            val totalSalary = daysPresent * dailyRate

          //  val totalSalary1 =

            val totalAdvance = thisMonthAdvanceRecords.sumOf { it.amount }

            val netSalary = maxOf(0.0, totalSalary - totalAdvance)

             val netSalary1 =  totalSalary - totalAdvance

            MonthlyStats(
                daysPresent = daysPresent,
                daysAbsent = daysAbsent,
                totalSalary = totalSalary,
                totalAdvance = totalAdvance,
                netSalary = netSalary1
            )
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
