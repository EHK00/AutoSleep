package com.ekh.autosleep.data.timer

import com.ekh.autosleep.domain.entity.TimerConfig
import com.ekh.autosleep.domain.entity.TimerState
import com.ekh.autosleep.domain.repository.TimerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [TimerRepository]의 코루틴 기반 구현체.
 * 앱 전역 싱글턴으로 관리되며, 1초 간격으로 남은 시간을 갱신하고
 * 만료 시 [TimerState.Expired]를 방출하여 [TimerService]가 수면 시퀀스를 시작하도록 신호를 보낸다.
 */
@Singleton
class TimerRepositoryImpl @Inject constructor() : TimerRepository {

    private val _state = MutableStateFlow<TimerState>(TimerState.Idle)
    override val state: StateFlow<TimerState> = _state.asStateFlow()

    /** 타이머 틱을 실행하는 코루틴 스코프. [SupervisorJob]으로 개별 타이머 실패가 전파되지 않도록 격리한다. */
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var timerJob: Job? = null

    /**
     * 새 타이머를 시작한다. 기존 타이머가 실행 중이면 먼저 취소한다.
     * 시작 시각을 기준으로 경과 시간을 계산하여 남은 시간을 1초마다 갱신한다.
     */
    override fun start(config: TimerConfig) {
        timerJob?.cancel()
        timerJob = scope.launch {
            val startTime = System.currentTimeMillis()
            _state.value = TimerState.Running(config.durationMs)
            while (true) {
                delay(TICK_INTERVAL_MS)
                val elapsed = System.currentTimeMillis() - startTime
                val remaining = config.durationMs - elapsed
                if (remaining <= 0) {
                    _state.value = TimerState.Expired
                    break
                }
                _state.value = TimerState.Running(remaining)
            }
        }
    }

    /** 진행 중인 타이머 코루틴을 중단하고 상태를 [TimerState.Cancelled]로 전환한다. */
    override fun cancel() {
        timerJob?.cancel()
        timerJob = null
        _state.value = TimerState.Cancelled
    }

    companion object {
        /** 남은 시간 갱신 간격 (밀리초). */
        private const val TICK_INTERVAL_MS = 1_000L
    }
}
