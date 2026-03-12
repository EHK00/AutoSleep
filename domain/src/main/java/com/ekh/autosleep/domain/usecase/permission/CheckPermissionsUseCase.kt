package com.ekh.autosleep.domain.usecase.permission

import com.ekh.autosleep.domain.entity.PermissionState
import com.ekh.autosleep.domain.repository.PermissionRepository

/**
 * 현재 권한 상태를 조회하는 Use Case.
 * [MainViewModel]이 온보딩 화면 표시 여부와 수면 기능 활성화 여부를 결정할 때 사용한다.
 */
class CheckPermissionsUseCase(private val permissionRepository: PermissionRepository) {
    /**
     * 현재 시점의 [PermissionState] 스냅샷을 반환한다.
     * 권한 변경을 반영하려면 매번 명시적으로 호출해야 한다.
     */
    operator fun invoke(): PermissionState = permissionRepository.getPermissionState()
}
