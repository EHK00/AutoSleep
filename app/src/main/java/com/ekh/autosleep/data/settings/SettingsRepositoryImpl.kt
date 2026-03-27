package com.ekh.autosleep.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
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

    companion object {
        private val KEY_TIME_FORMAT = stringPreferencesKey("time_format")
    }
}
