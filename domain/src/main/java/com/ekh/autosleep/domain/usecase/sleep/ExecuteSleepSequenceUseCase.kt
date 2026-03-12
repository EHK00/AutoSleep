package com.ekh.autosleep.domain.usecase.sleep

import com.ekh.autosleep.domain.entity.SleepResult
import com.ekh.autosleep.domain.usecase.media.PauseAllMediaUseCase
import com.ekh.autosleep.domain.usecase.media.RequestAudioFocusUseCase
import com.ekh.autosleep.domain.usecase.screen.LockScreenUseCase

/**
 * 타이머 만료 시 실행되는 수면 전환 시퀀스를 조율하는 Use Case.
 *
 * 실행 순서:
 * 1. [PauseAllMediaUseCase] — 활성 미디어 세션 전체 일시정지.
 * 2. [RequestAudioFocusUseCase] — AudioFocus 강제 획득 (실패 무시).
 * 3. [LockScreenUseCase] — 권한에 따라 화면 잠금.
 *
 * 결과는 [SleepResult]로 반환되며, 부분 실패(미디어 또는 화면 잠금 중 하나만 성공)도 표현한다.
 */
class ExecuteSleepSequenceUseCase(
    private val pauseAllMedia: PauseAllMediaUseCase,
    private val requestAudioFocus: RequestAudioFocusUseCase,
    private val lockScreen: LockScreenUseCase,
) {
    /**
     * 미디어 일시정지 → AudioFocus 획득 → 화면 잠금 순으로 수면 전환을 실행한다.
     * @return 모두 성공 시 [SleepResult.Success], 부분 실패 시 [SleepResult.PartialSuccess],
     *         권한 없이 화면 잠금 불가능한 경우 [SleepResult.PartialSuccess] (screenLocked=false).
     */
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
