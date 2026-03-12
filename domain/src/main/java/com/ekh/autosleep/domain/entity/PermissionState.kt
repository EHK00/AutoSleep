package com.ekh.autosleep.domain.entity

/**
 * 앱이 동작하는 데 필요한 각 권한의 허용 여부를 담는 데이터 클래스.
 * [CheckPermissionsUseCase]가 [PermissionRepository]를 통해 조회하며,
 * UI 온보딩 화면과 수면 시퀀스 실행 여부 결정에 사용된다.
 *
 * @property notificationListenerGranted 알림 리스너 권한 허용 여부. 미디어 세션 제어에 필요.
 * @property accessibilityGranted 접근성 서비스 활성화 여부. 화면 잠금(GLOBAL_ACTION_LOCK_SCREEN)에 필요.
 * @property deviceAdminGranted 기기 관리자 권한 허용 여부. 접근성 서비스 대체 화면 잠금 수단.
 * @property writeSettingsGranted WRITE_SETTINGS 권한 허용 여부. 화면 타임아웃 단축의 최후 수단.
 */
data class PermissionState(
    val notificationListenerGranted: Boolean,
    val accessibilityGranted: Boolean,
    val deviceAdminGranted: Boolean,
    val writeSettingsGranted: Boolean,
) {
    /** NotificationListenerService가 활성화되어 미디어 세션을 제어할 수 있는 경우 true. */
    val canControlMedia: Boolean get() = notificationListenerGranted

    /** 접근성 서비스, 기기 관리자, WRITE_SETTINGS 중 하나라도 허용된 경우 true. */
    val canLockScreen: Boolean get() = accessibilityGranted || deviceAdminGranted || writeSettingsGranted
}
