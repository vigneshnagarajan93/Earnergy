package com.earnergy.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

import kotlinx.coroutines.flow.Flow

/**
 * DAO for accessing app switch event data.
 */
@Dao
interface AppSwitchEventDao {
    /**
     * Observe all app switch events for a specific date.
     */
    @Query("SELECT * FROM app_switch_events WHERE dateEpochDay = :epochDay ORDER BY timestamp ASC")
    fun observeForDay(epochDay: Long): Flow<List<AppSwitchEventEntity>>

    /**
     * Get all app switch events for a specific date.
     */
    @Query("SELECT * FROM app_switch_events WHERE dateEpochDay = :epochDay ORDER BY timestamp ASC")
    suspend fun getForDay(epochDay: Long): List<AppSwitchEventEntity>
    
    /**
     * Get app switch events for a date range.
     */
    @Query("""
        SELECT * FROM app_switch_events 
        WHERE dateEpochDay >= :startEpochDay AND dateEpochDay <= :endEpochDay
        ORDER BY timestamp ASC
    """)
    suspend fun getForDateRange(startEpochDay: Long, endEpochDay: Long): List<AppSwitchEventEntity>
    
    /**
     * Insert a single app switch event.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: AppSwitchEventEntity)
    
    /**
     * Insert multiple app switch events.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<AppSwitchEventEntity>)
    
    /**
     * Delete app switch events older than a specific date.
     * Used to clean up old data and save storage.
     */
    @Query("DELETE FROM app_switch_events WHERE dateEpochDay < :epochDay")
    suspend fun deleteOlderThan(epochDay: Long)
    
    /**
     * Get count of app switches for a specific date.
     */
    @Query("SELECT COUNT(*) FROM app_switch_events WHERE dateEpochDay = :epochDay")
    suspend fun getCountForDay(epochDay: Long): Int
    
    /**
     * Get app switch events for a specific hour of a day.
     * Used for peak productivity hour analysis.
     */
    @Query("""
        SELECT * FROM app_switch_events 
        WHERE dateEpochDay = :epochDay 
        AND timestamp >= :hourStartMillis 
        AND timestamp < :hourEndMillis
        ORDER BY timestamp ASC
    """)
    suspend fun getForHour(epochDay: Long, hourStartMillis: Long, hourEndMillis: Long): List<AppSwitchEventEntity>
    
    /**
     * Delete all app switch events.
     */
    @Query("DELETE FROM app_switch_events")
    suspend fun deleteAll()
}
