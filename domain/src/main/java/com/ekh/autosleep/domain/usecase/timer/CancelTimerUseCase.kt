package com.ekh.autosleep.domain.usecase.timer

import com.ekh.autosleep.domain.repository.TimerRepository

/**
 * 실행 중인 타이머를 취소하는 Use Case.
 * 타이머 상태를 [TimerState.Cancelled]로 전환하고, 진행 중인 카운트다운 코루틴을 중단한다.
 */
class CancelTimerUseCase(private val timerRepository: TimerRepository) {
    /** 실행 중인 타이머를 즉시 취소한다. 타이머가 idle 상태여도 안전하게 호출할 수 있다. */
    operator fun invoke() {
        timerRepository.cancel()
    }
}
