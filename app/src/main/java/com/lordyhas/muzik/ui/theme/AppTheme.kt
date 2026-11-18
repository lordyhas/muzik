package com.lordyhas.muzik.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.core.content.edit

/**
 * Every color that changes with the app theme. Two brand hues per theme: [accent]
 * (text, icons, sliders on the background) and [secondary]; [fill] is the accent used
 * behind text (Play buttons) and [onFill] is the text color that stays readable on it.
 */
data class MuzikColors(
    val background: Color,
    val surface: Color,
    val card: Color,
    val elevated: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val accent: Color,
    val fill: Color,
    val onFill: Color,
    val secondary: Color,
    val gradient: List<Color>,
    /** Smart playlist covers: Liked, Most Played, Recently Added, Rediscover. */
    val liked: List<Color>,
    val mostPlayed: List<Color>,
    val recentlyAdded: List<Color>,
    val rediscover: List<Color>
)

/** The themes listeners can pick in Settings. The first one is the default. */
enum class AppTheme(val label: String, val description: String, val colors: MuzikColors) {
    NOCTURNE(
        label = "Nocturne",
        description = "Violet, with a touch of coral",
        colors = MuzikColors(
            background = Color(0xFF0A0910),
            surface = Color(0xFF13121B),
            card = Color(0xFF1B1A26),
            elevated = Color(0xFF252433),
            textSecondary = Color(0xFFA3A2AE),
            textMuted = Color(0xFF686778),
            accent = Color(0xFFA78BFA),
            fill = Color(0xFF7C3AED),
            onFill = Color(0xFFFFFFFF),
            secondary = Color(0xFFFF5C7A),
            gradient = listOf(Color(0xFF8B5CF6), Color(0xFFFF5C7A)),
            liked = listOf(Color(0xFF8B5CF6), Color(0xFFFF5C7A)),
            mostPlayed = listOf(Color(0xFF7C3AED), Color(0xFF4C1D95)),
            recentlyAdded = listOf(Color(0xFF6D28D9), Color(0xFF312E81)),
            rediscover = listOf(Color(0xFF5B21B6), Color(0xFF2E1065))
        )
    ),
    EMBER(
        label = "Ember",
        description = "Warm orange and amber",
        colors = MuzikColors(
            background = Color(0xFF0C0A09),
            surface = Color(0xFF15110F),
            card = Color(0xFF1F1916),
            elevated = Color(0xFF2A221E),
            textSecondary = Color(0xFFADA49F),
            textMuted = Color(0xFF786C64),
            accent = Color(0xFFFF6B3D),
            fill = Color(0xFFFF6B3D),
            onFill = Color(0xFF1A0B05),
            secondary = Color(0xFFFFB020),
            gradient = listOf(Color(0xFFFF6B3D), Color(0xFFFFB020)),
            liked = listOf(Color(0xFFFF6B3D), Color(0xFFE8452C)),
            mostPlayed = listOf(Color(0xFFFFB020), Color(0xFFF97316)),
            recentlyAdded = listOf(Color(0xFFC2410C), Color(0xFF7C2D12)),
            rediscover = listOf(Color(0xFF9A3412), Color(0xFF431407))
        )
    ),
    LAGOON(
        label = "Lagoon",
        description = "Turquoise and indigo",
        colors = MuzikColors(
            background = Color(0xFF070B0C),
            surface = Color(0xFF0F1517),
            card = Color(0xFF162023),
            elevated = Color(0xFF1F2C30),
            textSecondary = Color(0xFF9FB0B3),
            textMuted = Color(0xFF5F7377),
            accent = Color(0xFF2DD4BF),
            fill = Color(0xFF2DD4BF),
            onFill = Color(0xFF04201C),
            secondary = Color(0xFF818CF8),
            gradient = listOf(Color(0xFF2DD4BF), Color(0xFF6366F1)),
            liked = listOf(Color(0xFF14B8A6), Color(0xFF0F766E)),
            mostPlayed = listOf(Color(0xFF6366F1), Color(0xFF4338CA)),
            recentlyAdded = listOf(Color(0xFF0D9488), Color(0xFF4F46E5)),
            rediscover = listOf(Color(0xFF115E59), Color(0xFF134E4A))
        )
    );

    companion object {
        val DEFAULT = NOCTURNE
    }
}

/**
 * The theme in use, shared by the player and the Tag editor. It is Compose state, so
 * every color read from `Color.kt` follows a change at once, and it is kept in
 * `muzik_prefs`. Activities call [init] before `setContent`.
 */
object AppThemeStore {
    private const val PREFS = "muzik_prefs"
    private const val KEY = "app_theme"

    private var prefs: SharedPreferences? = null

    var current: AppTheme by mutableStateOf(AppTheme.DEFAULT)
        private set

    fun init(context: Context) {
        if (prefs != null) return
        val p = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs = p
        current = AppTheme.entries.firstOrNull { it.name == p.getString(KEY, null) } ?: AppTheme.DEFAULT
    }

    fun select(theme: AppTheme) {
        current = theme
        prefs?.edit { putString(KEY, theme.name) }
    }
}
