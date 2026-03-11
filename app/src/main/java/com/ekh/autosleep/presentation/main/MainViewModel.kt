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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val timerRepository: TimerRepository,
    private val startTimer: StartTimerUseCase,
    private val cancelTimer: CancelTimerUseCase,
    private val checkPermissions: CheckPermissionsUseCase,
) : ViewModel() {

    val timerState: StateFlow<TimerState> = timerRepository.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TimerState.Idle)

    val permissionState: PermissionState get() = checkPermissions()

    fun startTimer(durationMs: Long) {
        startTimer(TimerConfig(durationMs))
        context.startForegroundService(Intent(context, TimerService::class.java))
    }

    fun cancelTimer() {
        cancelTimer()
        context.stopService(Intent(context, TimerService::class.java))
    }
}
