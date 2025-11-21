package com.earnergy.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.earnergy.domain.model.AppRole

@Entity(tableName = "app_config")
data class AppConfigEntity(
    @PrimaryKey val packageName: String,
    val role: AppRole
)
