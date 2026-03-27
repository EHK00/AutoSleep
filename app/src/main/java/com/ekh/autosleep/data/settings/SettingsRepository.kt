package com.ekh.autosleep.data.settings

import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    val timeFormat: StateFlow<TimeFormat>
    fun setTimeFormat(format: TimeFormat)
}
