package com.ekh.autosleep.domain.usecase.sleep

import com.ekh.autosleep.domain.entity.SleepResult
import com.ekh.autosleep.domain.usecase.media.PauseAllMediaUseCase
import com.ekh.autosleep.domain.usecase.media.RequestAudioFocusUseCase
import com.ekh.autosleep.domain.usecase.screen.LockScreenUseCase

class ExecuteSleepSequenceUseCase(
    private val pauseAllMedia: PauseAllMediaUseCase,
    private val requestAudioFocus: RequestAudioFocusUseCase,
    private val lockScreen: LockScreenUseCase,
) {
    suspend operator fun invoke(): SleepResult {
        val mediaPauseResult = pauseAllMedia()
        val sessionsPaused = mediaPauseResult.getOrDefault(0)

        // AudioFocus as an additional measure; ignore failure
        requestAudioFocus()

        val lockResult = lockScreen()

        return when {
            mediaPauseResult.isSuccess && lockResult.isSuccess ->
                SleepResult.Success(sessionsPaused)
            lockResult.isFailure ->
                SleepResult.PartialSuccess(
                    sessionsPaused = sessionsPaused,
                    screenLocked = false,
                    error = lockResult.exceptionOrNull()?.message ?: "Screen lock failed",
                )
            else ->
                SleepResult.PartialSuccess(
                    sessionsPaused = sessionsPaused,
                    screenLocked = true,
                    error = mediaPauseResult.exceptionOrNull()?.message ?: "Media pause failed",
                )
        }
    }
}
