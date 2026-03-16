package com.ekh.autosleep

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ekh.autosleep.domain.entity.TimerState
import com.ekh.autosleep.presentation.main.MainViewModel
import com.ekh.autosleep.presentation.permission.PermissionSetupScreen
import com.ekh.autosleep.ui.theme.AutoSleepTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 앱의 단일 진입점 Activity.
 * Hilt 주입을 위해 [@AndroidEntryPoint]로 선언되며,
 * Compose로 [MainScreen]을 렌더링한다.
 * [AppState]를 onStart/onStop에서 갱신해 [TimerService]가 알림 표시 여부를 판단한다.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var appState: AppState

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        setContent {
            AutoSleepTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        appState.isInForeground.value = true
    }

    override fun onStop() {
        super.onStop()
        appState.isInForeground.value = false
    }
}

/**
 * 메인 화면 컴포저블.
 * 권한이 부족하면 [PermissionSetupScreen]을 표시하고,
 * 권한이 충족되면 타이머 UI를 표시한다.
 * 타이머가 실행 중이면 카운트다운을 보여주고, 그 외에는 HH:MM:SS 키패드 입력 UI를 보여준다.
 * [setupDone] 플래그로 사용자가 권한 화면을 건너뛸 수 있다.
 */
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel(),
) {
    val timerState by viewModel.timerState.collectAsState()
    val permissionState by viewModel.permissionState.collectAsState()
    val timerDigits by viewModel.timerDigits.collectAsState()
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
        if (timerState is TimerState.Running) {
            val running = timerState as TimerState.Running
            val h = running.remainingMs / 3_600_000
            val m = (running.remainingMs % 3_600_000) / 60_000
            val s = (running.remainingMs % 60_000) / 1_000
            Text(
                text = "%02d:%02d:%02d".format(h, m, s),
                fontSize = 56.sp,
                fontWeight = FontWeight.Light,
            )

            Spacer(modifier = Modifier.height(48.dp))

            OutlinedButton(
                onClick = { viewModel.cancelTimer() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("취소")
            }
        } else {
            TimerInputDisplay(digits = timerDigits)

            Spacer(modifier = Modifier.height(24.dp))

            TimerInputPad(
                onDigit = viewModel::onTimerDigit,
                onDoubleZero = viewModel::onTimerDoubleZero,
                onDelete = viewModel::onTimerDelete,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.startTimer(timerDigits.toDurationMs()) },
                enabled = timerDigits.toDurationMs() > 0,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("시작", fontSize = 18.sp)
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
private fun TimerInputPad(
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

/** digits 목록을 밀리초 단위 총 시간으로 변환한다. */
private fun List<Int>.toDurationMs(): Long {
    val d = List(6 - size) { 0 } + this
    val h = d[0] * 10 + d[1]
    val m = d[2] * 10 + d[3]
    val s = d[4] * 10 + d[5]
    return (h * 3600L + m * 60L + s) * 1000L
}
