package com.ekh.autosleep.service

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * 타이머 포그라운드 서비스의 생명주기를 추상화하는 인터페이스.
 * ViewModel이 [TimerService] 구체 클래스를 직접 참조하지 않도록 분리한다.
 */
interface TimerServiceController {
    fun start(durationMs: Long)
    fun cancel()
}

/**
 * [TimerServiceController]의 실제 구현체.
 * [TimerService] 포그라운드 서비스를 시작/종료한다.
 */
class TimerServiceControllerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : TimerServiceController {

    override fun start(durationMs: Long) {
        context.startForegroundService(
            Intent(context, TimerService::class.java)
                .putExtra(TimerService.EXTRA_DURATION_MS, durationMs),
        )
    }

    override fun cancel() {
        context.stopService(Intent(context, TimerService::class.java))
    }
}
