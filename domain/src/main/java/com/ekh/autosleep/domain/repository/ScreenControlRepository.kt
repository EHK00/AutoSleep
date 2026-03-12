package com.ekh.autosleep.domain.repository

/**
 * 화면 잠금 및 타임아웃 제어를 담당하는 저장소 인터페이스.
 * 구현체는 접근성 서비스 → 기기 관리자 순의 우선순위로 화면을 끄며,
 * 두 권한이 모두 없을 경우 WRITE_SETTINGS를 통한 타임아웃 단축으로 폴백한다.
 */
interface ScreenControlRepository {
    /**
     * 즉시 화면을 잠근다.
     * 접근성 서비스([SleepAccessibilityService]) 또는 기기 관리자([DevicePolicyManager]) 권한이 필요하다.
     * @return 성공 시 [Result.success], 권한 없거나 실패 시 [Result.failure].
     */
    suspend fun lockScreen(): Result<Unit>

    /**
     * 화면 자동 꺼짐 타임아웃을 최솟값(1초)으로 단축한다.
     * WRITE_SETTINGS 권한이 필요하며, 기존 타임아웃 값을 내부에 저장해 복원 시 사용한다.
     * @return 성공 시 [Result.success], 권한 없거나 실패 시 [Result.failure].
     */
    suspend fun shortenScreenTimeout(): Result<Unit>

    /**
     * [shortenScreenTimeout] 호출 이전의 화면 타임아웃 값을 복원한다.
     * @return 성공 시 [Result.success], 권한 없거나 실패 시 [Result.failure].
     */
    suspend fun restoreScreenTimeout(): Result<Unit>
}
