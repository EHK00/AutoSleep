package com.ekh.autosleep.domain.usecase.routine

import com.ekh.autosleep.domain.entity.Routine
import com.ekh.autosleep.domain.repository.RoutineRepository

class UpdateRoutineUseCase(private val repository: RoutineRepository) {
    suspend operator fun invoke(routine: Routine) {
        require(routine.hour in 0..23) { "hour must be 0-23" }
        require(routine.minute in 0..59) { "minute must be 0-59" }
        repository.update(routine)
    }
}
