package com.lordyhas.muzik.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private fun colorSchemeFor(colors: MuzikColors) = darkColorScheme(
    primary = colors.fill,
    secondary = colors.secondary,
    tertiary = colors.accent,
    background = colors.background,
    surface = colors.surface,
    surfaceVariant = colors.card,
    surfaceContainer = colors.card,
    surfaceContainerHigh = colors.elevated,
    outline = colors.textMuted,
    outlineVariant = StrokeSubtle,
    error = DangerRed,
    onPrimary = colors.onFill,
    onSecondary = contentColorOn(colors.secondary),
    onTertiary = contentColorOn(colors.accent),
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = colors.textSecondary
)

@Composable
fun MuzikTheme(
    content: @Composable () -> Unit
) {
    val theme = AppThemeStore.current
    val colorScheme = remember(theme) { colorSchemeFor(theme.colors) }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = theme.colors.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}