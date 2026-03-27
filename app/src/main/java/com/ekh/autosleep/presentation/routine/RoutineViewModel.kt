package com.ekh.autosleep.presentation.routine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekh.autosleep.domain.entity.Routine
import com.ekh.autosleep.domain.usecase.routine.DeleteRoutineUseCase
import com.ekh.autosleep.domain.usecase.routine.GetRoutinesUseCase
import com.ekh.autosleep.domain.usecase.routine.ToggleRoutineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoutineViewModel @Inject constructor(
    private val getRoutines: GetRoutinesUseCase,
    private val toggleRoutine: ToggleRoutineUseCase,
    private val deleteRoutine: DeleteRoutineUseCase,
) : ViewModel() {

    val routines: StateFlow<List<Routine>> = getRoutines()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun toggle(routine: Routine) {
        viewModelScope.launch { toggleRoutine(routine) }
    }

    fun delete(routine: Routine) {
        viewModelScope.launch { deleteRoutine(routine) }
    }
}
