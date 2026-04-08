package com.ekh.autosleep.domain.usecase.analytics

import com.ekh.autosleep.domain.entity.TimerLog
import com.ekh.autosleep.domain.repository.TimerLogRepository
import kotlinx.coroutines.flow.Flow

class GetTimerLogsUseCase(private val repo: TimerLogRepository) {
    operator fun invoke(): Flow<List<TimerLog>> = repo.getLogs()
}
