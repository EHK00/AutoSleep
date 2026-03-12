package com.ekh.autosleep.data.screen

import android.content.Context
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [Settings.System.SCREEN_OFF_TIMEOUT]을 조작하여 화면을 빠르게 끄는 데이터 소스.
 * 접근성 서비스와 기기 관리자 권한이 모두 없을 때 사용하는 최후 수단 폴백.
 * WRITE_SETTINGS 권한([Settings.System.canWrite])이 필요하며, 기존 타임아웃을 저장해 복원한다.
 */
@Singleton
class ScreenTimeoutSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    /** [shortenTimeout] 호출 전 저장된 원래 화면 타임아웃 값 (밀리초). */
    private var previousTimeoutMs: Int = DEFAULT_TIMEOUT_MS

    /** WRITE_SETTINGS 권한이 허용되어 있는지 확인한다. */
    fun canWrite(): Boolean = Settings.System.canWrite(context)

    /**
     * 현재 화면 타임아웃을 저장한 후 [MINIMUM_TIMEOUT_MS](1초)로 단축한다.
     * @return 성공 시 [Result.success], 권한 없거나 실패 시 [Result.failure].
     */
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

    /**
     * [shortenTimeout] 호출 이전에 저장한 화면 타임아웃 값을 복원한다.
     * @return 성공 시 [Result.success], 권한 없거나 실패 시 [Result.failure].
     */
    fun restoreTimeout(): Result<Unit> = runCatching {
        if (!canWrite()) error("WRITE_SETTINGS permission not granted")
        Settings.System.putInt(
            context.contentResolver,
            Settings.System.SCREEN_OFF_TIMEOUT,
            previousTimeoutMs,
        )
    }

    companion object {
        /** 저장된 타임아웃이 없을 때 사용하는 기본값 (30초). */
        private const val DEFAULT_TIMEOUT_MS = 30_000
        /** 타임아웃 단축 시 설정할 최솟값 (1초). */
        private const val MINIMUM_TIMEOUT_MS = 1_000
    }
}
