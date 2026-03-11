package com.ekh.autosleep.data.permission

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import com.ekh.autosleep.domain.entity.PermissionState
import com.ekh.autosleep.domain.repository.PermissionRepository
import com.ekh.autosleep.service.AdminReceiver
import com.ekh.autosleep.service.SleepAccessibilityService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : PermissionRepository {

    private val devicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val adminComponent = ComponentName(context, AdminReceiver::class.java)

    override fun getPermissionState(): PermissionState = PermissionState(
        notificationListenerGranted = isNotificationListenerEnabled(),
        accessibilityGranted = SleepAccessibilityService.isConnected(),
        deviceAdminGranted = devicePolicyManager.isAdminActive(adminComponent),
        writeSettingsGranted = Settings.System.canWrite(context),
    )

    private fun isNotificationListenerEnabled(): Boolean {
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners",
        ) ?: return false
        return enabledListeners.contains(context.packageName)
    }
}
