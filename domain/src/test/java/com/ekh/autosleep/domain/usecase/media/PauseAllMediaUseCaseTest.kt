package com.ekh.autosleep.domain.usecase.media

import com.ekh.autosleep.domain.entity.MediaSessionInfo
import com.ekh.autosleep.domain.repository.MediaControlRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PauseAllMediaUseCaseTest {

    private val mediaControlRepository: MediaControlRepository = mockk()
    private val useCase = PauseAllMediaUseCase(mediaControlRepository)

    @Test
    fun `재생 중인 세션이 있으면 pauseAll을 호출하고 정지된 수를 반환한다`() = runTest {
        coEvery { mediaControlRepository.pauseAll() } returns Result.success(2)

        val result = useCase()

        coVerify(exactly = 1) { mediaControlRepository.pauseAll() }
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull())
    }

    @Test
    fun `세션이 없으면 0을 반환한다`() = runTest {
        coEvery { mediaControlRepository.pauseAll() } returns Result.success(0)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull())
    }

    @Test
    fun `pauseAll이 실패하면 Result_failure를 반환한다`() = runTest {
        val exception = RuntimeException("MediaSession unavailable")
        coEvery { mediaControlRepository.pauseAll() } returns Result.failure(exception)

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}
