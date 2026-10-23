package com.example.staffregister

import android.app.Application
import com.example.staffregister.data.AppDatabase
import com.example.staffregister.data.StaffRepository

class StaffApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}

interface AppContainer {
    val repository: StaffRepository
}

class AppDataContainer(private val context: android.content.Context) : AppContainer {
    override val repository: StaffRepository by lazy {
        val db = AppDatabase.getDatabase(context)
        StaffRepository(db.staffDao(), db.attendanceDao())
    }
}



