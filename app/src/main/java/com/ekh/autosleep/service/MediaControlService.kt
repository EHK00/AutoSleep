package com.ekh.autosleep.service

import android.content.ComponentName
import android.media.session.MediaSessionManager
import android.service.notification.NotificationListenerService
import com.ekh.autosleep.domain.entity.MediaSessionInfo
import java.lang.ref.WeakReference

/**
 * 미디어 세션 제어를 위한 알림 리스너 서비스.
 * [NotificationListenerService]를 상속하여 시스템으로부터 [MediaSessionManager] 접근 권한을 얻는다.
 *
 * 직접 알림을 처리하지는 않으며, [MediaSessionManager.getActiveSessions]를 통해
 * YouTube, Twitch, Spotify 등 활성 미디어 세션에 pause/stop 명령을 전송하는 것이 목적이다.
 *
 * 시스템이 서비스 수명을 관리하므로, [WeakReference] singleton 패턴으로 현재 인스턴스에 접근한다.
 */
class MediaControlService : NotificationListenerService() {

    override fun onListenerConnected() {
        instance = WeakReference(this)
    }

    override fun onListenerDisconnected() {
        instance = null
    }

    /** [MediaSessionManager] 시스템 서비스를 반환한다. */
    private fun getMediaSessionManager(): MediaSessionManager =
        getSystemService(MEDIA_SESSION_SERVICE) as MediaSessionManager

    companion object {
        private var instance: WeakReference<MediaControlService>? = null

        /** 알림 리스너 서비스가 현재 시스템에 연결되어 있는지 확인한다. */
        fun isConnected(): Boolean = instance?.get() != null

        /**
         * 현재 활성화된 미디어 세션 목록을 [MediaSessionInfo]로 변환하여 반환한다.
         * 서비스 미연결 또는 [SecurityException] 발생 시 빈 리스트를 반환한다.
         */
        fun getActiveSessions(): List<MediaSessionInfo> {
            val service = instance?.get() ?: return emptyList()
            return try {
                service.getMediaSessionManager()
                    .getActiveSessions(ComponentName(service, MediaControlService::class.java))
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

        /**
         * 활성 미디어 세션 전체에 pause 및 stop 명령을 순차적으로 전송한다.
         * pause만으로 멈추지 않는 앱(예: Twitch 라이브)을 위해 stop도 함께 호출한다.
         * @return 명령을 전송한 세션 수. 서비스 미연결 또는 보안 예외 시 0.
         */
        fun pauseAll(): Int {
            val service = instance?.get() ?: return 0
            return try {
                val controllers = service.getMediaSessionManager()
                    .getActiveSessions(ComponentName(service, MediaControlService::class.java))
                var paused = 0
                for (controller in controllers) {
                    controller.transportControls.pause()
                    controller.transportControls.stop()
                    paused++
                }
                paused
            } catch (e: SecurityException) {
                0
            }
        }
    }
}
