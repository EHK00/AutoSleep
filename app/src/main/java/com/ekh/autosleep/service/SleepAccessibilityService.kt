package com.ekh.autosleep.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import java.lang.ref.WeakReference

/**
 * 화면 잠금을 위한 접근성 서비스.
 * [GLOBAL_ACTION_LOCK_SCREEN]을 수행하는 것이 유일한 목적이며, 실제 접근성 이벤트는 처리하지 않는다.
 *
 * XML에서 `accessibilityEventTypes="typeWindowStateChanged"`로 선언되어 있지만,
 * [onServiceConnected]에서 `eventTypes = 0`으로 런타임 오버라이드하여 이벤트 수신을 차단한다.
 * 이를 통해 배터리 소모 없이 화면 잠금 기능만 제공한다.
 *
 * 시스템이 서비스 수명을 관리하므로, [WeakReference] singleton 패턴으로 현재 인스턴스에 접근한다.
 */
class SleepAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        // XML의 typeWindowStateChanged는 Android 빌드 validator를 통과하기 위한 것이고,
        // 런타임에 eventTypes=0으로 오버라이드해 이벤트 수신을 차단한다.
        serviceInfo = serviceInfo.apply { eventTypes = 0 }
        instance = WeakReference(this)
    }

    /** 이벤트를 수신하지 않으므로 구현이 비어 있다. */
    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    /** 인터럽트 처리 불필요. */
    override fun onInterrupt() = Unit

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    companion object {
        private var instance: WeakReference<SleepAccessibilityService>? = null

        /** 접근성 서비스가 현재 시스템에 연결되어 있는지 확인한다. */
        fun isConnected(): Boolean = instance?.get() != null

        /**
         * [GLOBAL_ACTION_LOCK_SCREEN]을 수행하여 즉시 화면을 잠근다.
         * @return 잠금 명령 전송 성공 시 true, 서비스 미연결 시 false.
         */
        fun lockScreen(): Boolean {
            return instance?.get()
                ?.performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
                ?: false
        }
    }
}
