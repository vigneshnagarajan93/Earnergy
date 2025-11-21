package com.earnergy.di

import android.content.Context
import androidx.room.Room
import com.earnergy.core.data.local.AppConfigDao
import com.earnergy.core.data.local.AppSwitchEventDao
import com.earnergy.core.data.local.AppUsageDao
import com.earnergy.core.data.local.AppUsageDatabase
import com.earnergy.core.data.local.BreakEventDao
import com.earnergy.core.data.local.MIGRATION_3_4
import com.earnergy.core.data.local.SuggestionDao
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
        )
        .addMigrations(MIGRATION_3_4)
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideAppUsageDao(database: AppUsageDatabase): AppUsageDao = database.appUsageDao()

    @Provides
    fun provideAppConfigDao(database: AppUsageDatabase): AppConfigDao = database.appConfigDao()
    
    @Provides
    fun provideSuggestionDao(database: AppUsageDatabase): SuggestionDao = database.suggestionDao()
    
    @Provides
    fun provideAppSwitchEventDao(database: AppUsageDatabase): AppSwitchEventDao = database.appSwitchEventDao()
    
    @Provides
    fun provideBreakEventDao(database: AppUsageDatabase): BreakEventDao = database.breakEventDao()
}
