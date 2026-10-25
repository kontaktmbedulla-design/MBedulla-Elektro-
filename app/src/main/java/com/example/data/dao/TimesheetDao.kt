package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.TimesheetLog
import kotlinx.coroutines.flow.Flow

@Dao
interface TimesheetDao {
    @Query("SELECT * FROM timesheets ORDER BY date DESC, id DESC")
    fun getAllTimesheets(): Flow<List<TimesheetLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimesheets(entries: List<TimesheetLog>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimesheet(entry: TimesheetLog)

    @Query("DELETE FROM timesheets WHERE id = :id")
    suspend fun deleteTimesheet(id: Long)
}
