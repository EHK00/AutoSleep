package com.ekh.autosleep.domain.repository

import com.ekh.autosleep.domain.entity.MediaSessionInfo

/**
 * 미디어 재생 제어를 담당하는 저장소 인터페이스.
 * 구현체는 [NotificationListenerService]를 통해 활성 미디어 세션에 접근하고,
 * AudioFocus 요청을 통해 보조적인 재생 중단을 시도한다.
 */
interface MediaControlRepository {
    /** 현재 활성화된 모든 미디어 세션 목록을 반환한다. */
    fun getActiveSessions(): List<MediaSessionInfo>

    /**
     * 활성 미디어 세션 전체에 pause 및 stop 명령을 전송한다.
     * @return 성공 시 일시정지 시도된 세션 수, 실패 시 [Result.failure].
     */
    suspend fun pauseAll(): Result<Int>

    /**
     * AudioFocus를 강제로 획득하여 재생 중인 앱이 스스로 일시정지하도록 유도한다.
     * MediaSession 제어를 지원하지 않는 앱에 대한 보조 수단으로 활용된다.
     * @return 성공 시 [Result.success], 실패 시 [Result.failure].
     */
    suspend fun requestAudioFocus(): Result<Unit>
}
