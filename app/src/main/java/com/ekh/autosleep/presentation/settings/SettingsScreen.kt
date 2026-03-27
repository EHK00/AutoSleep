package com.ekh.autosleep.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ekh.autosleep.data.settings.TimeFormat
import com.ekh.autosleep.ui.theme.AutoSleepTheme

@Composable
fun SettingsScreen(
    onCheckPermissions: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val versionName = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "–"
        } catch (e: Exception) {
            "–"
        }
    }
    val timeFormat by viewModel.timeFormat.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "설정",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("프리셋 표시 형식", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = if (timeFormat == TimeFormat.KOREAN) "1시간 30분 45초" else "01:30:45",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            SingleChoiceSegmentedButtonRow(modifier = Modifier.padding(start = 8.dp)) {
                SegmentedButton(
                    selected = timeFormat == TimeFormat.KOREAN,
                    onClick = { viewModel.setTimeFormat(TimeFormat.KOREAN) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    label = { Text("시/분/초") },
                )
                SegmentedButton(
                    selected = timeFormat == TimeFormat.CLOCK,
                    onClick = { viewModel.setTimeFormat(TimeFormat.CLOCK) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    label = { Text("00:00:00") },
                )
            }
        }

        HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("버전", style = MaterialTheme.typography.bodyLarge)
            Text("v$versionName", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("권한 확인", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "앱 동작에 필요한 권한을 확인합니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            OutlinedButton(
                onClick = onCheckPermissions,
                modifier = Modifier.padding(start = 8.dp),
            ) {
                Text("확인")
            }
        }
    }
}

@Preview(name = "설정 화면", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SettingsScreenPreview() {
    AutoSleepTheme {
        SettingsScreen(onCheckPermissions = {})
    }
}
