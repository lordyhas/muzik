package com.lordyhas.muzik.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

// Colors that follow the theme picked in Settings (see AppTheme.kt). They read Compose
// state, so composables and draw blocks using them update when the theme changes.
private val themed: MuzikColors get() = AppThemeStore.current.colors

val BackgroundDark: Color get() = themed.background
val SurfaceDark: Color get() = themed.surface
val SurfaceCard: Color get() = themed.card
val SurfaceElevated: Color get() = themed.elevated

/** Brand accent for text, icons and sliders on the background. */
val AccentPrimary: Color get() = themed.accent
/** Second brand hue, for small highlights. */
val AccentSecondary: Color get() = themed.secondary
/** Accent used behind text (Play, Save, Allow). Pair it with [OnAccentFill]. */
val AccentFill: Color get() = themed.fill
/** Text and icon color on [AccentFill]. */
val OnAccentFill: Color get() = themed.onFill

val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary: Color get() = themed.textSecondary
val TextMuted: Color get() = themed.textMuted

val DangerRed = Color(0xFFFF5252)

val TrackBackground = Color(0x33FFFFFF)
val TrackActive: Color get() = AccentPrimary

/** Hairline stroke used to lift cards off the background. */
val StrokeSubtle = Color(0x14FFFFFF)

/** Signature brand gradient of the current theme. */
val BrandGradient: List<Color> get() = themed.gradient

private val NearBlack = Color(0xFF0A0A0A)

/**
 * White on [background] when it reaches [minContrast] (WCAG), otherwise whichever of white
 * or near-black reads better. The default 3:1 suits icons; pass 4.5f for small text.
 */
fun contentColorOn(background: Color, minContrast: Float = 3f): Color {
    val l = background.luminance()
    val onWhite = 1.05f / (l + 0.05f)
    if (onWhite >= minContrast) return Color.White
    val onBlack = (l + 0.05f) / (NearBlack.luminance() + 0.05f)
    return if (onWhite >= onBlack) Color.White else NearBlack
}

/** [contentColorOn] for an icon centered on a [gradient]: judged on its middle color. */
fun contentColorOn(gradient: List<Color>, minContrast: Float = 3f): Color =
    contentColorOn(lerp(gradient.first(), gradient.last(), 0.5f), minContrast)
