package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.WorkSite
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkSiteDao {
    @Query("SELECT * FROM work_sites ORDER BY name ASC")
    fun getAllSites(): Flow<List<WorkSite>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSites(sites: List<WorkSite>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSite(site: WorkSite)

    @Update
    suspend fun updateSite(site: WorkSite)

    @Query("DELETE FROM work_sites WHERE id = :siteId")
    suspend fun deleteSite(siteId: String)
}
