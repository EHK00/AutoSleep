package com.ekh.autosleep.domain.entity

sealed class SleepResult {
    data class Success(val sessionsPaused: Int) : SleepResult()
    data class PartialSuccess(
        val sessionsPaused: Int,
        val screenLocked: Boolean,
        val error: String,
    ) : SleepResult()
    data class Failure(val error: String) : SleepResult()
}
