package com.ekh.autosleep

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ekh.autosleep.domain.entity.TimerState
import com.ekh.autosleep.presentation.analytics.AnalyticsScreen
import com.ekh.autosleep.presentation.permission.PermissionSetupScreen
import com.ekh.autosleep.presentation.permission.PermissionViewModel
import com.ekh.autosleep.presentation.routine.RoutineEditScreen
import com.ekh.autosleep.presentation.routine.RoutineScreen
import com.ekh.autosleep.presentation.settings.SettingsScreen
import com.ekh.autosleep.presentation.timer.TimerScreen
import com.ekh.autosleep.presentation.timer.TimerViewModel
import com.ekh.autosleep.ui.theme.AutoSleepTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private data class NavTab(val route: String, @DrawableRes val iconRes: Int, val label: String)

private object AppRoute {
    const val TIMER = "timer"
    const val ROUTINE = "routine"
    const val ROUTINE_EDIT = "routine_edit"
    const val ANALYTICS = "analytics"
    const val SETTINGS = "settings"
    const val PERMISSIONS = "permissions"
}

/**
 * 앱의 단일 진입점 Activity.
 * Hilt 주입을 위해 [@AndroidEntryPoint]로 선언되며,
 * Compose로 [MainScreen]을 렌더링한다.
 * [AppState]를 onStart/onStop에서 갱신해 [TimerService]가 알림 표시 여부를 판단한다.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var appState: AppState

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        setContent {
            AutoSleepTheme {
                MainScreen(
                    onRequestPostNotifications = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                )
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
 *
 * 최초 실행 시 권한이 부족하면 [PermissionSetupScreen]을 전체 화면으로 표시한다.
 * 타이머가 실행 중이면 카운트다운을 전체 화면으로 표시한다.
 * 그 외에는 [NavHost] 기반의 하단 내비게이션 바(타이머 / 설정)를 표시한다.
 * 설정 화면에서 권한 확인 버튼을 누르면 [AppRoute.PERMISSIONS]로 이동한다.
 */
@Composable
fun MainScreen(
    onRequestPostNotifications: () -> Unit,
    modifier: Modifier = Modifier,
    timerViewModel: TimerViewModel = hiltViewModel(),
    permissionViewModel: PermissionViewModel = hiltViewModel(),
) {
    val timerState by timerViewModel.timerState.collectAsState()
    val permissionState by permissionViewModel.permissionState.collectAsState()
    var setupDone by rememberSaveable { mutableStateOf(false) }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val isFirstRunPermission = !setupDone && !permissionState.canShowTimer
    val isTimerRunning = timerState is TimerState.Running
    val showBottomBar = !isFirstRunPermission && !isTimerRunning
        && currentRoute != AppRoute.PERMISSIONS
        && currentRoute != AppRoute.ROUTINE_EDIT

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) { it } + fadeIn(tween(200)),
                exit = ExitTransition.None,
            ) {
                AppBottomNavBar(
                    currentRoute = currentRoute,
                    onTabSelected = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        if (isFirstRunPermission) {
            PermissionSetupScreen(
                onContinue = { setupDone = true },
                onRequestPostNotifications = onRequestPostNotifications,
                viewModel = permissionViewModel,
                modifier = Modifier.padding(innerPadding),
            )
        } else if (isTimerRunning) {
            val running = timerState as TimerState.Running
            val h = running.remainingMs / 3_600_000
            val m = (running.remainingMs % 3_600_000) / 60_000
            val s = (running.remainingMs % 60_000) / 1_000
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "%02d:%02d:%02d".format(h, m, s),
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Light,
                )
                Spacer(modifier = Modifier.height(48.dp))
                OutlinedButton(
                    onClick = { timerViewModel.cancelTimer() },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        } else {
            NavHost(
                navController = navController,
                startDestination = AppRoute.TIMER,
                modifier = Modifier.padding(innerPadding),
            ) {
                composable(AppRoute.TIMER) {
                    TimerScreen(viewModel = timerViewModel)
                }
                if (BuildConfig.ROUTINE_FEATURE_ENABLED) {
                    composable(AppRoute.ROUTINE) {
                        RoutineScreen(
                            onAddRoutine = { navController.navigate("${AppRoute.ROUTINE_EDIT}/-1") },
                            onEditRoutine = { id: Long -> navController.navigate("${AppRoute.ROUTINE_EDIT}/$id") },
                        )
                    }
                    composable(
                        route = "${AppRoute.ROUTINE_EDIT}/{routineId}",
                        arguments = listOf(navArgument("routineId") { type = NavType.LongType }),
                    ) {
                        RoutineEditScreen(onBack = { navController.popBackStack() })
                    }
                }
                composable(AppRoute.ANALYTICS) {
                    AnalyticsScreen()
                }
                composable(AppRoute.SETTINGS) {
                    SettingsScreen(
                        onCheckPermissions = {
                            navController.navigate(AppRoute.PERMISSIONS)
                        },
                    )
                }
                composable(AppRoute.PERMISSIONS) {
                    PermissionSetupScreen(
                        onContinue = {},
                        onClose = { navController.popBackStack() },
                        onRequestPostNotifications = onRequestPostNotifications,
                        viewModel = permissionViewModel,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@Composable
private fun AppBottomNavBar(
    currentRoute: String?,
    onTabSelected: (String) -> Unit,
) {
    val tabs = buildList {
        add(NavTab(AppRoute.TIMER, R.drawable.ic_timer_bottom_nav, stringResource(R.string.nav_timer)))
        if (BuildConfig.ROUTINE_FEATURE_ENABLED) {
            add(NavTab(AppRoute.ROUTINE, R.drawable.ic_routine_bottom_nav, stringResource(R.string.nav_routine)))
        }
        add(NavTab(AppRoute.ANALYTICS, R.drawable.ic_analytics_bottom_nav, stringResource(R.string.nav_analytics)))
        add(NavTab(AppRoute.SETTINGS, R.drawable.ic_settings_bottom_nav, stringResource(R.string.nav_settings)))
    }

    Column {
        HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
        Row(modifier = Modifier.fillMaxWidth()) {
            tabs.forEach { tab ->
                val selected = currentRoute == tab.route
                val tint by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(durationMillis = 250),
                    label = "tabTint",
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = Color.Transparent),
                        ) {
                            onTabSelected(tab.route)
                        }
                        .padding(vertical = 8.dp),
                ) {
                    val indicatorAlpha by animateFloatAsState(
                        targetValue = if (selected) 1f else 0f,
                        animationSpec = tween(durationMillis = 200),
                        label = "indicatorAlpha",
                    )
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(28.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f * indicatorAlpha),
                                    shape = RoundedCornerShape(50),
                                ),
                        )
                        Icon(
                            painter = painterResource(tab.iconRes),
                            contentDescription = tab.label,
                            tint = tint,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(tab.label, fontSize = 10.sp, color = tint)
                }
            }
        }
    }
}
