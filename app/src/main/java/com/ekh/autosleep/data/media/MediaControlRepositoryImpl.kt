package com.ekh.autosleep.data.media

import com.ekh.autosleep.domain.entity.MediaSessionInfo
import com.ekh.autosleep.domain.repository.MediaControlRepository
import com.ekh.autosleep.service.MediaControlService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [MediaControlRepository]의 구현체.
 * [MediaControlService] companion object의 정적 메서드를 통해 활성 미디어 세션에 접근하고,
 * [AudioFocusDataSource]를 통해 AudioFocus 요청을 위임한다.
 */
@Singleton
class MediaControlRepositoryImpl @Inject constructor(
    private val audioFocusDataSource: AudioFocusDataSource,
) : MediaControlRepository {

    /** [MediaControlService]가 연결된 경우 현재 활성 미디어 세션 목록을 반환한다. 미연결 시 빈 리스트. */
    override fun getActiveSessions(): List<MediaSessionInfo> {
        return MediaControlService.getActiveSessions()
    }

    /**
     * [MediaControlService]를 통해 모든 활성 세션에 pause/stop 명령을 전송한다.
     * [MediaControlService]가 연결되지 않은 경우 0을 반환한다.
     */
    override suspend fun pauseAll(): Result<Int> = runCatching {
        MediaControlService.pauseAll()
    }

    /** [AudioFocusDataSource]에 AudioFocus 획득을 위임한다. */
    override suspend fun requestAudioFocus(): Result<Unit> {
        return audioFocusDataSource.requestFocus()
    }
}
