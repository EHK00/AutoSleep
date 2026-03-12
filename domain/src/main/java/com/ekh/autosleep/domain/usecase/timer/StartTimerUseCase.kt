package com.ekh.autosleep.domain.usecase.timer

import com.ekh.autosleep.domain.entity.TimerConfig
import com.ekh.autosleep.domain.repository.TimerRepository

/**
 * 타이머를 시작하는 Use Case.
 * [TimerConfig.durationMs]가 0 이하이면 [IllegalArgumentException]을 던진다.
 */
class StartTimerUseCase(private val timerRepository: TimerRepository) {
    /**
     * 주어진 설정으로 타이머를 시작한다.
     * @param config 타이머 설정. [TimerConfig.durationMs]는 반드시 양수여야 한다.
     * @throws IllegalArgumentException durationMs가 0 이하인 경우.
     */
    operator fun invoke(config: TimerConfig) {
        require(config.durationMs > 0) { "Timer duration must be positive" }
        timerRepository.start(config)
    }
}
