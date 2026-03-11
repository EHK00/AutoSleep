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

@Singleton
class TimerRepositoryImpl @Inject constructor() : TimerRepository {

    private val _state = MutableStateFlow<TimerState>(TimerState.Idle)
    override val state: StateFlow<TimerState> = _state.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var timerJob: Job? = null

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

    override fun cancel() {
        timerJob?.cancel()
        timerJob = null
        _state.value = TimerState.Cancelled
    }

    companion object {
        private const val TICK_INTERVAL_MS = 1_000L
    }
}
