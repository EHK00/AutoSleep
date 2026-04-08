package com.ekh.autosleep.domain.usecase.analytics

import com.ekh.autosleep.domain.entity.TimerLog
import com.ekh.autosleep.domain.repository.TimerLogRepository

class RecordTimerLogUseCase(private val repo: TimerLogRepository) {
    suspend operator fun invoke(startedAt: Long, durationMs: Long) {
        repo.insert(TimerLog(startedAt = startedAt, durationMs = durationMs))
    }
}
