package com.earnergy.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.earnergy.domain.model.AppCategory

@Entity(tableName = "app_usage")
data class AppUsageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochDay: Long,
    val packageName: String,
    val displayName: String,
    val category: AppCategory,
    val totalSeconds: Long,
    val isSystemApp: Boolean
)
