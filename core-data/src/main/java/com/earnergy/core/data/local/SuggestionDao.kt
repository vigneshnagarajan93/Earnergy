package com.earnergy.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO for accessing suggestion data.
 */
@Dao
interface SuggestionDao {
    /**
     * Observe all active (not dismissed) suggestions ordered by priority/timestamp.
     */
    @Query("""
        SELECT * FROM suggestions 
        WHERE dismissed = 0 
        ORDER BY timestamp DESC
    """)
    fun observeActiveSuggestions(): Flow<List<SuggestionEntity>>
    
    /**
     * Get all suggestions for a specific date.
     */
    @Query("""
        SELECT * FROM suggestions 
        WHERE timestamp >= :startMillis AND timestamp < :endMillis
        ORDER BY timestamp DESC
    """)
    suspend fun getSuggestionsForDateRange(startMillis: Long, endMillis: Long): List<SuggestionEntity>
    
    /**
     * Insert a new suggestion.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(suggestion: SuggestionEntity)
    
    /**
     * Insert multiple suggestions.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(suggestions: List<SuggestionEntity>)
    
    /**
     * Mark a suggestion as dismissed.
     */
    @Query("UPDATE suggestions SET dismissed = 1 WHERE id = :id")
    suspend fun dismiss(id: String)
    
    /**
     * Mark a suggestion as completed (user acted on it).
     */
    @Query("UPDATE suggestions SET completed = 1 WHERE id = :id")
    suspend fun markCompleted(id: String)
    
    /**
     * Delete old suggestions (older than 7 days).
     */
    @Query("DELETE FROM suggestions WHERE timestamp < :cutoffMillis")
    suspend fun deleteOlderThan(cutoffMillis: Long)
    
    /**
     * Delete all suggestions.
     */
    @Query("DELETE FROM suggestions")
    suspend fun deleteAll()
}
