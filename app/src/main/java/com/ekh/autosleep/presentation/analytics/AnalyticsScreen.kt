package com.ekh.autosleep.presentation.analytics

import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.ekh.autosleep.R
import android.graphics.Paint as AndroidPaint

@Composable
fun AnalyticsScreen(
    modifier: Modifier = Modifier,
    viewModel: AnalyticsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var showWindowDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "분석",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        painter = painterResource(R.drawable.ic_more_vert),
                        contentDescription = "더 보기",
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp),
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("수면 시간 범위 설정", fontSize = 14.sp) },
                        onClick = {
                            showMenu = false
                            showWindowDialog = true
                        },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        SummaryCard(state)

        Spacer(modifier = Modifier.height(24.dp))

        WeeklyChartSection(state)
    }

    if (showWindowDialog) {
        SleepWindowDialog(
            initialStartHour = state.sleepWindowStartHour,
            initialEndHour = state.sleepWindowEndHour,
            onConfirm = { start, end ->
                viewModel.setSleepWindow(start, end)
                showWindowDialog = false
            },
            onDismiss = { showWindowDialog = false },
        )
    }
}

@Composable
private fun SleepWindowDialog(
    initialStartHour: Int,
    initialEndHour: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var startHour by remember { mutableIntStateOf(initialStartHour) }
    var endHour by remember { mutableIntStateOf(initialEndHour) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "수면 시간 범위",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "이 범위 안에서 시작된 타이머만 수면 기록으로 집계됩니다.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )

            HourPickerRow(label = "시작", hour = startHour, onHourChange = { startHour = it })
            HourPickerRow(label = "종료", hour = endHour, onHourChange = { endHour = it })

            Text(
                text = buildWindowPreview(startHour, endHour),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDismiss) { Text("취소") }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = { onConfirm(startHour, endHour) }) { Text("확인") }
            }
        }
    }
}

@Composable
private fun HourPickerRow(
    label: String,
    hour: Int,
    onHourChange: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { onHourChange((hour - 1 + 24) % 24) },
                modifier = Modifier.size(36.dp),
            ) {
                Text("‹", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
            }
            Text(
                text = formatHour(hour),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.width(72.dp),
                textAlign = TextAlign.Center,
            )
            IconButton(
                onClick = { onHourChange((hour + 1) % 24) },
                modifier = Modifier.size(36.dp),
            ) {
                Text("›", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun SummaryCard(state: AnalyticsUiState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryRow(
                label = "최근 7일 평균 시작 시간",
                value = state.recentAverageMinute?.let { formatMinute(it) } ?: "기록 없음",
            )
            SummaryRow(
                label = "누적 수면 타이머",
                value = "${state.totalCount}회",
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun WeeklyChartSection(state: AnalyticsUiState) {
    Column {
        Text(
            text = "최근 7일 시작 시간",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (state.chartPoints.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "최근 기록이 없습니다",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                )
            }
        } else {
            WeeklyLineChart(
                points = state.chartPoints,
                xAxisLabels = state.xAxisLabels,
                primaryColor = MaterialTheme.colorScheme.primary,
                surfaceColor = MaterialTheme.colorScheme.surface,
                onSurfaceColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
            )
        }
    }
}

@Composable
private fun WeeklyLineChart(
    points: List<DayPoint>,
    xAxisLabels: List<String>,
    primaryColor: Color,
    surfaceColor: Color,
    onSurfaceColor: Color,
    modifier: Modifier = Modifier,
) {
    val yMin = 20 * 60
    val yMax = 30 * 60

    fun normalizeMinute(m: Int): Int = if (m < 12 * 60) m + 24 * 60 else m

    val density = LocalDensity.current
    val labelTextSizePx = with(density) { 10.sp.toPx() }
    val dotLabelColor = onSurfaceColor.copy(alpha = 0.9f).toArgb()

    Box(
        modifier = modifier
            .background(surfaceColor, RoundedCornerShape(16.dp))
            .padding(start = 12.dp, end = 12.dp, top = 24.dp, bottom = 36.dp),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val yRange = (yMax - yMin).toFloat()

            fun yFraction(minute: Int): Float = 1f - (minute - yMin) / yRange
            fun xFraction(slotIndex: Int): Float = (slotIndex + 0.5f) / 7f

            val normalized = points.map { it.copy(minuteOfDay = normalizeMinute(it.minuteOfDay)) }

            if (normalized.size >= 2) {
                val path = Path()
                normalized.forEachIndexed { i, pt ->
                    val x = w * xFraction(pt.slotIndex)
                    val y = h * yFraction(pt.minuteOfDay)
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(
                    path = path,
                    color = primaryColor.copy(alpha = 0.45f),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
                )
            }

            val labelPaint = AndroidPaint().apply {
                color = dotLabelColor
                textSize = labelTextSizePx
                textAlign = AndroidPaint.Align.CENTER
                isAntiAlias = true
                typeface = Typeface.DEFAULT
            }
            val labelOffsetUp = 18.dp.toPx()
            val labelOffsetDown = labelTextSizePx + 8.dp.toPx()

            normalized.forEach { pt ->
                val x = w * xFraction(pt.slotIndex)
                val y = h * yFraction(pt.minuteOfDay)

                drawCircle(color = primaryColor, radius = 5.dp.toPx(), center = Offset(x, y))
                drawCircle(color = surfaceColor, radius = 2.5f.dp.toPx(), center = Offset(x, y))

                val labelY = if (y < labelOffsetUp) y + labelOffsetDown else y - labelOffsetUp
                val timeLabel = formatMinuteShort(pt.minuteOfDay)
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawText(timeLabel, x, labelY, labelPaint)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            xAxisLabels.forEach { label ->
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = onSurfaceColor.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

private fun formatHour(hour: Int): String {
    val amPm = if (hour < 12) "오전" else "오후"
    val displayH = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
    return "$amPm ${displayH}시"
}

private fun buildWindowPreview(startHour: Int, endHour: Int): String =
    "${formatHour(startHour)} ~ ${formatHour(endHour)}"

private fun formatMinute(totalMinutes: Int): String {
    val h = (totalMinutes / 60) % 24
    val m = totalMinutes % 60
    val amPm = if (h < 12) "오전" else "오후"
    val displayH = if (h == 0) 12 else if (h > 12) h - 12 else h
    return "$amPm ${displayH}시 ${"%02d".format(m)}분"
}

private fun formatMinuteShort(normalizedMinute: Int): String {
    val actual = normalizedMinute % (24 * 60)
    val h = actual / 60
    val m = actual % 60
    return "${h}:${"%02d".format(m)}"
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun WeeklyLineChartPreview() {
    com.ekh.autosleep.ui.theme.AutoSleepTheme {
        WeeklyLineChart(
            points = listOf(
                DayPoint(slotIndex = 0, minuteOfDay = 23 * 60 + 10),
                DayPoint(slotIndex = 1, minuteOfDay = 0 * 60 + 30),
                DayPoint(slotIndex = 2, minuteOfDay = 23 * 60 + 45),
                DayPoint(slotIndex = 4, minuteOfDay = 1 * 60 + 15),
                DayPoint(slotIndex = 5, minuteOfDay = 2 * 60 + 0),
                DayPoint(slotIndex = 6, minuteOfDay = 0 * 60 + 50),
            ),
            xAxisLabels = listOf("화", "수", "목", "금", "토", "일", "월"),
            primaryColor = androidx.compose.ui.graphics.Color(0xFF7EB8F7),
            surfaceColor = androidx.compose.ui.graphics.Color(0xFF111111),
            onSurfaceColor = androidx.compose.ui.graphics.Color(0xFFE0E0E0),
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth()
                .height(220.dp),
        )
    }
}
