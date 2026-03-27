package com.ekh.autosleep.domain.repository

import com.ekh.autosleep.domain.entity.Routine
import kotlinx.coroutines.flow.Flow

interface RoutineRepository {
    fun getAll(): Flow<List<Routine>>
    suspend fun add(routine: Routine): Long
    suspend fun update(routine: Routine)
    suspend fun delete(routine: Routine)
}
