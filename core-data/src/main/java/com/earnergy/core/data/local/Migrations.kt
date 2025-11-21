package com.earnergy.core.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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
        
        // Create break_events table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS break_events (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                timestamp INTEGER NOT NULL,
                durationMinutes INTEGER NOT NULL,
                dateEpochDay INTEGER NOT NULL
            )
        """)
    }
}
