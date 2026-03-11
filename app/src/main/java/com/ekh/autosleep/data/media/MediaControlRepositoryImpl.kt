package com.ekh.autosleep.data.media

import com.ekh.autosleep.domain.entity.MediaSessionInfo
import com.ekh.autosleep.domain.repository.MediaControlRepository
import com.ekh.autosleep.service.MediaControlService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaControlRepositoryImpl @Inject constructor(
    private val audioFocusDataSource: AudioFocusDataSource,
) : MediaControlRepository {

    override fun getActiveSessions(): List<MediaSessionInfo> {
        return MediaControlService.getActiveSessions()
    }

    override suspend fun pauseAll(): Result<Int> = runCatching {
        MediaControlService.pauseAll()
    }

    override suspend fun requestAudioFocus(): Result<Unit> {
        return audioFocusDataSource.requestFocus()
    }
}
