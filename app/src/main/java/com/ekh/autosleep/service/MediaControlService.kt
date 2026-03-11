package com.ekh.autosleep.service

import android.media.session.MediaSessionManager
import android.service.notification.NotificationListenerService
import com.ekh.autosleep.domain.entity.MediaSessionInfo
import java.lang.ref.WeakReference

class MediaControlService : NotificationListenerService() {

    override fun onListenerConnected() {
        instance = WeakReference(this)
    }

    override fun onListenerDisconnected() {
        instance = null
    }

    private fun getMediaSessionManager(): MediaSessionManager =
        getSystemService(MEDIA_SESSION_SERVICE) as MediaSessionManager

    companion object {
        private var instance: WeakReference<MediaControlService>? = null

        fun isConnected(): Boolean = instance?.get() != null

        fun getActiveSessions(): List<MediaSessionInfo> {
            val service = instance?.get() ?: return emptyList()
            return try {
                service.getMediaSessionManager()
                    .getActiveSessions(service.componentName)
                    .map { controller ->
                        MediaSessionInfo(
                            packageName = controller.packageName ?: "",
                            sessionTag = controller.tag,
                        )
                    }
            } catch (e: SecurityException) {
                emptyList()
            }
        }

        fun pauseAll(): Int {
            val service = instance?.get() ?: return 0
            return try {
                val controllers = service.getMediaSessionManager()
                    .getActiveSessions(service.componentName)
                var paused = 0
                for (controller in controllers) {
                    controller.transportControls?.pause()
                    paused++
                }
                paused
            } catch (e: SecurityException) {
                0
            }
        }
    }
}
