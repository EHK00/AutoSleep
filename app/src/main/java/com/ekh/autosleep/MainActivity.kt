package com.ekh.autosleep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ekh.autosleep.domain.entity.TimerState
import com.ekh.autosleep.presentation.main.MainViewModel
import com.ekh.autosleep.presentation.permission.PermissionSetupScreen
import com.ekh.autosleep.ui.theme.AutoSleepTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * 앱의 단일 진입점 Activity.
 * Hilt 주입을 위해 [@AndroidEntryPoint]로 선언되며,
 * Compose로 [MainScreen]을 렌더링한다.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AutoSleepTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

/**
 * 메인 화면 컴포저블.
 * 권한이 부족하면 [PermissionSetupScreen]을 표시하고,
 * 권한이 충족되면 타이머 UI(시간 표시 + 프리셋 버튼 + 취소 버튼)를 표시한다.
 * [setupDone] 플래그로 사용자가 권한 화면을 건너뛸 수 있다.
 */
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel(),
) {
    val timerState by viewModel.timerState.collectAsState()
    val permissionState by viewModel.permissionState.collectAsState()
    var setupDone by rememberSaveable { mutableStateOf(false) }

    if (!setupDone && !permissionState.canLockScreen) {
        PermissionSetupScreen(
            permissionState = permissionState,
            onRefresh = viewModel::refreshPermissions,
            onContinue = { setupDone = true },
            modifier = modifier,
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        val timerText = when (timerState) {
            is TimerState.Idle -> "타이머 대기 중"
            is TimerState.Running -> {
                val running = timerState as TimerState.Running
                val minutes = running.remainingMs / 60_000
                val seconds = (running.remainingMs % 60_000) / 1_000
                "%02d:%02d".format(minutes, seconds)
            }
            is TimerState.Expired -> "수면 전환 완료"
            is TimerState.Cancelled -> "취소됨"
        }
        Text(text = timerText, fontSize = 48.sp)

        Spacer(modifier = Modifier.height(48.dp))

        val presets = listOf(
            "10초" to 10_000L,
            "5분" to 5 * 60_000L,
            "15분" to 15 * 60_000L,
            "30분" to 30 * 60_000L,
            "60분" to 60 * 60_000L,
        )

        presets.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                row.forEach { (label, durationMs) ->
                    Button(
                        onClick = { viewModel.startTimer(durationMs) },
                        modifier = Modifier.weight(1f),
                        enabled = timerState !is TimerState.Running,
                    ) {
                        Text(label)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = { viewModel.cancelTimer() },
            enabled = timerState is TimerState.Running,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("취소")
        }
    }
}
