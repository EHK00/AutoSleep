package com.ekh.autosleep.domain.entity

/**
 * 타이머의 현재 상태를 나타내는 sealed class.
 * UI와 서비스 레이어가 이 상태를 구독(StateFlow)하여 화면 및 알림을 갱신한다.
 */
sealed class TimerState {
    /** 타이머가 시작되지 않은 초기 상태. */
    data object Idle : TimerState()

    /**
     * 타이머가 카운트다운 중인 상태.
     * @property remainingMs 만료까지 남은 시간 (밀리초).
     */
    data class Running(val remainingMs: Long) : TimerState()

    /** 타이머가 정상적으로 만료된 상태. 수면 전환 시퀀스 트리거 신호로 사용된다. */
    data object Expired : TimerState()

    /** 사용자가 타이머를 수동으로 취소한 상태. */
    data object Cancelled : TimerState()
}
