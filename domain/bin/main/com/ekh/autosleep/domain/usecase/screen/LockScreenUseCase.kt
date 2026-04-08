package com.ekh.autosleep.domain.usecase.screen

import com.ekh.autosleep.domain.repository.ScreenControlRepository

/**
 * 접근성 서비스를 통해 화면을 잠그는 Use Case.
 * [SleepAccessibilityService]가 연결되지 않은 경우 [Result.failure]를 반환한다.
 */
class LockScreenUseCase(
    private val screenControlRepository: ScreenControlRepository,
) {
    /**
     * [ScreenControlRepository.lockScreen]을 호출하여 즉시 화면을 잠근다.
     * @return 성공 시 [Result.success], 접근성 서비스 미연결 또는 실패 시 [Result.failure].
     */
    suspend operator fun invoke(): Result<Unit> = screenControlRepository.lockScreen()
}
