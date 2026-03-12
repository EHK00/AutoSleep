package com.ekh.autosleep.domain.usecase.media

import com.ekh.autosleep.domain.repository.MediaControlRepository

/**
 * 현재 활성화된 모든 미디어 세션을 일시정지하는 Use Case.
 * YouTube, Twitch, Spotify 등 [MediaSession] API를 사용하는 앱에 pause/stop 명령을 전송한다.
 */
class PauseAllMediaUseCase(private val mediaControlRepository: MediaControlRepository) {
    /**
     * 활성 미디어 세션 전체에 일시정지를 시도한다.
     * @return 성공 시 일시정지 시도된 세션 수, 실패(권한 없음 등) 시 [Result.failure].
     */
    suspend operator fun invoke(): Result<Int> = mediaControlRepository.pauseAll()
}
