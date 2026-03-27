package com.ekh.autosleep.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.ekh.autosleep.domain.entity.TimerConfig
import dagger.hilt.android.EntryPointAccessors

class StartPresetAction : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val durationMs = parameters[durationKey] ?: return
        val ep = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java,
        )
        ep.startTimerUseCase().invoke(TimerConfig(durationMs))
        ep.timerServiceController().start(durationMs)
    }

    companion object {
        val durationKey = ActionParameters.Key<Long>("duration_ms")
    }
}
