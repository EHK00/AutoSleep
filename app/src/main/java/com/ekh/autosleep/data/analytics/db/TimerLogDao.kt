package com.ekh.autosleep.data.analytics.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TimerLogDao {
    @Query("SELECT * FROM timer_logs ORDER BY startedAt DESC")
    fun getAll(): Flow<List<TimerLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: TimerLogEntity)
}
