package com.ekh.autosleep.data.permission

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import com.ekh.autosleep.domain.entity.PermissionState
import com.ekh.autosleep.domain.repository.PermissionRepository
import com.ekh.autosleep.service.SleepAccessibilityService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [PermissionRepository]의 구현체.
 * Android 시스템 API를 직접 조회하여 각 권한의 허용 여부를 확인한다.
 * 접근성 서비스 연결 여부는 [SleepAccessibilityService]의 WeakReference singleton으로 판단한다.
 */
@Singleton
class PermissionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : PermissionRepository {

    /**
     * 두 가지 권한(알림 리스너, 접근성)의 현재 상태를 반환한다.
     * 실시간 감지가 아닌 호출 시점의 스냅샷이므로, 화면 복귀 시 명시적으로 재호출해야 한다.
     */
    override fun getPermissionState(): PermissionState = PermissionState(
        notificationListenerGranted = isNotificationListenerEnabled(),
        accessibilityGranted = SleepAccessibilityService.isConnected(),
        postNotificationsGranted = isPostNotificationsGranted(),
        promotedNotificationsGranted = isPromotedNotificationsGranted(),
    )

    private fun isPromotedNotificationsGranted(): Boolean {
        if (Build.VERSION.SDK_INT < 36) return true
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return nm.canPostPromotedNotifications()
    }

    private fun isPostNotificationsGranted(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }

    /**
     * [Settings.Secure]의 `enabled_notification_listeners` 값에 현재 패키지가 포함되어 있는지 확인한다.
     * @return NotificationListenerService가 시스템에 등록된 경우 true.
     */
    private fun isNotificationListenerEnabled(): Boolean {
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners",
        ) ?: return false
        return enabledListeners.contains(context.packageName)
    }
}
