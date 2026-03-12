package com.ekh.autosleep.data.screen

import com.ekh.autosleep.domain.repository.ScreenControlRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [ScreenControlRepository]의 구현체.
 * [AccessibilityScreenSource]에 화면 잠금을 위임한다.
 */
@Singleton
class ScreenControlRepositoryImpl @Inject constructor(
    private val accessibilityScreenSource: AccessibilityScreenSource,
) : ScreenControlRepository {

    /** [AccessibilityScreenSource]에 화면 잠금을 위임한다. */
    override suspend fun lockScreen(): Result<Unit> =
        accessibilityScreenSource.lockScreen()
}
