package com.projectgame.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun AventiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AventiColorScheme,
        typography = AventiTypography,
        content = content
    )
}
