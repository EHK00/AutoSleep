package com.ekh.autosleep.domain.repository

import com.ekh.autosleep.domain.entity.MediaSessionInfo

interface MediaControlRepository {
    fun getActiveSessions(): List<MediaSessionInfo>
    suspend fun pauseAll(): Result<Int>
    suspend fun requestAudioFocus(): Result<Unit>
}
