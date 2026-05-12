package com.earnergy.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UnlockEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<UnlockEventEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: UnlockEventEntity)

    @Query("SELECT * FROM unlock_events WHERE dateEpochDay = :dateEpochDay ORDER BY timestamp ASC")
    suspend fun getForDay(dateEpochDay: Long): List<UnlockEventEntity>

    @Query("SELECT * FROM unlock_events WHERE dateEpochDay = :dateEpochDay ORDER BY timestamp ASC")
    fun observeForDay(dateEpochDay: Long): Flow<List<UnlockEventEntity>>

    @Query("DELETE FROM unlock_events WHERE dateEpochDay = :dateEpochDay")
    suspend fun deleteForDay(dateEpochDay: Long)

    @Query("DELETE FROM unlock_events WHERE dateEpochDay < :dateEpochDay")
    suspend fun deleteOlderThan(dateEpochDay: Long)
}
