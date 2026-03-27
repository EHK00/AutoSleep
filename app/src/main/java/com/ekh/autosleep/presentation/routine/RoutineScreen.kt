package com.ekh.autosleep.presentation.routine

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ekh.autosleep.domain.entity.Routine

@Composable
fun RoutineScreen(
    onAddRoutine: () -> Unit,
    onEditRoutine: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RoutineViewModel = hiltViewModel(),
) {
    val routines by viewModel.routines.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "루틴",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
            IconButton(onClick = onAddRoutine) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "루틴 추가")
            }
        }

        if (routines.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 48.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "루틴이 없습니다\n+ 버튼으로 추가해보세요",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 24.sp,
                    modifier = Modifier.alpha(0.6f),
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(routines, key = { it.id }) { routine ->
                    RoutineItem(
                        routine = routine,
                        onToggle = { viewModel.toggle(routine) },
                        onDelete = { viewModel.delete(routine) },
                        onClick = { onEditRoutine(routine.id) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoutineItem(
    routine: Routine,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else false
        },
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart)
                    MaterialTheme.colorScheme.errorContainer
                else Color.Transparent,
                animationSpec = tween(200),
                label = "swipeBg",
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(end = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "삭제",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .clickable(onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                val alpha = if (routine.isEnabled) 1f else 0.4f
                Text(
                    text = "%02d:%02d".format(routine.hour, routine.minute),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Light,
                    modifier = Modifier.alpha(alpha),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.alpha(alpha),
                ) {
                    DayChips(days = routine.days)
                    if (routine.label.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = routine.label,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Switch(
                checked = routine.isEnabled,
                onCheckedChange = { onToggle() },
            )
        }
    }
}

@Composable
private fun DayChips(days: Set<Int>) {
    if (days.isEmpty()) {
        Text(
            text = "1회",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }

    val dayLabels = listOf(2 to "월", 3 to "화", 4 to "수", 5 to "목", 6 to "금", 7 to "토", 1 to "일")
    val isEveryDay = dayLabels.all { (day, _) -> day in days }
    val isWeekday = listOf(2, 3, 4, 5, 6).all { it in days } && 7 !in days && 1 !in days
    val isWeekend = listOf(7, 1).all { it in days } && listOf(2, 3, 4, 5, 6).none { it in days }

    when {
        isEveryDay -> Text(
            text = "매일",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.primary,
        )
        isWeekday -> Text(
            text = "주중",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.primary,
        )
        isWeekend -> Text(
            text = "주말",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.primary,
        )
        else -> Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            dayLabels.forEach { (day, label) ->
                if (day in days) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(20.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                shape = CircleShape,
                            ),
                    ) {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}
