package com.ekh.autosleep.domain.entity

/**
 * 타이머 시작 기록.
 * @param id 고유 식별자
 * @param startedAt 타이머를 시작한 시각 (Unix timestamp, ms)
 * @param durationMs 설정한 타이머 시간 (ms)
 */
data class TimerLog(
    val id: Long = 0,
    val startedAt: Long,
    val durationMs: Long,
)
