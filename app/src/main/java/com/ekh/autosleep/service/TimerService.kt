package com.ekh.autosleep.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.ekh.autosleep.MainActivity
import com.ekh.autosleep.R
import com.ekh.autosleep.domain.entity.TimerState
import com.ekh.autosleep.domain.repository.TimerRepository
import com.ekh.autosleep.domain.usecase.sleep.ExecuteSleepSequenceUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
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

    @Inject lateinit var timerRepository: TimerRepository
    @Inject lateinit var executeSleepSequence: ExecuteSleepSequenceUseCase

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("타이머 대기 중"))
        observeTimer()
    }

    /**
     * [TimerRepository.state]를 수집하여 알림 갱신 및 수면 시퀀스 실행을 처리한다.
     * 만료 시 [ExecuteSleepSequenceUseCase]를 호출한 뒤 [stopSelf]로 서비스를 종료한다.
     */
    private fun observeTimer() {
        scope.launch {
            timerRepository.state.collect { state ->
                when (state) {
                    is TimerState.Running -> {
                        val remaining = state.remainingMs / 1_000
                        updateNotification("수면까지 ${remaining}초")
                    }
                    is TimerState.Expired -> {
                        updateNotification("수면 전환 중...")
                        executeSleepSequence()
                        stopSelf()
                    }
                    is TimerState.Cancelled -> stopSelf()
                    else -> Unit
                }
            }
        }
    }

    /** 시스템 재시작 후에도 서비스가 복구되도록 [START_STICKY]를 반환한다. */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int =
        START_STICKY

    /** 바인드 불필요. null 반환. */
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    /** 포그라운드 서비스 알림 채널을 생성한다. Android 8+ 필수. */
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "자동 수면 타이머",
            NotificationManager.IMPORTANCE_LOW,
        )
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    /**
     * 주어진 텍스트로 포그라운드 알림을 생성한다.
     * 알림을 탭하면 [MainActivity]로 이동한다.
     */
    private fun buildNotification(text: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AutoSleep")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    /** 기존 포그라운드 알림의 텍스트를 갱신한다. */
    private fun updateNotification(text: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(text))
    }

    companion object {
        private const val CHANNEL_ID = "autosleep_timer"
        private const val NOTIFICATION_ID = 1001
    }
}
