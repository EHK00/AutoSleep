package com.ekh.autosleep.data.screen

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import com.ekh.autosleep.service.AdminReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceAdminScreenSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val devicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val adminComponent = ComponentName(context, AdminReceiver::class.java)

    fun isAdminActive(): Boolean = devicePolicyManager.isAdminActive(adminComponent)

    fun lockScreen(): Result<Unit> = runCatching {
        if (!isAdminActive()) error("Device admin not active")
        devicePolicyManager.lockNow()
    }
}
