package com.example.staffregister.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Staff::class, Attendance::class, Advance::class, SalaryPayment::class],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun staffDao(): StaffDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun advanceDao(): AdvanceDao
    abstract fun salaryPaymentDao(): SalaryPaymentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE staff ADD COLUMN isActive INTEGER NOT NULL DEFAULT 1")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "staff_register_database"
                )
                .addMigrations(MIGRATION_7_8)
                .fallbackToDestructiveMigration()
                .build().also { INSTANCE = it }
            }
        }
    }
}
