package com.ekh.autosleep.domain.usecase.screen

import com.ekh.autosleep.domain.repository.PermissionRepository
import com.ekh.autosleep.domain.repository.ScreenControlRepository

class LockScreenUseCase(
    private val screenControlRepository: ScreenControlRepository,
    private val permissionRepository: PermissionRepository,
) {
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
