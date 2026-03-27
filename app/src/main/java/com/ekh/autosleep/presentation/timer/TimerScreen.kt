package com.ekh.autosleep.presentation.timer

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ekh.autosleep.data.settings.TimeFormat
import com.ekh.autosleep.ui.theme.AutoSleepTheme

/**
 * 타이머 입력 화면.
 * 키패드로 시간을 입력하고 프리셋을 관리하며 타이머를 시작한다.
 * [TimerViewModel]에서 상태를 구독하고 액션을 위임한다.
 */
@Composable
fun TimerScreen(
    viewModel: TimerViewModel = hiltViewModel(),
) {
    val timerDigits by viewModel.timerDigits.collectAsState()
    val savedPresets by viewModel.savedPresets.collectAsState()
    val timeFormat by viewModel.timeFormat.collectAsState()
    val durationMs = timerDigits.toDurationMs()
    var isTimerFocused by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = Color.Transparent)
                ) { isTimerFocused = !isTimerFocused },
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TimerInputDisplay(digits = timerDigits)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isTimerFocused) "완료" else "탭하여 수정",
                    fontSize = 12.sp,
                    color = Color.Gray,
                )
            }
        }

        Column(modifier = Modifier.wrapContentHeight()) {
            AnimatedContent(
                targetState = isTimerFocused,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "bottom_section",
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter,
            ) { focused ->
                if (focused) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        TimerInputPad(
                            onDigit = viewModel::onTimerDigit,
                            onDoubleZero = viewModel::onTimerDoubleZero,
                            onDelete = viewModel::onTimerDelete,
                        )
                    }
                } else {
                    PresetList(
                        presets = savedPresets,
                        timeFormat = timeFormat,
                        onSelect = viewModel::selectPreset,
                        onDelete = viewModel::deletePreset,
                        modifier = Modifier
                            .heightIn(max = 400.dp)
                            .fillMaxWidth(),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = { viewModel.savePreset(durationMs); isTimerFocused = false },
                    enabled = durationMs > 0,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("저장", fontSize = 18.sp)
                }
                Button(
                    onClick = { viewModel.startTimer(durationMs) },
                    enabled = durationMs > 0,
                    modifier = Modifier.weight(2f),
                ) {
                    Text("시작", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * 저장된 타이머 프리셋을 세로 스크롤 목록으로 표시하는 컴포저블.
 * 항목을 탭하면 해당 시간이 입력 상태로 설정되고, ✕ 버튼을 탭하면 삭제된다.
 * 저장된 프리셋이 없으면 안내 문구를 표시한다.
 */
@Composable
private fun PresetList(
    presets: List<Long>,
    timeFormat: TimeFormat,
    onSelect: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        if (presets.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("저장된 프리셋이 없습니다", color = Color.Gray, fontSize = 14.sp)
                }
            }
        } else {
            items(presets, key = { it }) { durationMs ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(durationMs) }
                        .padding(vertical = 14.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = formatPresetLabel(durationMs, timeFormat),
                        fontSize = 18.sp,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = { onDelete(durationMs) }) {
                        Text("✕", color = Color.Gray)
                    }
                }
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
            }
        }
    }
}

/**
 * 입력 중인 숫자 목록을 HH:MM:SS 형식으로 표시하는 컴포저블.
 * 아직 입력되지 않은 자리는 회색으로, 입력된 자리는 흰색으로 강조한다.
 */
@Composable
private fun TimerInputDisplay(digits: List<Int>) {
    val padded = List(6 - digits.size) { 0 } + digits
    val activeStart = 6 - digits.size

    val text = buildAnnotatedString {
        padded.forEachIndexed { i, d ->
            val color = if (i >= activeStart) Color.White else Color.Gray
            withStyle(SpanStyle(color = color)) { append(d.toString()) }
            when (i) {
                1 -> withStyle(SpanStyle(color = Color.White)) { append(":") }
                3 -> withStyle(SpanStyle(color = Color.White)) { append(":") }
            }
        }
    }

    Text(text = text, fontSize = 64.sp, fontWeight = FontWeight.Light)
}

/**
 * 갤럭시 시계 스타일 타이머 키패드 컴포저블.
 * 1–9, "00", 0, ⌫ 버튼을 3×4 그리드로 배치한다.
 */
@Composable
internal fun TimerInputPad(
    onDigit: (Int) -> Unit,
    onDoubleZero: () -> Unit,
    onDelete: () -> Unit,
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("00", "0", "⌫"),
    )

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                row.forEach { key ->
                    TextButton(
                        onClick = {
                            when (key) {
                                "⌫" -> onDelete()
                                "00" -> onDoubleZero()
                                else -> onDigit(key.toInt())
                            }
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(key, fontSize = 24.sp)
                    }
                }
            }
        }
    }
}

/** 밀리초를 설정된 형식의 문자열로 변환한다. */
private fun formatPresetLabel(durationMs: Long, format: TimeFormat): String {
    val totalSec = durationMs / 1000
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return when (format) {
        TimeFormat.CLOCK -> "%02d:%02d:%02d".format(h, m, s)
        TimeFormat.KOREAN -> buildString {
            if (h > 0) append("${h}시간")
            if (m > 0) {
                if (isNotEmpty()) append(" "); append("${m}분")
            }
            if (s > 0) {
                if (isNotEmpty()) append(" "); append("${s}초")
            }
            if (isEmpty()) append("0초")
        }
    }
}

/** digits 목록을 밀리초 단위 총 시간으로 변환한다. */
private fun List<Int>.toDurationMs(): Long {
    val d = List(6 - size) { 0 } + this
    val h = d[0] * 10 + d[1]
    val m = d[2] * 10 + d[3]
    val s = d[4] * 10 + d[5]
    return (h * 3600L + m * 60L + s) * 1000L
}

// ─── Previews ────────────────────────────────────────────────────────────────

@Preview(name = "키패드", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun TimerInputPadPreview() {
    AutoSleepTheme {
        TimerInputPad(onDigit = {}, onDoubleZero = {}, onDelete = {})
    }
}

@Preview(name = "타이머 디스플레이 - 빈 상태", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun TimerInputDisplayEmptyPreview() {
    AutoSleepTheme {
        TimerInputDisplay(digits = emptyList())
    }
}

@Preview(name = "타이머 디스플레이 - 1시간 30분", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun TimerInputDisplayFilledPreview() {
    AutoSleepTheme {
        // 1:30:00 → digits = [1, 3, 0, 0, 0]
        TimerInputDisplay(digits = listOf(1, 3, 0, 0, 0))
    }
}

@Preview(name = "프리셋 목록 - 빈 상태", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun PresetListEmptyPreview() {
    AutoSleepTheme {
        PresetList(presets = emptyList(), timeFormat = TimeFormat.KOREAN, onSelect = {}, onDelete = {})
    }
}

@Preview(name = "프리셋 목록 - 항목 있음", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun PresetListFilledPreview() {
    AutoSleepTheme {
        PresetList(
            presets = listOf(
                30 * 60 * 1000L,
                1 * 3600 * 1000L,
                1 * 3600 * 1000L + 30 * 60 * 1000L,
            ),
            timeFormat = TimeFormat.KOREAN,
            onSelect = {},
            onDelete = {},
        )
    }
}
