package com.ekh.autosleep.data.screen

import com.ekh.autosleep.service.SleepAccessibilityService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccessibilityScreenSource @Inject constructor() {
    fun lockScreen(): Result<Unit> = runCatching {
        val locked = SleepAccessibilityService.lockScreen()
        if (!locked) error("Accessibility service not connected or lock failed")
    }
}
