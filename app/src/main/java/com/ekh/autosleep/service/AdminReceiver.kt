package com.ekh.autosleep.service

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent

/**
 * 기기 관리자 권한을 위한 [DeviceAdminReceiver] 구현체.
 * [DeviceAdminScreenSource]가 [DevicePolicyManager.lockNow]를 호출하기 위해 등록된다.
 * 권한 활성화/비활성화 시 별도 처리가 필요 없으므로 콜백 구현이 비어 있다.
 */
class AdminReceiver : DeviceAdminReceiver() {
    /** 기기 관리자 권한이 허용될 때 호출된다. 별도 처리 없음. */
    override fun onEnabled(context: Context, intent: Intent) = Unit

    /** 기기 관리자 권한이 해제될 때 호출된다. 별도 처리 없음. */
    override fun onDisabled(context: Context, intent: Intent) = Unit
}
