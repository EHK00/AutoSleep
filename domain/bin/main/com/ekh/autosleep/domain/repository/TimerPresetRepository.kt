package com.ekh.autosleep.domain.repository

import kotlinx.coroutines.flow.StateFlow

/**
 * 사용자가 저장한 타이머 프리셋 목록을 관리하는 저장소.
 * 프리셋은 밀리초 단위의 타이머 시간 목록으로 영속 관리된다.
 */
interface TimerPresetRepository {
    /** 저장된 프리셋 목록. 최신 저장 순으로 정렬된다. */
    val presets: StateFlow<List<Long>>

    /** [durationMs]를 프리셋에 추가한다. 이미 존재하면 무시한다. */
    fun save(durationMs: Long)

    /** [durationMs]에 해당하는 프리셋을 삭제한다. */
    fun delete(durationMs: Long)
}
