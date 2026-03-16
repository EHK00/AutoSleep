package com.ekh.autosleep.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NightColorScheme = darkColorScheme(
    primary          = NightPrimary,
    onPrimary        = NightOnPrimary,
    background       = NightBackground,
    onBackground     = NightOnBg,
    surface          = NightSurface,
    onSurface        = NightOnSurface,
    outline          = NightOutline,
)

@Composable
fun AutoSleepTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NightColorScheme,
        typography = Typography,
        content = content,
    )
}
