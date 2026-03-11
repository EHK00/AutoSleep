package com.ekh.autosleep.domain.entity

sealed class TimerState {
    data object Idle : TimerState()
    data class Running(val remainingMs: Long) : TimerState()
    data object Expired : TimerState()
    data object Cancelled : TimerState()
}
