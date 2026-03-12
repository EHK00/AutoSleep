package com.ekh.autosleep.domain.usecase.screen

import com.ekh.autosleep.domain.repository.ScreenControlRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertTrue

class LockScreenUseCaseTest {

    private val screenControlRepository: ScreenControlRepository = mockk()
    private val useCase = LockScreenUseCase(screenControlRepository)

    @Test
    fun `lockScreen 성공 시 success를 반환한다`() = runTest {
        coEvery { screenControlRepository.lockScreen() } returns Result.success(Unit)

        val result = useCase()

        coVerify(exactly = 1) { screenControlRepository.lockScreen() }
        assertTrue(result.isSuccess)
    }

    @Test
    fun `lockScreen 실패 시 failure를 전파한다`() = runTest {
        val error = IllegalStateException("접근성 서비스 미연결")
        coEvery { screenControlRepository.lockScreen() } returns Result.failure(error)

        val result = useCase()

        coVerify(exactly = 1) { screenControlRepository.lockScreen() }
        assertTrue(result.isFailure)
    }
}
