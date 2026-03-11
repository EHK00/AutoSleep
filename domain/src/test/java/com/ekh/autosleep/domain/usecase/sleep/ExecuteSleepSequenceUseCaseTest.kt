package com.ekh.autosleep.domain.usecase.sleep

import com.ekh.autosleep.domain.entity.SleepResult
import com.ekh.autosleep.domain.usecase.media.PauseAllMediaUseCase
import com.ekh.autosleep.domain.usecase.media.RequestAudioFocusUseCase
import com.ekh.autosleep.domain.usecase.screen.LockScreenUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ExecuteSleepSequenceUseCaseTest {

    private val pauseAllMedia: PauseAllMediaUseCase = mockk()
    private val requestAudioFocus: RequestAudioFocusUseCase = mockk()
    private val lockScreen: LockScreenUseCase = mockk()
    private val useCase = ExecuteSleepSequenceUseCase(pauseAllMedia, requestAudioFocus, lockScreen)

    @Test
    fun `미디어 pause와 화면 잠금이 모두 성공하면 Success를 반환한다`() = runTest {
        coEvery { pauseAllMedia() } returns Result.success(3)
        coEvery { requestAudioFocus() } returns Result.success(Unit)
        coEvery { lockScreen() } returns Result.success(Unit)

        val result = useCase()

        assertIs<SleepResult.Success>(result)
        assertEquals(3, result.sessionsPaused)
    }

    @Test
    fun `화면 잠금이 실패하면 PartialSuccess를 반환한다`() = runTest {
        coEvery { pauseAllMedia() } returns Result.success(2)
        coEvery { requestAudioFocus() } returns Result.success(Unit)
        coEvery { lockScreen() } returns Result.failure(Exception("lock failed"))

        val result = useCase()

        assertIs<SleepResult.PartialSuccess>(result)
        assertEquals(2, result.sessionsPaused)
        assertEquals(false, result.screenLocked)
    }

    @Test
    fun `미디어 pause가 실패해도 화면 잠금이 성공하면 PartialSuccess를 반환한다`() = runTest {
        coEvery { pauseAllMedia() } returns Result.failure(Exception("no session"))
        coEvery { requestAudioFocus() } returns Result.success(Unit)
        coEvery { lockScreen() } returns Result.success(Unit)

        val result = useCase()

        assertIs<SleepResult.PartialSuccess>(result)
        assertEquals(true, result.screenLocked)
    }

    @Test
    fun `AudioFocus 실패는 전체 결과에 영향을 주지 않는다`() = runTest {
        coEvery { pauseAllMedia() } returns Result.success(1)
        coEvery { requestAudioFocus() } returns Result.failure(Exception("focus denied"))
        coEvery { lockScreen() } returns Result.success(Unit)

        val result = useCase()

        assertIs<SleepResult.Success>(result)
    }

    @Test
    fun `실행 순서는 pauseAllMedia → requestAudioFocus → lockScreen 이다`() = runTest {
        val callOrder = mutableListOf<String>()
        coEvery { pauseAllMedia() } coAnswers { callOrder.add("pause"); Result.success(0) }
        coEvery { requestAudioFocus() } coAnswers { callOrder.add("focus"); Result.success(Unit) }
        coEvery { lockScreen() } coAnswers { callOrder.add("lock"); Result.success(Unit) }

        useCase()

        assertEquals(listOf("pause", "focus", "lock"), callOrder)
    }
}
