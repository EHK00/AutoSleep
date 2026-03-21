package com.ekh.autosleep.data.preset

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ekh.autosleep.data.preset.TimerPresetRepositoryImpl.Companion.MAX_PRESETS
import com.ekh.autosleep.domain.repository.TimerPresetRepository
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

/**
 * [TimerPresetRepository]의 DataStore 기반 구현체.
 * 프리셋은 최대 [MAX_PRESETS]개까지 저장되며 최신 저장 순으로 유지된다.
 * 앱을 재실행해도 데이터가 유지된다.
 */
@Singleton
class TimerPresetRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : TimerPresetRepository {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override val presets: StateFlow<List<Long>> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs -> prefs.parsePresets() }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override fun save(durationMs: Long) {
        scope.launch {
            dataStore.edit { prefs ->
                val current = prefs.parsePresets()
                if (current.contains(durationMs)) return@edit
                prefs[KEY_PRESETS] = (listOf(durationMs) + current)
                    .take(MAX_PRESETS)
                    .joinToString(",")
            }
        }
    }

    override fun delete(durationMs: Long) {
        scope.launch {
            dataStore.edit { prefs ->
                prefs[KEY_PRESETS] = prefs.parsePresets()
                    .filter { it != durationMs }
                    .joinToString(",")
            }
        }
    }

    private fun Preferences.parsePresets(): List<Long> =
        this[KEY_PRESETS]?.split(",")?.mapNotNull { it.toLongOrNull() } ?: emptyList()

    companion object {
        private val KEY_PRESETS = stringPreferencesKey("presets")
        private const val MAX_PRESETS = 10
    }
}
