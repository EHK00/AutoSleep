package com.ekh.autosleep.presentation.timer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ekh.autosleep.R
import com.ekh.autosleep.data.settings.TimeFormat
import com.ekh.autosleep.ui.theme.AutoSleepTheme

/**
 * 타이머 실행 중 상태 화면.
 * 남은 시간과 취소 버튼을 전체 화면으로 표시한다.
 * [timeFormat] 설정에 따라 시계 형식(HH:MM:SS) 또는 한국어 형식(N시간 N분 N초)으로 표시한다.
 */
@Composable
fun TimerRunningScreen(
    remainingMs: Long,
    timeFormat: TimeFormat,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val h = remainingMs / 3_600_000
    val m = (remainingMs % 3_600_000) / 60_000
    val s = (remainingMs % 60_000) / 1_000

    val hourStr = stringResource(R.string.duration_hour)
    val minStr = stringResource(R.string.duration_minute)
    val secStr = stringResource(R.string.duration_second)

    val timeText = when (timeFormat) {
        TimeFormat.CLOCK -> if (h > 0) "%02d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
        TimeFormat.KOREAN -> buildString {
            if (h > 0) append("${h}${hourStr} ")
            if (m > 0) append("${m}${minStr} ")
            append("${s}${secStr}")
        }.trim()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.timer_running_label),
            fontSize = 14.sp,
            color = Color.Gray,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = timeText,
            fontSize = 56.sp,
            fontWeight = FontWeight.Light,
        )
        Spacer(modifier = Modifier.height(48.dp))
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.cancel), fontSize = 18.sp)
        }
    }
}

@Preview(name = "실행 중 - 시계 형식", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun TimerRunningClockPreview() {
    AutoSleepTheme {
        TimerRunningScreen(
            remainingMs = 5_430_000L,
            timeFormat = TimeFormat.CLOCK,
            onCancel = {},
        )
    }
}

@Preview(name = "실행 중 - 한국어 형식", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun TimerRunningKoreanPreview() {
    AutoSleepTheme {
        TimerRunningScreen(
            remainingMs = 5_430_000L,
            timeFormat = TimeFormat.KOREAN,
            onCancel = {},
        )
    }
}
