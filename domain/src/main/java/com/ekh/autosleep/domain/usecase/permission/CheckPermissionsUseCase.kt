package com.ekh.autosleep.domain.usecase.permission

import com.ekh.autosleep.domain.entity.PermissionState
import com.ekh.autosleep.domain.repository.PermissionRepository

class CheckPermissionsUseCase(private val permissionRepository: PermissionRepository) {
    operator fun invoke(): PermissionState = permissionRepository.getPermissionState()
}
