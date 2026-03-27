package com.ekh.autosleep.domain.entity

data class Routine(
    val id: Long = 0,
    val hour: Int,
    val minute: Int,
    /** 반복 요일. Calendar.MONDAY(2)~Calendar.SUNDAY(1) 상수. 빈 집합 = 1회성 */
    val days: Set<Int>,
    val label: String,
    val isEnabled: Boolean = true,
)
