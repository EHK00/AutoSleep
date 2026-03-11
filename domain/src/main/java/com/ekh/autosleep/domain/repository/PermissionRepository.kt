package com.ekh.autosleep.domain.repository

import com.ekh.autosleep.domain.entity.PermissionState

interface PermissionRepository {
    fun getPermissionState(): PermissionState
}
