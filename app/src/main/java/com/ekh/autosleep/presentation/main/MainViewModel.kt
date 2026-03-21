package com.ekh.autosleep.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekh.autosleep.domain.entity.PermissionState
import com.ekh.autosleep.domain.entity.TimerConfig
import com.ekh.autosleep.domain.entity.TimerState
import com.ekh.autosleep.domain.repository.TimerRepository
import com.ekh.autosleep.domain.usecase.permission.CheckPermissionsUseCase
import com.ekh.autosleep.domain.usecase.timer.CancelTimerUseCase
import com.ekh.autosleep.domain.usecase.timer.StartTimerUseCase
import com.ekh.autosleep.service.TimerServiceController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * 메인 화면의 상태를 관리하는 ViewModel.
 * 타이머 상태([timerState])와 권한 상태([permissionState])를 UI에 노출하며,
 * 타이머 시작/취소 시 [TimerServiceController]를 통해 포그라운드 서비스를 제어한다.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val timerRepository: TimerRepository,
    private val startTimer: StartTimerUseCase,
    private val cancelTimer: CancelTimerUseCase,
    private val checkPermissions: CheckPermissionsUseCase,
    private val timerServiceController: TimerServiceController,
) : ViewModel() {

    /**
     * 현재 타이머 상태 스트림.
     * 구독자가 없는 상태로 5초가 지나면 업스트림 수집을 중단한다([SharingStarted.WhileSubscribed]).
     */
    val timerState: StateFlow<TimerState> = timerRepository.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TimerState.Idle)

    private val _permissionState = MutableStateFlow(checkPermissions())

    /** 현재 권한 허용 상태 스트림. [refreshPermissions] 호출 시 갱신된다. */
    val permissionState: StateFlow<PermissionState> = _permissionState.asStateFlow()

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
     * 권한 상태를 현재 시점으로 다시 조회하여 갱신한다.
     * 설정 화면에서 돌아올 때([Lifecycle.Event.ON_RESUME]) UI가 호출한다.
     */
    fun refreshPermissions() {
        _permissionState.value = checkPermissions()
    }

    /**
     * 타이머를 시작하고 [TimerServiceController]를 통해 포그라운드 서비스를 실행한다.
     * @param durationMs 카운트다운할 시간 (밀리초).
     */
    fun startTimer(durationMs: Long) {
        _timerDigits.value = emptyList()
        startTimer.invoke(TimerConfig(durationMs))
        timerServiceController.start(durationMs)
    }

    /**
     * 타이머를 취소하고 [TimerServiceController]를 통해 포그라운드 서비스를 종료한다.
     */
    fun cancelTimer() {
        cancelTimer.invoke()
        timerServiceController.cancel()
    }
}
