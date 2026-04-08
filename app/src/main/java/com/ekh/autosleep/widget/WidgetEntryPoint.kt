package com.ekh.autosleep.widget

import com.ekh.autosleep.data.settings.SettingsRepository
import com.ekh.autosleep.domain.repository.TimerPresetRepository
import com.ekh.autosleep.domain.repository.TimerRepository
import com.ekh.autosleep.domain.usecase.timer.StartTimerUseCase
import com.ekh.autosleep.service.TimerServiceController
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun timerPresetRepository(): TimerPresetRepository
    fun timerRepository(): TimerRepository
    fun startTimerUseCase(): StartTimerUseCase
    fun timerServiceController(): TimerServiceController
    fun settingsRepository(): SettingsRepository
}
