package com.earnergy.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<NotificationEventEntity>)

    @Query("SELECT * FROM notification_events WHERE dateEpochDay = :dateEpochDay ORDER BY timestamp ASC")
    suspend fun getForDay(dateEpochDay: Long): List<NotificationEventEntity>

    @Query("SELECT * FROM notification_events WHERE dateEpochDay = :dateEpochDay ORDER BY timestamp ASC")
    fun observeForDay(dateEpochDay: Long): Flow<List<NotificationEventEntity>>

    @Query("DELETE FROM notification_events WHERE dateEpochDay = :dateEpochDay")
    suspend fun deleteForDay(dateEpochDay: Long)

    @Query("DELETE FROM notification_events WHERE dateEpochDay < :dateEpochDay")
    suspend fun deleteOlderThan(dateEpochDay: Long)
}
