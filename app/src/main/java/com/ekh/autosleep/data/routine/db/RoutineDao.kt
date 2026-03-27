package com.ekh.autosleep.data.routine.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routines ORDER BY hour ASC, minute ASC")
    fun getAll(): Flow<List<RoutineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RoutineEntity): Long

    @Update
    suspend fun update(entity: RoutineEntity)

    @Delete
    suspend fun delete(entity: RoutineEntity)
}
