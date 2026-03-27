package com.ekh.autosleep.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekh.autosleep.data.settings.SettingsRepository
import com.ekh.autosleep.data.settings.TimeFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val timeFormat: StateFlow<TimeFormat> = settingsRepository.timeFormat
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TimeFormat.KOREAN)

    fun setTimeFormat(format: TimeFormat) = settingsRepository.setTimeFormat(format)
}
