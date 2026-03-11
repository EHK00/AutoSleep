package com.ekh.autosleep.domain.usecase.media

import com.ekh.autosleep.domain.repository.MediaControlRepository

class PauseAllMediaUseCase(private val mediaControlRepository: MediaControlRepository) {
    suspend operator fun invoke(): Result<Int> = mediaControlRepository.pauseAll()
}
