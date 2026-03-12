package com.ekh.autosleep.domain.entity

/**
 * 타이머 시작에 필요한 설정 값을 담는 불변 데이터 클래스.
 *
 * @property durationMs 타이머 총 지속 시간 (밀리초). 반드시 양수여야 한다.
 * @property label 타이머를 식별하기 위한 선택적 레이블. UI 표시 또는 로깅에 활용된다.
 */
data class TimerConfig(
    val durationMs: Long,
    val label: String = "",
)
