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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int =
        START_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "자동 수면 타이머",
            NotificationManager.IMPORTANCE_LOW,
        )
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

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

    private fun updateNotification(text: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(text))
    }

    companion object {
        private const val CHANNEL_ID = "autosleep_timer"
        private const val NOTIFICATION_ID = 1001
    }
}
