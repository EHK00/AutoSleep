package com.ekh.autosleep.domain.usecase.routine

import com.ekh.autosleep.domain.entity.Routine
import com.ekh.autosleep.domain.repository.RoutineRepository

class ToggleRoutineUseCase(private val repository: RoutineRepository) {
    suspend operator fun invoke(routine: Routine) =
        repository.update(routine.copy(isEnabled = !routine.isEnabled))
}
