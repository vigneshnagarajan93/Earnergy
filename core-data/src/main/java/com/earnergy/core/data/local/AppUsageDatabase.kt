package com.earnergy.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.earnergy.core.data.local.converter.AppCategoryConverter

@Database(entities = [AppUsageEntity::class], version = 1, exportSchema = false)
@TypeConverters(AppCategoryConverter::class)
abstract class AppUsageDatabase : RoomDatabase() {
    abstract fun appUsageDao(): AppUsageDao
}
