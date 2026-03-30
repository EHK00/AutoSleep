package com.ekh.autosleep.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override val timeFormat: StateFlow<TimeFormat> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            when (prefs[KEY_TIME_FORMAT]) {
                TimeFormat.CLOCK.name -> TimeFormat.CLOCK
                else -> TimeFormat.KOREAN
            }
        }
        .stateIn(scope, SharingStarted.Eagerly, TimeFormat.KOREAN)

    override fun setTimeFormat(format: TimeFormat) {
        scope.launch {
            dataStore.edit { prefs ->
                prefs[KEY_TIME_FORMAT] = format.name
            }
        }
    }

    override val sleepWindowStartHour: StateFlow<Int> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs -> prefs[KEY_SLEEP_WINDOW_START] ?: DEFAULT_SLEEP_WINDOW_START }
        .stateIn(scope, SharingStarted.Eagerly, DEFAULT_SLEEP_WINDOW_START)

    override val sleepWindowEndHour: StateFlow<Int> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs -> prefs[KEY_SLEEP_WINDOW_END] ?: DEFAULT_SLEEP_WINDOW_END }
        .stateIn(scope, SharingStarted.Eagerly, DEFAULT_SLEEP_WINDOW_END)

    override fun setSleepWindow(startHour: Int, endHour: Int) {
        scope.launch {
            dataStore.edit { prefs ->
                prefs[KEY_SLEEP_WINDOW_START] = startHour
                prefs[KEY_SLEEP_WINDOW_END] = endHour
            }
        }
    }

    companion object {
        private val KEY_TIME_FORMAT = stringPreferencesKey("time_format")
        private val KEY_SLEEP_WINDOW_START = intPreferencesKey("sleep_window_start")
        private val KEY_SLEEP_WINDOW_END = intPreferencesKey("sleep_window_end")
        private const val DEFAULT_SLEEP_WINDOW_START = 21
        private const val DEFAULT_SLEEP_WINDOW_END = 6
    }
}
