package com.ekh.autosleep.domain.repository

import com.ekh.autosleep.domain.entity.PermissionState

/**
 * 앱에 부여된 권한 상태를 조회하는 저장소 인터페이스.
 * 구현체는 Android 시스템 API를 통해 각 권한의 허용 여부를 확인하며,
 * 결과는 [CheckPermissionsUseCase]를 경유해 UI와 Use Case에 전달된다.
 */
interface PermissionRepository {
    /**
     * 현재 시점의 권한 상태 스냅샷을 반환한다.
     * 권한 변경은 실시간으로 감지하지 않으므로, 화면 복귀 시 명시적으로 재호출해야 한다.
     */
    fun getPermissionState(): PermissionState
}
