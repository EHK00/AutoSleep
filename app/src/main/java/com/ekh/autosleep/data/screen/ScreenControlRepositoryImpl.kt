package com.ekh.autosleep.data.screen

import com.ekh.autosleep.domain.repository.PermissionRepository
import com.ekh.autosleep.domain.repository.ScreenControlRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [ScreenControlRepository]의 구현체.
 * 현재 허용된 권한에 따라 화면 잠금 전략을 선택한다.
 * - 접근성 권한 → [AccessibilityScreenSource]
 * - 기기 관리자 권한 → [DeviceAdminScreenSource]
 * - WRITE_SETTINGS만 있을 경우 → [ScreenTimeoutSource] (타임아웃 단축)
 */
@Singleton
class ScreenControlRepositoryImpl @Inject constructor(
    private val accessibilityScreenSource: AccessibilityScreenSource,
    private val deviceAdminScreenSource: DeviceAdminScreenSource,
    private val screenTimeoutSource: ScreenTimeoutSource,
    private val permissionRepository: PermissionRepository,
) : ScreenControlRepository {

    /**
     * 접근성 → 기기 관리자 우선순위로 화면을 즉시 잠근다.
     * 두 권한 모두 없으면 [Result.failure]를 반환한다.
     */
    override suspend fun lockScreen(): Result<Unit> {
        val permissions = permissionRepository.getPermissionState()
        return when {
            permissions.accessibilityGranted ->
                accessibilityScreenSource.lockScreen()
            permissions.deviceAdminGranted ->
                deviceAdminScreenSource.lockScreen()
            else ->
                Result.failure(IllegalStateException("No lock permission available"))
        }
    }

    /** [ScreenTimeoutSource]에 타임아웃 단축을 위임한다. */
    override suspend fun shortenScreenTimeout(): Result<Unit> =
        screenTimeoutSource.shortenTimeout()

    /** [ScreenTimeoutSource]에 타임아웃 복원을 위임한다. */
    override suspend fun restoreScreenTimeout(): Result<Unit> =
        screenTimeoutSource.restoreTimeout()
}
