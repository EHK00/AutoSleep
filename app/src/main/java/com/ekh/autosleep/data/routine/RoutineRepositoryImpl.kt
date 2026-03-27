package com.ekh.autosleep.data.routine

import com.ekh.autosleep.data.routine.db.RoutineDao
import com.ekh.autosleep.data.routine.db.RoutineEntity
import com.ekh.autosleep.domain.entity.Routine
import com.ekh.autosleep.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoutineRepositoryImpl @Inject constructor(
    private val dao: RoutineDao,
) : RoutineRepository {

    override fun getAll(): Flow<List<Routine>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun add(routine: Routine): Long =
        dao.insert(routine.toEntity())

    override suspend fun update(routine: Routine) =
        dao.update(routine.toEntity())

    override suspend fun delete(routine: Routine) =
        dao.delete(routine.toEntity())
}

private fun RoutineEntity.toDomain() = Routine(
    id = id,
    hour = hour,
    minute = minute,
    days = if (days.isBlank()) emptySet() else days.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet(),
    label = label,
    isEnabled = isEnabled,
)

private fun Routine.toEntity() = RoutineEntity(
    id = id,
    hour = hour,
    minute = minute,
    days = days.sorted().joinToString(","),
    label = label,
    isEnabled = isEnabled,
)
