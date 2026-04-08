package com.ekh.autosleep.domain.repository

import com.ekh.autosleep.domain.entity.TimerLog
import kotlinx.coroutines.flow.Flow

interface TimerLogRepository {
    fun getLogs(): Flow<List<TimerLog>>
    suspend fun insert(log: TimerLog)
}
