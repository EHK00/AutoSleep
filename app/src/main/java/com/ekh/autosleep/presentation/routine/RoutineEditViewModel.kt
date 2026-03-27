package com.ekh.autosleep.presentation.routine

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekh.autosleep.domain.entity.Routine
import com.ekh.autosleep.domain.usecase.routine.AddRoutineUseCase
import com.ekh.autosleep.domain.usecase.routine.GetRoutinesUseCase
import com.ekh.autosleep.domain.usecase.routine.UpdateRoutineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoutineEditUiState(
    val days: Set<Int> = emptySet(),
    val label: String = "",
    val isNew: Boolean = true,
)

@HiltViewModel
class RoutineEditViewModel @Inject constructor(
    private val addRoutine: AddRoutineUseCase,
    private val updateRoutine: UpdateRoutineUseCase,
    private val getRoutines: GetRoutinesUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val routineId: Long? = (savedStateHandle.get<Long>("routineId")
        ?: savedStateHandle.get<String>("routineId")?.toLongOrNull())
        ?.takeIf { it >= 0 }

    private val _uiState = MutableStateFlow(RoutineEditUiState(isNew = routineId == null))
    val uiState: StateFlow<RoutineEditUiState> = _uiState.asStateFlow()

    /** HHMM 순서의 digit 목록 (최대 4자리). 초는 사용하지 않음. */
    private val _digits = MutableStateFlow<List<Int>>(emptyList())
    val digits: StateFlow<List<Int>> = _digits.asStateFlow()

    init {
        if (routineId != null) {
            viewModelScope.launch {
                val routine = getRoutines().first().find { it.id == routineId }
                if (routine != null) {
                    _uiState.update {
                        RoutineEditUiState(
                            days = routine.days,
                            label = routine.label,
                            isNew = false,
                        )
                    }
                    _digits.value = hourMinuteToDigits(routine.hour, routine.minute)
                }
            }
        }
    }

    fun onDigit(digit: Int) {
        val current = _digits.value
        if (current.size >= 4) return
        if (current.isEmpty() && digit == 0) return
        val next = current + digit
        val padded = List(4 - next.size) { 0 } + next
        val h = padded[0] * 10 + padded[1]
        if (h > 23) return
        _digits.update { next }
    }

    fun onDoubleZero() {
        repeat(2) { onDigit(0) }
    }

    fun onDelete() {
        _digits.update { if (it.isEmpty()) it else it.dropLast(1) }
    }

    fun toggleDay(day: Int) {
        _uiState.update { state ->
            val updated = if (day in state.days) state.days - day else state.days + day
            state.copy(days = updated)
        }
    }

    fun setLabel(label: String) {
        _uiState.update { it.copy(label = label) }
    }

    suspend fun save(): Boolean {
        val state = _uiState.value
        val (h, m) = digitsToHourMinute(_digits.value)
        return try {
            val routine = Routine(
                id = routineId ?: 0,
                hour = h,
                minute = m,
                days = state.days,
                label = state.label,
                isEnabled = true,
            )
            if (state.isNew) addRoutine(routine) else updateRoutine(routine)
            true
        } catch (e: Exception) {
            false
        }
    }
}

/** digit 목록을 (hour, minute) 쌍으로 변환. digits는 최대 4자리 HHMM. */
internal fun digitsToHourMinute(digits: List<Int>): Pair<Int, Int> {
    val d = List(4 - digits.size) { 0 } + digits
    return d[0] * 10 + d[1] to d[2] * 10 + d[3]
}

/** (hour, minute)를 digit 목록으로 변환. 앞자리 0은 생략. */
private fun hourMinuteToDigits(hour: Int, minute: Int): List<Int> {
    val str = "%02d%02d".format(hour, minute)
    val digits = str.map { it.digitToInt() }
    return digits.dropWhile { it == 0 }.ifEmpty { emptyList() }
}
