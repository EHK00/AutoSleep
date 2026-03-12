package com.ekh.autosleep.domain.usecase.screen

import com.ekh.autosleep.domain.repository.PermissionRepository
import com.ekh.autosleep.domain.repository.ScreenControlRepository

/**
 * 권한 상태에 따라 최적의 방법으로 화면을 잠그는 Use Case.
 *
 * 우선순위:
 * 1. 접근성 서비스 또는 기기 관리자 권한이 있으면 즉시 화면 잠금.
 * 2. WRITE_SETTINGS만 있으면 화면 타임아웃을 1초로 단축(최후 수단).
 * 3. 권한이 없으면 [Result.failure] 반환.
 */
class LockScreenUseCase(
    private val screenControlRepository: ScreenControlRepository,
    private val permissionRepository: PermissionRepository,
) {
    /**
     * 현재 허용된 권한 중 가장 우선순위 높은 방법으로 화면을 끈다.
     * @return 성공 시 [Result.success], 사용 가능한 권한이 없으면 [Result.failure].
     */
    suspend operator fun invoke(): Result<Unit> {
        val permissions = permissionRepository.getPermissionState()
        return when {
            permissions.accessibilityGranted || permissions.deviceAdminGranted ->
                screenControlRepository.lockScreen()
            permissions.writeSettingsGranted ->
                screenControlRepository.shortenScreenTimeout()
            else ->
                Result.failure(IllegalStateException("No screen control permission granted"))
        }
    }
}
