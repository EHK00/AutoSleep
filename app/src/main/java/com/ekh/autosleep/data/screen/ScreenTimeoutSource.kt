package com.ekh.autosleep.data.screen

import android.content.Context
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScreenTimeoutSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private var previousTimeoutMs: Int = DEFAULT_TIMEOUT_MS

    fun canWrite(): Boolean = Settings.System.canWrite(context)

    fun shortenTimeout(): Result<Unit> = runCatching {
        if (!canWrite()) error("WRITE_SETTINGS permission not granted")
        previousTimeoutMs = Settings.System.getInt(
            context.contentResolver,
            Settings.System.SCREEN_OFF_TIMEOUT,
            DEFAULT_TIMEOUT_MS,
        )
        Settings.System.putInt(
            context.contentResolver,
            Settings.System.SCREEN_OFF_TIMEOUT,
            MINIMUM_TIMEOUT_MS,
        )
    }

    fun restoreTimeout(): Result<Unit> = runCatching {
        if (!canWrite()) error("WRITE_SETTINGS permission not granted")
        Settings.System.putInt(
            context.contentResolver,
            Settings.System.SCREEN_OFF_TIMEOUT,
            previousTimeoutMs,
        )
    }

    companion object {
        private const val DEFAULT_TIMEOUT_MS = 30_000
        private const val MINIMUM_TIMEOUT_MS = 1_000
    }
}
