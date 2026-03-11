package com.ekh.autosleep.domain.usecase.timer

import com.ekh.autosleep.domain.entity.TimerConfig
import com.ekh.autosleep.domain.repository.TimerRepository

class StartTimerUseCase(private val timerRepository: TimerRepository) {
    operator fun invoke(config: TimerConfig) {
        require(config.durationMs > 0) { "Timer duration must be positive" }
        timerRepository.start(config)
    }
}
