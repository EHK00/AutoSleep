package com.ekh.autosleep.presentation.permission

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ekh.autosleep.domain.entity.PermissionState
import com.ekh.autosleep.ui.theme.AutoSleepTheme

/**
 * 앱 최초 실행 시 표시되는 권한 설정 온보딩 화면.
 *
 * 두 가지 권한을 필수/선택으로 나누어 안내하며, 각 항목에 ✓/✗ 상태와 설정 버튼을 표시한다.
 * 화면 복귀 시([Lifecycle.Event.ON_RESUME]) [onRefresh]를 호출하여 권한 상태를 자동으로 갱신한다.
 * [PermissionState.canLockScreen]이 true일 때만 "시작하기" 버튼이 활성화된다.
 *
 * @param permissionState 현재 권한 허용 상태.
 * @param onRefresh 권한 상태를 재조회하도록 ViewModel에 요청하는 콜백.
 * @param onContinue 권한 설정 완료 후 메인 화면으로 진입하는 콜백.
 */
@Composable
fun PermissionSetupScreen(
    permissionState: PermissionState,
    onRefresh: () -> Unit,
    onContinue: () -> Unit,
    onRequestPostNotifications: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 설정 화면에서 돌아올 때마다 권한 상태 갱신
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) onRefresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "권한 설정",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "AutoSleep이 동작하려면 아래 권한이 필요합니다.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 선택 권한 섹션
        SectionLabel("선택 (미디어 제어 / 상태바)")

        PermissionItem(
            title = "알림 리스너",
            description = "YouTube, Twitch 등 재생 중인 미디어를 자동으로 일시정지합니다.",
            granted = permissionState.notificationListenerGranted,
            onSetup = {
                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            },
        )
//
//        if (Build.VERSION.SDK_INT >= 36) {
//            PermissionItem(
//                title = "실시간 정보",
//                description = "타이머 실행 중 상태바에 남은 시간을 표시합니다.",
//                granted = permissionState.promotedNotificationsGranted,
//                onSetup = {
//                    context.startActivity(
//                        Intent(Settings.ACTION_MANAGE_APP_PROMOTED_NOTIFICATIONS).apply {
//                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
//                        }
//                    )
//                },
//            )
//        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))


        // 필수 권한 섹션
        SectionLabel("필수 (화면 끄기 / 알림)")

        PermissionItem(
            title = "접근성 서비스",
            description = "타이머 만료 시 화면을 잠급니다.",
            granted = permissionState.accessibilityGranted,
            onSetup = {
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            },
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionItem(
                title = "알림 권한",
                description = "타이머 실행 중 백그라운드 알림을 표시합니다.",
                granted = permissionState.postNotificationsGranted,
                onSetup = onRequestPostNotifications,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onContinue,
            enabled = permissionState.canShowTimer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (permissionState.canShowTimer) "시작하기" else "필수 권한이 필요합니다")
        }
    }
}

/** 권한 목록의 섹션 제목을 표시하는 레이블 컴포저블. */
@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 4.dp),
    )
}

/**
 * 단일 권한 항목을 표시하는 컴포저블.
 * 권한 허용 시 ✓ 아이콘만 표시하고, 미허용 시 ✗ 아이콘과 "설정" 버튼을 함께 표시한다.
 *
 * @param title 권한 이름.
 * @param description 권한이 필요한 이유 설명.
 * @param granted 권한 허용 여부.
 * @param onSetup "설정" 버튼 클릭 시 해당 시스템 설정 화면으로 이동하는 콜백.
 */
@Preview(name = "권한 항목 - 허용됨", showBackground = true)
@Composable
private fun PermissionItemGrantedPreview() {
    AutoSleepTheme {
        PermissionItem(
            title = "접근성 서비스",
            description = "타이머 만료 시 화면을 잠급니다.",
            granted = true,
            onSetup = {},
        )
    }
}

@Preview(name = "권한 항목 - 거부됨", showBackground = true)
@Composable
private fun PermissionItemDeniedPreview() {
    AutoSleepTheme {
        PermissionItem(
            title = "접근성 서비스",
            description = "타이머 만료 시 화면을 잠급니다.",
            granted = false,
            onSetup = {},
        )
    }
}

@Composable
private fun PermissionItem(
    title: String,
    description: String,
    granted: Boolean,
    onSetup: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (granted) "✓" else "✗",
                    color = if (granted) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                )
                Text(text = title, fontWeight = FontWeight.SemiBold)
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 18.dp),
            )
        }

        if (!granted) {
            OutlinedButton(
                onClick = onSetup,
                modifier = Modifier.padding(start = 8.dp),
            ) {
                Text("설정")
            }
        }
    }
}

// ─── Previews ────────────────────────────────────────────────────────────────

@Preview(name = "권한 설정 - 모든 권한 거부됨", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PermissionSetupAllDeniedPreview() {
    AutoSleepTheme {
        PermissionSetupScreen(
            permissionState = PermissionState(
                notificationListenerGranted = false,
                accessibilityGranted = false,
                postNotificationsGranted = false,
            ),
            onRefresh = {},
            onContinue = {},
            onRequestPostNotifications = {},
        )
    }
}

@Preview(name = "권한 설정 - 모든 권한 허용됨", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PermissionSetupAllGrantedPreview() {
    AutoSleepTheme {
        PermissionSetupScreen(
            permissionState = PermissionState(
                notificationListenerGranted = true,
                accessibilityGranted = true,
                postNotificationsGranted = true,
            ),
            onRefresh = {},
            onContinue = {},
            onRequestPostNotifications = {},
        )
    }
}

@Preview(name = "권한 설정 - 필수만 허용됨", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PermissionSetupRequiredOnlyPreview() {
    AutoSleepTheme {
        PermissionSetupScreen(
            permissionState = PermissionState(
                notificationListenerGranted = false,
                accessibilityGranted = true,
                postNotificationsGranted = true,
            ),
            onRefresh = {},
            onContinue = {},
            onRequestPostNotifications = {},
        )
    }
}
