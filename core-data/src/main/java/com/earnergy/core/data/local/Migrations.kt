package com.earnergy.core.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from database version 1 to 2.
 * Adds isSystemApp column to app_usage table.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE app_usage ADD COLUMN isSystemApp INTEGER NOT NULL DEFAULT 0")
    }
}

/**
 * Migration from database version 2 to 3.
 * Adds app_config table.
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS app_config (
                packageName TEXT PRIMARY KEY NOT NULL,
                role TEXT NOT NULL
            )
        """)
    }
}

/**
 * Migration from database version 4 to 5.
 * Adds unlock_events table.
 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS unlock_events (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                timestamp INTEGER NOT NULL,
                dateEpochDay INTEGER NOT NULL,
                wasNotificationLed INTEGER NOT NULL DEFAULT 0,
                triggeringPackage TEXT
            )
        """)

        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_unlock_events_dateEpochDay
            ON unlock_events(dateEpochDay)
        """)

    }
}

/**
 * Migration from database version 3 to 4.
 * Adds three new tables: suggestions, app_switch_events, and break_events.
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create suggestions table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS suggestions (
                id TEXT PRIMARY KEY NOT NULL,
                type TEXT NOT NULL,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                dismissed INTEGER NOT NULL DEFAULT 0,
                completed INTEGER NOT NULL DEFAULT 0
            )
        """)
        
        // Create app_switch_events table with index on dateEpochDay
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS app_switch_events (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                timestamp INTEGER NOT NULL,
                fromPackage TEXT NOT NULL,
                toPackage TEXT NOT NULL,
                dateEpochDay INTEGER NOT NULL
            )
        """)
        
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_app_switch_events_dateEpochDay 
            ON app_switch_events(dateEpochDay)
        """)
        
        // Create break_events table with index on dateEpochDay
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS break_events (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                timestamp INTEGER NOT NULL,
                dateEpochDay INTEGER NOT NULL,
                durationSeconds INTEGER NOT NULL,
                wasManual INTEGER NOT NULL DEFAULT 0
            )
        """)
        
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_break_events_dateEpochDay 
            ON break_events(dateEpochDay)
        """)
    }
}
