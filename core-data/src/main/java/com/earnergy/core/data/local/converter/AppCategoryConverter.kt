package com.earnergy.core.data.local.converter

import androidx.room.TypeConverter
import com.earnergy.domain.model.AppCategory

class AppCategoryConverter {
    @TypeConverter
    fun fromStored(value: String): AppCategory = AppCategory.valueOf(value)

    @TypeConverter
    fun toStored(category: AppCategory): String = category.name
}
