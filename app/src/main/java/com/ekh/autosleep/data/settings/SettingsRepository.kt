package com.ekh.autosleep.data.settings

import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    val timeFormat: StateFlow<TimeFormat>
    fun setTimeFormat(format: TimeFormat)
    val sleepWindowStartHour: StateFlow<Int>
    val sleepWindowEndHour: StateFlow<Int>
    fun setSleepWindow(startHour: Int, endHour: Int)
}
