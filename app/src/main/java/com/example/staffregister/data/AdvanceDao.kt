package com.example.staffregister.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AdvanceDao {
    @Query("SELECT * FROM advance WHERE staffId = :staffId ORDER BY date DESC")
    fun getAdvanceForStaff(staffId: Long): Flow<List<Advance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdvance(advance: Advance): Long

    @Delete
    suspend fun deleteAdvance(advance: Advance)
}
