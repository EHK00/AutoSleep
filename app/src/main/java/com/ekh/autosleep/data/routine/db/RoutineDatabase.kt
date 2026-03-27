package com.ekh.autosleep.data.routine.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [RoutineEntity::class], version = 1, exportSchema = false)
abstract class RoutineDatabase : RoomDatabase() {
    abstract fun routineDao(): RoutineDao
}
