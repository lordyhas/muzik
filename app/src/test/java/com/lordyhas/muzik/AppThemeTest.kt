package com.lordyhas.muzik

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.lordyhas.muzik.ui.theme.AppTheme
import com.lordyhas.muzik.ui.theme.contentColorOn
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppThemeTest {

    private fun contrast(a: Color, b: Color): Float {
        val la = a.luminance()
        val lb = b.luminance()
        return (maxOf(la, lb) + 0.05f) / (minOf(la, lb) + 0.05f)
    }

    @Test
    fun buttonTextIsReadableOnTheFillInEveryTheme() {
        AppTheme.entries.forEach { theme ->
            val c = theme.colors
            assertTrue("${theme.label}: text on fill", contrast(c.onFill, c.fill) >= 4.5f)
        }
    }

    @Test
    fun accentTextIsReadableOnTheBackgroundInEveryTheme() {
        AppTheme.entries.forEach { theme ->
            val c = theme.colors
            assertTrue("${theme.label}: accent on background", contrast(c.accent, c.background) >= 4.5f)
            assertTrue("${theme.label}: secondary on background", contrast(c.secondary, c.background) >= 4.5f)
            assertTrue("${theme.label}: secondary text", contrast(c.textSecondary, c.background) >= 4.5f)
        }
    }

    @Test
    fun contentColorOnPicksTheMoreReadableColor() {
        assertEquals(Color.White, contentColorOn(Color(0xFF7C3AED)))
        assertTrue(contentColorOn(Color(0xFF2DD4BF)) != Color.White)
        assertTrue(contentColorOn(Color(0xFFFFB020)) != Color.White)
    }

    @Test
    fun nocturneIsTheDefault() {
        assertEquals(AppTheme.NOCTURNE, AppTheme.DEFAULT)
    }
}
