package com.ekh.autosleep.data.screen

import com.ekh.autosleep.domain.repository.PermissionRepository
import com.ekh.autosleep.domain.repository.ScreenControlRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScreenControlRepositoryImpl @Inject constructor(
    private val accessibilityScreenSource: AccessibilityScreenSource,
    private val deviceAdminScreenSource: DeviceAdminScreenSource,
    private val screenTimeoutSource: ScreenTimeoutSource,
    private val permissionRepository: PermissionRepository,
) : ScreenControlRepository {

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

    override suspend fun shortenScreenTimeout(): Result<Unit> =
        screenTimeoutSource.shortenTimeout()

    override suspend fun restoreScreenTimeout(): Result<Unit> =
        screenTimeoutSource.restoreTimeout()
}
