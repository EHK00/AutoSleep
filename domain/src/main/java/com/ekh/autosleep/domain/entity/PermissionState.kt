package com.ekh.autosleep.domain.entity

data class PermissionState(
    val notificationListenerGranted: Boolean,
    val accessibilityGranted: Boolean,
    val deviceAdminGranted: Boolean,
    val writeSettingsGranted: Boolean,
) {
    val canControlMedia: Boolean get() = notificationListenerGranted
    val canLockScreen: Boolean get() = accessibilityGranted || deviceAdminGranted || writeSettingsGranted
}
