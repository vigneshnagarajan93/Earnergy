package com.earnergy.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface AppUsageDao {
    @Query("SELECT * FROM app_usage WHERE dateEpochDay = :epochDay ORDER BY totalSeconds DESC")
    fun observeForDay(epochDay: Long): Flow<List<AppUsageEntity>>

    @Query("SELECT * FROM app_usage WHERE dateEpochDay = :epochDay")
    suspend fun getForDay(epochDay: Long): List<AppUsageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(usages: List<AppUsageEntity>)

    @Query("DELETE FROM app_usage WHERE dateEpochDay = :epochDay")
    suspend fun deleteForDay(epochDay: Long)

    @Transaction
    suspend fun replaceForDay(epochDay: Long, usages: List<AppUsageEntity>) {
        deleteForDay(epochDay)
        insertAll(usages)
    }
}
