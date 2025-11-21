package com.earnergy.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppConfigDao {
    @Query("SELECT * FROM app_config")
    fun observeAll(): Flow<List<AppConfigEntity>>

    @Query("SELECT * FROM app_config WHERE packageName = :packageName")
    suspend fun get(packageName: String): AppConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(config: AppConfigEntity)
}
