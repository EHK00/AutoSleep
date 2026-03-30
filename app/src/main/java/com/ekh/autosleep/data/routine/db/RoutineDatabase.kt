package com.ekh.autosleep.data.routine.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ekh.autosleep.data.analytics.db.TimerLogDao
import com.ekh.autosleep.data.analytics.db.TimerLogEntity

@Database(
    entities = [RoutineEntity::class, TimerLogEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class RoutineDatabase : RoomDatabase() {
    abstract fun routineDao(): RoutineDao
    abstract fun timerLogDao(): TimerLogDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `timer_logs` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `startedAt` INTEGER NOT NULL,
                `durationMs` INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}
