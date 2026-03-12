package com.ekh.autosleep.domain.usecase.media

import com.ekh.autosleep.domain.repository.MediaControlRepository

/**
 * AudioFocus를 강제 획득하여 재생 중인 앱이 스스로 일시정지하도록 유도하는 Use Case.
 * [PauseAllMediaUseCase]의 보조 수단으로, MediaSession을 등록하지 않는 앱에도 부분적으로 효과가 있다.
 * 단, 앱이 AudioFocus loss를 무시하면 효과가 없으므로 실패는 치명적이지 않다.
 */
class RequestAudioFocusUseCase(private val mediaControlRepository: MediaControlRepository) {
    /**
     * AudioFocus GAIN을 요청한다. 실패해도 수면 시퀀스는 계속 진행된다.
     * @return 성공 시 [Result.success], 시스템이 포커스를 허용하지 않으면 [Result.failure].
     */
    suspend operator fun invoke(): Result<Unit> = mediaControlRepository.requestAudioFocus()
}
