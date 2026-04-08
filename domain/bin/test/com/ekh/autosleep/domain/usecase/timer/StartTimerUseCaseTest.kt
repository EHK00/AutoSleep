package com.ekh.autosleep.domain.usecase.timer

import com.ekh.autosleep.domain.entity.TimerConfig
import com.ekh.autosleep.domain.entity.TimerState
import com.ekh.autosleep.domain.repository.TimerRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test
import kotlin.test.assertFailsWith

class StartTimerUseCaseTest {

    private val timerRepository: TimerRepository = mockk(relaxed = true) {
        every { state } returns MutableStateFlow(TimerState.Idle)
    }
    private val useCase = StartTimerUseCase(timerRepository)

    @Test
    fun `유효한 durationMs로 호출하면 repository start가 실행된다`() {
        val config = TimerConfig(durationMs = 5 * 60_000L)

        useCase(config)

        verify(exactly = 1) { timerRepository.start(config) }
    }

    @Test
    fun `durationMs가 0이면 예외가 발생한다`() {
        assertFailsWith<IllegalArgumentException> {
            useCase(TimerConfig(durationMs = 0L))
        }
    }

    @Test
    fun `durationMs가 음수이면 예외가 발생한다`() {
        assertFailsWith<IllegalArgumentException> {
            useCase(TimerConfig(durationMs = -1_000L))
        }
    }
}
