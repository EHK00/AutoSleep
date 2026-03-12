package com.ekh.autosleep.domain.repository

import com.ekh.autosleep.domain.entity.TimerConfig
import com.ekh.autosleep.domain.entity.TimerState
import kotlinx.coroutines.flow.StateFlow

/**
 * 타이머 상태를 관리하는 저장소 인터페이스.
 * 구현체([TimerRepositoryImpl])는 코루틴 기반 카운트다운을 수행하며,
 * 상태 변화를 [StateFlow]로 방출한다.
 */
interface TimerRepository {
    /** 현재 타이머 상태를 방출하는 스트림. 초기값은 [TimerState.Idle]. */
    val state: StateFlow<TimerState>

    /**
     * 주어진 설정으로 타이머를 시작한다. 이미 실행 중인 타이머는 취소 후 재시작된다.
     * @param config 타이머 지속 시간 등 시작 설정.
     */
    fun start(config: TimerConfig)

    /** 실행 중인 타이머를 취소하고 상태를 [TimerState.Cancelled]로 전환한다. */
    fun cancel()
}
