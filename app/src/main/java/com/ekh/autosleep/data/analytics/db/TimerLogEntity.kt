package com.ekh.autosleep.data.analytics.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timer_logs")
data class TimerLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAt: Long,
    val durationMs: Long,
)
