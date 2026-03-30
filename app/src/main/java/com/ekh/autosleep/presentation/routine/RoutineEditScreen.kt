package com.ekh.autosleep.presentation.routine

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ekh.autosleep.R
import com.ekh.autosleep.presentation.timer.TimerInputPad
import kotlinx.coroutines.launch

@Composable
fun RoutineEditScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RoutineEditViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val digits by viewModel.digits.collectAsState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
    ) {
        // 상단 뒤로가기 + 타이틀
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                )
            }
            Text(
                text = if (uiState.isNew) stringResource(R.string.routine_edit_add_title) else stringResource(R.string.routine_edit_edit_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 4.dp),
            )
        }

        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        // 시간 디스플레이
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            RoutineTimeDisplay(digits = digits)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 키패드
        TimerInputPad(
            onDigit = viewModel::onDigit,
            onDoubleZero = viewModel::onDoubleZero,
            onDelete = viewModel::onDelete,
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 반복 요일 선택
        Text(
            text = stringResource(R.string.routine_edit_repeat),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))

        val dayList = listOf(
            2 to stringResource(R.string.day_mon),
            3 to stringResource(R.string.day_tue),
            4 to stringResource(R.string.day_wed),
            5 to stringResource(R.string.day_thu),
            6 to stringResource(R.string.day_fri),
            7 to stringResource(R.string.day_sat),
            1 to stringResource(R.string.day_sun),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            dayList.forEach { (day, label) ->
                val selected = day in uiState.days
                val bgColor = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surface
                val textColor = if (selected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurface

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(bgColor)
                        .border(
                            width = 1.dp,
                            color = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            shape = CircleShape,
                        )
                        .clickable { viewModel.toggleDay(day) },
                ) {
                    Text(text = label, fontSize = 13.sp, color = textColor)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 이름 입력
        Text(
            text = stringResource(R.string.routine_edit_name),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))

        BasicTextField(
            value = uiState.label,
            onValueChange = { viewModel.setLabel(it) },
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            decorationBox = { inner ->
                Column {
                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                        if (uiState.label.isEmpty()) {
                            Text(
                                text = stringResource(R.string.routine_edit_name_placeholder),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                fontSize = 16.sp,
                            )
                        }
                        inner()
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.weight(1f))

        // 저장 / 취소
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.cancel))
            }
            Button(
                onClick = {
                    focusManager.clearFocus()
                    scope.launch {
                        val success = viewModel.save()
                        if (success) onBack()
                    }
                },
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}

/**
 * HHMM digit 목록을 HH:MM 형식으로 표시.
 * 아직 입력되지 않은 자리는 회색, 입력된 자리는 흰색으로 표시.
 */
@Composable
private fun RoutineTimeDisplay(digits: List<Int>) {
    val padded = List(4 - digits.size) { 0 } + digits
    val activeStart = 4 - digits.size

    val text = buildAnnotatedString {
        padded.forEachIndexed { i, d ->
            val color = if (i >= activeStart) Color.White else Color.Gray
            withStyle(SpanStyle(color = color)) { append(d.toString()) }
            if (i == 1) withStyle(SpanStyle(color = Color.White)) { append(":") }
        }
    }

    Text(text = text, fontSize = 64.sp, fontWeight = FontWeight.Light)
}
