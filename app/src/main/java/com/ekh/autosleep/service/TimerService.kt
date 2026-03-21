package com.ekh.autosleep.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.ekh.autosleep.AppState
import com.ekh.autosleep.MainActivity
import com.ekh.autosleep.R
import com.ekh.autosleep.domain.entity.TimerState
import com.ekh.autosleep.domain.repository.TimerRepository
import com.ekh.autosleep.domain.usecase.sleep.ExecuteSleepSequenceUseCase
import com.ekh.autosleep.service.TimerService.Companion.EXTRA_DURATION_MS
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 타이머 카운트다운을 포그라운드에서 유지하고, 만료 시 수면 전환을 트리거하는 서비스.
 *
 * [TimerRepository.state]를 구독하여:
 * - [TimerState.Running]: 알림에 남은 시간을 표시한다.
 * - [TimerState.Expired]: [ExecuteSleepSequenceUseCase]를 실행하고 서비스를 종료한다.
 * - [TimerState.Cancelled]: 서비스를 즉시 종료한다.
 *
 * [START_STICKY]로 선언되어 시스템이 프로세스를 종료하더라도 서비스를 재시작하려 시도한다.
 */
@AndroidEntryPoint
class TimerService : Service() {

    @Inject
    lateinit var timerRepository: TimerRepository

    @Inject
    lateinit var executeSleepSequence: ExecuteSleepSequenceUseCase

    @Inject
    lateinit var appState: AppState

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var totalMs: Long = 0L
    private lateinit var notificationManager: NotificationManager
    private var isStarted = false

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("타이머 대기 중"))
        if (appState.isInForeground.value) {
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        }
        observeTimer()
    }

    /** [EXTRA_DURATION_MS] extra에서 총 타이머 시간을 읽어 저장한다. */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        totalMs = intent?.getLongExtra(EXTRA_DURATION_MS, 0L) ?: 0L
        return START_STICKY
    }

    /**
     * [TimerRepository.state]를 수집하여 알림 갱신 및 수면 시퀀스 실행을 처리한다.
     * 만료 시 [ExecuteSleepSequenceUseCase]를 호출한 뒤 [stopSelf]로 서비스를 종료한다.
     */
    private fun observeTimer() {
        Log.d("LiveUpdate", "SDK_INT: ${Build.VERSION.SDK_INT}") // 35 = Android 15, 36 = Android 16
        if (Build.VERSION.SDK_INT >= 36) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            Log.d("LiveUpdate", "canPostPromotedNotifications: ${nm.canPostPromotedNotifications()}")
        } else {
            Log.d("LiveUpdate", "canPostPromotedNotifications API not available (< 36)")
        }
        scope.launch {
            timerRepository.state.collectLatest { state ->
                when (state) {
                    is TimerState.Running -> {
                        val expiryTimeMs = System.currentTimeMillis() + state.remainingMs
                        val notification = buildNotification(
                            text = "수면까지 카운트다운 중",
                            expiryTimeMs = expiryTimeMs,
                            remainingMs = state.remainingMs,
                        )
                        if (!isStarted) {
                            startForeground(NOTIFICATION_ID, notification)
                            isStarted = true
                        } else {
                            notificationManager.notify(NOTIFICATION_ID, notification)
                        }
                    }
                    is TimerState.Expired -> {
                        startForeground(NOTIFICATION_ID, buildNotification("수면 전환 중..."))
                        executeSleepSequence()
                        stopSelf()
                    }
                    is TimerState.Cancelled -> stopSelf()
                    else -> Unit
                }
            }
        }
    }

    /** 바인드 불필요. null 반환. */
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    /** 포그라운드 서비스 알림 채널을 생성한다. Android 8+ 필수. */
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            /* id = */ CHANNEL_ID,
            /* name = */ "자동 수면 타이머",
            /* importance = */ NotificationManager.IMPORTANCE_DEFAULT,
        )
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    /**
     * 포그라운드 알림을 생성한다.
     * [expiryTimeMs]가 0보다 크면 Chronometer 카운트다운을 활성화해 상태바 chip에 표시된다.
     * [remainingMs]가 0보다 크면 ProgressStyle로 경과 진행률을 표시한다.
     */
    private fun buildNotification(
        text: String,
        expiryTimeMs: Long = 0L,
        remainingMs: Long = 0L,
    ): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val expiryTimeSec = expiryTimeMs / 1_000
        val progress = if (totalMs > 0 && remainingMs > 0) {
            ((totalMs - remainingMs).toFloat() / totalMs * 100).toInt().coerceIn(0, 100)
        } else 0
        val progressStyle = NotificationCompat.ProgressStyle()
            .setProgress(progress)
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AutoSleep 타이머")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setWhen(expiryTimeMs)
            .setUsesChronometer(expiryTimeMs > 0)
            .setChronometerCountDown(true)
            .setRequestPromotedOngoing(true)
            .setStyle(progressStyle)
        return builder.build()
    }

    companion object {
        private const val CHANNEL_ID = "autosleep_timer6"
        private const val NOTIFICATION_ID = 1002

        /** 타이머 총 시간(ms)을 전달하는 Intent extra 키. */
        const val EXTRA_DURATION_MS = "extra_duration_ms"
    }
}
