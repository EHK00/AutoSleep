package com.ekh.autosleep.presentation.analytics

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekh.autosleep.R
import com.ekh.autosleep.data.settings.SettingsRepository
import com.ekh.autosleep.domain.entity.TimerLog
import com.ekh.autosleep.domain.usecase.analytics.GetTimerLogsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class DayPoint(
    /** 0..6, 0 = 7일 전, 6 = 마지막 기록일 */
    val slotIndex: Int,
    /** 원시 분 (0~1439) */
    val minuteOfDay: Int,
)

data class AnalyticsUiState(
    val chartPoints: List<DayPoint> = emptyList(),
    /** 마지막 기록 기준 7일의 X축 레이블 (요일 문자, 7개) */
    val xAxisLabels: List<String> = emptyList(),
    val recentAverageMinute: Int? = null,
    val totalCount: Int = 0,
    val sleepWindowStartHour: Int = 22,
    val sleepWindowEndHour: Int = 4,
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    getLogs: GetTimerLogsUseCase,
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    val uiState: StateFlow<AnalyticsUiState> = combine(
        getLogs(),
        settingsRepository.sleepWindowStartHour,
        settingsRepository.sleepWindowEndHour,
    ) { logs, startHour, endHour ->
        buildUiState(logs, startHour, endHour)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AnalyticsUiState())

    fun setSleepWindow(startHour: Int, endHour: Int) {
        settingsRepository.setSleepWindow(startHour, endHour)
    }

    private fun buildUiState(logs: List<TimerLog>, startHour: Int, endHour: Int): AnalyticsUiState {
        val zone = ZoneId.systemDefault()

        // 수면 시간 범위에 해당하는 로그만 필터
        val sleepLogs = logs.filter { log ->
            val hour = Instant.ofEpochMilli(log.startedAt).atZone(zone).hour
            isInSleepWindow(hour, startHour, endHour)
        }

        // 마지막 기록일 기준 (없으면 오늘), 수면 창 기반 경계 적용
        val lastLogDate: LocalDate = if (sleepLogs.isNotEmpty()) {
            sleepDate(sleepLogs.maxOf { it.startedAt }, zone, startHour, endHour)
        } else {
            LocalDate.now(zone)
        }
        val windowStart = lastLogDate.minusDays(6)

        // X축 레이블: windowStart ~ lastLogDate 7일의 요일
        val dayLabels = listOf(
            context.getString(R.string.day_mon),
            context.getString(R.string.day_tue),
            context.getString(R.string.day_wed),
            context.getString(R.string.day_thu),
            context.getString(R.string.day_fri),
            context.getString(R.string.day_sat),
            context.getString(R.string.day_sun),
        )
        val xAxisLabels = (0..6).map { offset ->
            dayLabels[windowStart.plusDays(offset.toLong()).dayOfWeek.value - 1]
        }

        // 7일 윈도우 내 로그 필터 (수면 창 기반 경계 적용)
        val windowLogs = sleepLogs.filter { log ->
            val logDate = sleepDate(log.startedAt, zone, startHour, endHour)
            !logDate.isBefore(windowStart) && !logDate.isAfter(lastLogDate)
        }

        val chartPoints = windowLogs
            .groupBy { log -> sleepDate(log.startedAt, zone, startHour, endHour) }
            .map { (date, logsOnDay) ->
                val lastLog = logsOnDay.maxBy { it.startedAt }
                val dt = Instant.ofEpochMilli(lastLog.startedAt).atZone(zone)
                val slotIndex = ChronoUnit.DAYS.between(windowStart, date).toInt()
                DayPoint(slotIndex = slotIndex, minuteOfDay = dt.hour * 60 + dt.minute)
            }
            .sortedBy { it.slotIndex }

        val recentAverageMinute = if (chartPoints.isNotEmpty()) {
            // endHour 이전(새벽 쪽)은 다음날로 취급해 자정 전후 평균이 자연스럽게 계산되도록 함
            val adjusted = chartPoints.map { pt ->
                if (pt.minuteOfDay < endHour * 60) pt.minuteOfDay + 24 * 60 else pt.minuteOfDay
            }
            adjusted.average().toInt() % (24 * 60)
        } else null

        return AnalyticsUiState(
            chartPoints = chartPoints,
            xAxisLabels = xAxisLabels,
            recentAverageMinute = recentAverageMinute,
            totalCount = sleepLogs.size,
            sleepWindowStartHour = startHour,
            sleepWindowEndHour = endHour,
        )
    }

    companion object {
        /**
         * 수면 창 기반 하루 경계로 날짜를 계산. 새벽 기록은 전날로 귀속.
         * 경계 시각 = endHour + (startHour까지 깨어있는 시간의 절반)
         * 예) 22~4시 → 깨어있는 시간 4~22시 → 경계 = 4 + (22-4)/2 = 13시
         */
        fun sleepDate(epochMs: Long, zone: ZoneId, startHour: Int, endHour: Int): LocalDate {
            val awakeStart = endHour
            val awakeEnd = if (startHour > endHour) startHour else startHour + 24
            val boundaryHour = (awakeStart + (awakeEnd - awakeStart) / 2) % 24
            val boundaryMs = boundaryHour * 60 * 60 * 1000L
            return Instant.ofEpochMilli(epochMs - boundaryMs).atZone(zone).toLocalDate()
        }

        /** startHour~endHour 범위 (자정 넘김 지원) 에 hour가 포함되는지 확인 */
        fun isInSleepWindow(hour: Int, startHour: Int, endHour: Int): Boolean =
            if (startHour <= endHour) {
                hour in startHour..endHour
            } else {
                // 자정을 넘기는 범위 (예: 22~4)
                hour >= startHour || hour <= endHour
            }
    }
}
