package com.ekh.autosleep.domain.usecase.routine

import com.ekh.autosleep.domain.entity.Routine
import com.ekh.autosleep.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.Flow

class GetRoutinesUseCase(private val repository: RoutineRepository) {
    operator fun invoke(): Flow<List<Routine>> = repository.getAll()
}
