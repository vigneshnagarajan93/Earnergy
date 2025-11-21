package com.earnergy.core.data.local.converter

import androidx.room.TypeConverter
import com.earnergy.domain.model.AppRole

class AppRoleConverter {
    @TypeConverter
    fun fromAppRole(role: AppRole): String {
        return role.name
    }

    @TypeConverter
    fun toAppRole(name: String): AppRole {
        return try {
            AppRole.valueOf(name)
        } catch (e: IllegalArgumentException) {
            AppRole.IGNORED // Default fallback
        }
    }
}
