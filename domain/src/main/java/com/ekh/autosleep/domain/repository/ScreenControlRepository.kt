package com.ekh.autosleep.domain.repository

interface ScreenControlRepository {
    suspend fun lockScreen(): Result<Unit>
    suspend fun shortenScreenTimeout(): Result<Unit>
    suspend fun restoreScreenTimeout(): Result<Unit>
}
