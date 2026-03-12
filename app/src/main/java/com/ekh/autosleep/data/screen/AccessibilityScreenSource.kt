package com.ekh.autosleep.data.screen

import com.ekh.autosleep.service.SleepAccessibilityService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [SleepAccessibilityService]를 통해 화면을 잠그는 데이터 소스.
 * [ScreenControlRepositoryImpl]에서 가장 우선순위 높은 화면 잠금 수단으로 사용된다.
 * 접근성 서비스가 활성화되지 않은 경우 [Result.failure]를 반환한다.
 */
@Singleton
class AccessibilityScreenSource @Inject constructor() {
    /**
     * [SleepAccessibilityService.lockScreen]을 호출하여 즉시 화면을 잠근다.
     * @return 잠금 성공 시 [Result.success], 서비스 미연결 또는 잠금 실패 시 [Result.failure].
     */
    fun lockScreen(): Result<Unit> = runCatching {
        val locked = SleepAccessibilityService.lockScreen()
        if (!locked) error("Accessibility service not connected or lock failed")
    }
}
