package com.ekh.autosleep.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.ekh.autosleep.data.settings.TimeFormat
import com.ekh.autosleep.domain.entity.TimerState
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine

private data class WidgetData(
    val presets: List<Long>,
    val timeFormat: TimeFormat,
    val timerState: TimerState,
)

class SleepTimerWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val ep = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java,
        )

        val widgetDataFlow: Flow<WidgetData> = combine(
            ep.timerPresetRepository().presets,
            ep.settingsRepository().timeFormat,
            ep.timerRepository().state,
        ) { presets: List<Long>, timeFormat: TimeFormat, timerState: TimerState ->
            WidgetData(presets, timeFormat, timerState)
        }

        widgetDataFlow.collectLatest { data ->
            provideContent {
                GlanceTheme {
                    Box(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .cornerRadius(20.dp)
                            .background(Color(0x1aFFFFFF))
                            .padding(1.dp),
                    ) {
                        Column(
                            modifier = GlanceModifier
                                .fillMaxSize()
                                .cornerRadius(20.dp)
                                .background(Color(0x1a000000))
                                .padding(8.dp),
                        ) {
                            Text(
                                text = context.getString(com.ekh.autosleep.R.string.widget_title),
                                maxLines = 1,
                                style = TextStyle(
                                    color = GlanceTheme.colors.onSurface,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                            )
                            Spacer(modifier = GlanceModifier.height(6.dp))

                            // 타이머 실행 중일 때 남은 시간 표시
                            val running = data.timerState as? TimerState.Running
                            if (running != null) {
                                Text(
                                    text = "${formatDuration(running.remainingMs, data.timeFormat, context)} ${context.getString(com.ekh.autosleep.R.string.widget_remaining_suffix)}",
                                    style = TextStyle(
                                        color = GlanceTheme.colors.primary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                    ),
                                )
                                Spacer(modifier = GlanceModifier.height(6.dp))
                            }

                            if (data.presets.isEmpty()) {
                                Text(
                                    text = context.getString(com.ekh.autosleep.R.string.widget_no_presets),
                                    style = TextStyle(
                                        color = GlanceTheme.colors.secondary,
                                        fontSize = 12.sp,
                                    ),
                                )
                            } else {
                                LazyColumn {
                                    items(data.presets) { durationMs ->
                                        Row(
                                            modifier = GlanceModifier
                                                .fillMaxWidth()
                                                .padding(vertical = 5.dp)
                                                .clickable(
                                                    actionRunCallback<StartPresetAction>(
                                                        actionParametersOf(
                                                            StartPresetAction.durationKey to durationMs,
                                                        ),
                                                    ),
                                                ),
                                        ) {
                                            Text(
                                                text = formatDuration(durationMs, data.timeFormat, context),
                                                style = TextStyle(
                                                    color = GlanceTheme.colors.onSurface,
                                                    fontSize = 12.sp,
                                                ),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatDuration(durationMs: Long, format: TimeFormat, context: Context): String {
    val totalSec = durationMs / 1000
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return when (format) {
        TimeFormat.CLOCK -> "%02d:%02d:%02d".format(h, m, s)
        TimeFormat.KOREAN -> buildString {
            val hourStr = context.getString(com.ekh.autosleep.R.string.duration_hour)
            val minStr = context.getString(com.ekh.autosleep.R.string.duration_minute)
            val secStr = context.getString(com.ekh.autosleep.R.string.duration_second)
            if (h > 0) append("${h}${hourStr}")
            if (m > 0) { if (isNotEmpty()) append(" "); append("${m}${minStr}") }
            if (s > 0) { if (isNotEmpty()) append(" "); append("${s}${secStr}") }
            if (isEmpty()) append("0${secStr}")
        }
    }
}
