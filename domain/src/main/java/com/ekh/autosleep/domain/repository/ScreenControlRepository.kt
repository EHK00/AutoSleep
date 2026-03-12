package com.ekh.autosleep.domain.repository

/**
 * 화면 잠금을 담당하는 저장소 인터페이스.
 * 구현체는 [SleepAccessibilityService]를 통해 [GLOBAL_ACTION_LOCK_SCREEN]을 수행한다.
 */
interface ScreenControlRepository {
    /**
     * 접근성 서비스([SleepAccessibilityService])를 통해 즉시 화면을 잠근다.
     * @return 성공 시 [Result.success], 서비스 미연결 또는 실패 시 [Result.failure].
     */
    suspend fun lockScreen(): Result<Unit>
}
