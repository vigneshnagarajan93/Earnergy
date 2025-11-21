package com.earnergy.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.earnergy.core.data.local.converter.AppCategoryConverter
import com.earnergy.core.data.local.converter.AppRoleConverter

@Database(
    entities = [
        AppUsageEntity::class,
        AppConfigEntity::class,
        SuggestionEntity::class,
        AppSwitchEventEntity::class,
        BreakEventEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(AppCategoryConverter::class, AppRoleConverter::class)
abstract class AppUsageDatabase : RoomDatabase() {
    abstract fun appUsageDao(): AppUsageDao
    abstract fun appConfigDao(): AppConfigDao
    abstract fun suggestionDao(): SuggestionDao
    abstract fun appSwitchEventDao(): AppSwitchEventDao
    abstract fun breakEventDao(): BreakEventDao
}
