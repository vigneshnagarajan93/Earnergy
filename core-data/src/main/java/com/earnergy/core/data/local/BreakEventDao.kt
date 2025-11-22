package com.earnergy.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO for accessing break event data.
 */
@Dao
interface BreakEventDao {
    /**
     * Observe all break events for a specific date.
     */
    @Query("SELECT * FROM break_events WHERE dateEpochDay = :epochDay ORDER BY timestamp ASC")
    fun observeForDay(epochDay: Long): Flow<List<BreakEventEntity>>
    
    /**
     * Get all break events for a specific date.
     */
    @Query("SELECT * FROM break_events WHERE dateEpochDay = :epochDay ORDER BY timestamp ASC")
    suspend fun getForDay(epochDay: Long): List<BreakEventEntity>
    
    /**
     * Get break events for a date range.
     */
    @Query("""
        SELECT * FROM break_events 
        WHERE dateEpochDay >= :startEpochDay AND dateEpochDay <= :endEpochDay
        ORDER BY timestamp ASC
    """)
    suspend fun getForDateRange(startEpochDay: Long, endEpochDay: Long): List<BreakEventEntity>
    
    /**
     * Insert a single break event.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: BreakEventEntity)
    
    /**
     * Get the most recent break event.
     */
    @Query("SELECT * FROM break_events ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastBreak(): BreakEventEntity?
    
    /**
     * Get total break time for a specific date (in seconds).
     */
    @Query("SELECT SUM(durationSeconds) FROM break_events WHERE dateEpochDay = :epochDay")
    suspend fun getTotalBreakSecondsForDay(epochDay: Long): Int?
    
    /**
     * Get count of breaks for a specific date.
     */
    @Query("SELECT COUNT(*) FROM break_events WHERE dateEpochDay = :epochDay")
    suspend fun getBreakCountForDay(epochDay: Long): Int
    
    /**
     * Delete break events older than a specific date.
     */
    @Query("DELETE FROM break_events WHERE dateEpochDay < :epochDay")
    suspend fun deleteOlderThan(epochDay: Long)
    
    /**
     * Delete all break events.
     */
    @Query("DELETE FROM break_events")
    suspend fun deleteAll()
}
