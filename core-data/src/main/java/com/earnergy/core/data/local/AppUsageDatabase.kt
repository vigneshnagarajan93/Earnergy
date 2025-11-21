package com.earnergy.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.earnergy.core.data.local.converter.AppCategoryConverter
import com.earnergy.core.data.local.converter.AppRoleConverter

@Database(entities = [AppUsageEntity::class, AppConfigEntity::class], version = 3, exportSchema = false)
@TypeConverters(AppCategoryConverter::class, AppRoleConverter::class)
abstract class AppUsageDatabase : RoomDatabase() {
    abstract fun appUsageDao(): AppUsageDao
    abstract fun appConfigDao(): AppConfigDao
}
