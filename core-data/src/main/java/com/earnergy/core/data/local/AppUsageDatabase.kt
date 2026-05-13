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
        BreakEventEntity::class,
        UnlockEventEntity::class,
        NotificationEventEntity::class
    ],
    version = 7,
    exportSchema = false
)
@TypeConverters(AppCategoryConverter::class, AppRoleConverter::class)
abstract class AppUsageDatabase : RoomDatabase() {
    abstract fun appUsageDao(): AppUsageDao
    abstract fun appConfigDao(): AppConfigDao
    abstract fun suggestionDao(): SuggestionDao
    abstract fun appSwitchEventDao(): AppSwitchEventDao
    abstract fun breakEventDao(): BreakEventDao
    abstract fun unlockEventDao(): UnlockEventDao
    abstract fun notificationEventDao(): NotificationEventDao
}
