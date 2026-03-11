package com.ekh.autosleep.domain.usecase.timer

import com.ekh.autosleep.domain.repository.TimerRepository

class CancelTimerUseCase(private val timerRepository: TimerRepository) {
    operator fun invoke() {
        timerRepository.cancel()
    }
}
