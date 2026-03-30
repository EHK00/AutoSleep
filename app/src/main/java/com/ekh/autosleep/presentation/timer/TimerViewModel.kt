package com.ekh.autosleep.presentation.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekh.autosleep.data.settings.SettingsRepository
import com.ekh.autosleep.data.settings.TimeFormat
import com.ekh.autosleep.domain.entity.TimerConfig
import com.ekh.autosleep.domain.entity.TimerState
import com.ekh.autosleep.domain.repository.TimerPresetRepository
import com.ekh.autosleep.domain.repository.TimerRepository
import com.ekh.autosleep.domain.usecase.analytics.RecordTimerLogUseCase
import com.ekh.autosleep.domain.usecase.timer.CancelTimerUseCase
import com.ekh.autosleep.domain.usecase.timer.StartTimerUseCase
import com.ekh.autosleep.service.TimerServiceController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 타이머 화면의 상태를 관리하는 ViewModel.
 * 타이머 상태([timerState]), 키패드 입력([timerDigits]), 프리셋([savedPresets])을 UI에 노출하며,
 * 타이머 시작/취소 시 [TimerServiceController]를 통해 포그라운드 서비스를 제어한다.
 */
@HiltViewModel
class TimerViewModel @Inject constructor(
    private val timerRepository: TimerRepository,
    private val startTimer: StartTimerUseCase,
    private val cancelTimer: CancelTimerUseCase,
    private val timerServiceController: TimerServiceController,
    private val timerPresetRepository: TimerPresetRepository,
    private val settingsRepository: SettingsRepository,
    private val recordTimerLog: RecordTimerLogUseCase,
) : ViewModel() {

    /**
     * 현재 타이머 상태 스트림.
     * 구독자가 없는 상태로 5초가 지나면 업스트림 수집을 중단한다([SharingStarted.WhileSubscribed]).
     */
    val timerState: StateFlow<TimerState> = timerRepository.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TimerState.Idle)

    /** 저장된 타이머 프리셋 목록 스트림. */
    val savedPresets: StateFlow<List<Long>> = timerPresetRepository.presets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** 프리셋 시간 표시 형식 스트림. */
    val timeFormat: StateFlow<TimeFormat> = settingsRepository.timeFormat
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TimeFormat.KOREAN)

    private val _timerDigits = MutableStateFlow<List<Int>>(emptyList())

    /**
     * 키패드로 입력 중인 타이머 숫자 목록 (최대 6자리).
     * 오른쪽 끝부터 채워지며 [H1,H2,M1,M2,S1,S2] 순서로 해석된다.
     */
    val timerDigits: StateFlow<List<Int>> = _timerDigits.asStateFlow()

    /**
     * 타이머 숫자를 하나 추가한다. 이미 6자리이거나 선행 0이면 무시한다.
     * @param digit 0–9 사이의 숫자.
     */
    fun onTimerDigit(digit: Int) {
        val current = _timerDigits.value
        if (current.size >= 6) return
        if (current.isEmpty() && digit == 0) return
        _timerDigits.value = current + digit
    }

    /**
     * 타이머 숫자 "00"을 추가한다.
     * 아직 아무 숫자도 없거나 남은 슬롯이 없으면 무시한다.
     */
    fun onTimerDoubleZero() {
        val current = _timerDigits.value
        if (current.isEmpty()) return
        val slotsLeft = 6 - current.size
        _timerDigits.value = when {
            slotsLeft >= 2 -> current + 0 + 0
            slotsLeft == 1 -> current + 0
            else -> current
        }
    }

    /** 마지막으로 입력한 타이머 숫자를 제거한다. */
    fun onTimerDelete() {
        if (_timerDigits.value.isEmpty()) return
        _timerDigits.value = _timerDigits.value.dropLast(1)
    }

    /**
     * 타이머를 시작하고 [TimerServiceController]를 통해 포그라운드 서비스를 실행한다.
     * @param durationMs 카운트다운할 시간 (밀리초).
     */
    fun startTimer(durationMs: Long) {
        val startedAt = System.currentTimeMillis()
        _timerDigits.value = emptyList()
        startTimer.invoke(TimerConfig(durationMs))
        timerServiceController.start(durationMs)
        viewModelScope.launch { recordTimerLog(startedAt, durationMs) }
    }

    /**
     * 타이머를 취소하고 [TimerServiceController]를 통해 포그라운드 서비스를 종료한다.
     */
    fun cancelTimer() {
        cancelTimer.invoke()
        timerServiceController.cancel()
    }

    /**
     * 현재 입력된 시간을 프리셋으로 저장한다.
     * @param durationMs 저장할 타이머 시간 (밀리초).
     */
    fun savePreset(durationMs: Long) {
        if (durationMs > 0) timerPresetRepository.save(durationMs)
    }

    /**
     * 프리셋을 선택하여 키패드 입력 상태를 해당 시간으로 설정한다.
     * @param durationMs 선택한 프리셋 시간 (밀리초).
     */
    fun selectPreset(durationMs: Long) {
        _timerDigits.value = durationMs.toDigits()
    }

    /**
     * 프리셋을 삭제한다.
     * @param durationMs 삭제할 프리셋 시간 (밀리초).
     */
    fun deletePreset(durationMs: Long) {
        timerPresetRepository.delete(durationMs)
    }

    /** 밀리초를 키패드 숫자 목록(최대 6자리)으로 변환한다. */
    private fun Long.toDigits(): List<Int> {
        val totalSec = this / 1000
        val h = totalSec / 3600
        val m = (totalSec % 3600) / 60
        val s = totalSec % 60
        val str = "%02d%02d%02d".format(h, m, s).trimStart('0')
        return if (str.isEmpty()) emptyList() else str.map { it.digitToInt() }
    }
}
