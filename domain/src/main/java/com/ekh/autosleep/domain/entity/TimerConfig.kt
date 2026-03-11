package com.ekh.autosleep.domain.entity

data class TimerConfig(
    val durationMs: Long,
    val label: String = "",
)
