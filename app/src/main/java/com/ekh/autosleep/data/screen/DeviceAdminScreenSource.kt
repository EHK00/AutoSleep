package com.ekh.autosleep.data.screen

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import com.ekh.autosleep.service.AdminReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [DevicePolicyManager]를 통해 화면을 잠그는 데이터 소스.
 * 접근성 서비스를 사용할 수 없을 때 [ScreenControlRepositoryImpl]의 두 번째 화면 잠금 수단으로 사용된다.
 * 앱이 기기 관리자([AdminReceiver])로 등록되어 있어야 한다.
 */
@Singleton
class DeviceAdminScreenSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val devicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val adminComponent = ComponentName(context, AdminReceiver::class.java)

    /** [AdminReceiver]가 기기 관리자로 활성화되어 있는지 확인한다. */
    fun isAdminActive(): Boolean = devicePolicyManager.isAdminActive(adminComponent)

    /**
     * [DevicePolicyManager.lockNow]를 호출하여 즉시 화면을 잠근다.
     * @return 잠금 성공 시 [Result.success], 기기 관리자 비활성 시 [Result.failure].
     */
    fun lockScreen(): Result<Unit> = runCatching {
        if (!isAdminActive()) error("Device admin not active")
        devicePolicyManager.lockNow()
    }
}
