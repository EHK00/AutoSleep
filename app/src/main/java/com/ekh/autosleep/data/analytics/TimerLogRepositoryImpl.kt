package com.ekh.autosleep.data.analytics

import com.ekh.autosleep.data.analytics.db.TimerLogDao
import com.ekh.autosleep.data.analytics.db.TimerLogEntity
import com.ekh.autosleep.domain.entity.TimerLog
import com.ekh.autosleep.domain.repository.TimerLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimerLogRepositoryImpl @Inject constructor(
    private val dao: TimerLogDao,
) : TimerLogRepository {

    override fun getLogs(): Flow<List<TimerLog>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun insert(log: TimerLog) {
        dao.insert(TimerLogEntity(startedAt = log.startedAt, durationMs = log.durationMs))
    }

    private fun TimerLogEntity.toDomain() = TimerLog(id = id, startedAt = startedAt, durationMs = durationMs)
}
