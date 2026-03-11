package com.ekh.autosleep.domain.repository

import com.ekh.autosleep.domain.entity.TimerConfig
import com.ekh.autosleep.domain.entity.TimerState
import kotlinx.coroutines.flow.StateFlow

interface TimerRepository {
    val state: StateFlow<TimerState>
    fun start(config: TimerConfig)
    fun cancel()
}
