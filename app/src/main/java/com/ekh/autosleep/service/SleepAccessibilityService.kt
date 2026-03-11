package com.ekh.autosleep.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import java.lang.ref.WeakReference

class SleepAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        instance = WeakReference(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    companion object {
        private var instance: WeakReference<SleepAccessibilityService>? = null

        fun isConnected(): Boolean = instance?.get() != null

        fun lockScreen(): Boolean {
            return instance?.get()
                ?.performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
                ?: false
        }
    }
}
