package com.earnergy.di

import android.content.Context
import androidx.room.Room
import com.earnergy.core.data.local.AppUsageDao
import com.earnergy.core.data.local.AppUsageDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppUsageDatabase {
        return Room.databaseBuilder(
            context,
            AppUsageDatabase::class.java,
            "app_usage.db"
        ).build()
    }

    @Provides
    fun provideAppUsageDao(database: AppUsageDatabase): AppUsageDao = database.appUsageDao()
}
