package com.ekh.autosleep.presentation.main

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekh.autosleep.domain.entity.PermissionState
import com.ekh.autosleep.domain.entity.TimerConfig
import com.ekh.autosleep.domain.entity.TimerState
import com.ekh.autosleep.domain.repository.TimerRepository
import com.ekh.autosleep.domain.usecase.permission.CheckPermissionsUseCase
import com.ekh.autosleep.domain.usecase.timer.CancelTimerUseCase
import com.ekh.autosleep.domain.usecase.timer.StartTimerUseCase
import com.ekh.autosleep.service.TimerService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 메인 화면의 상태를 관리하는 ViewModel.
 * 타이머 상태([timerState])와 권한 상태([permissionState])를 UI에 노출하며,
 * 타이머 시작/취소 시 [TimerService]의 포그라운드 서비스를 함께 제어한다.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val timerRepository: TimerRepository,
    private val startTimer: StartTimerUseCase,
    private val cancelTimer: CancelTimerUseCase,
    private val checkPermissions: CheckPermissionsUseCase,
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

    /**
     * 권한 상태를 현재 시점으로 다시 조회하여 갱신한다.
     * 설정 화면에서 돌아올 때([Lifecycle.Event.ON_RESUME]) UI가 호출한다.
     */
    fun refreshPermissions() {
        _permissionState.value = checkPermissions()
    }

    /**
     * 타이머를 시작하고 [TimerService] 포그라운드 서비스를 실행한다.
     * @param durationMs 카운트다운할 시간 (밀리초).
     */
    fun startTimer(durationMs: Long) {
        startTimer(TimerConfig(durationMs))
        context.startForegroundService(Intent(context, TimerService::class.java))
    }

    /**
     * 타이머를 취소하고 [TimerService] 포그라운드 서비스를 종료한다.
     */
    fun cancelTimer() {
        cancelTimer()
        context.stopService(Intent(context, TimerService::class.java))
    }
}
