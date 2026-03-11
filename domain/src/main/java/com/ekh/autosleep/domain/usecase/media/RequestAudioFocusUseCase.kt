package com.ekh.autosleep.domain.usecase.media

import com.ekh.autosleep.domain.repository.MediaControlRepository

class RequestAudioFocusUseCase(private val mediaControlRepository: MediaControlRepository) {
    suspend operator fun invoke(): Result<Unit> = mediaControlRepository.requestAudioFocus()
}
