package com.lordyhas.muzik.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.lordyhas.muzik.service.SpectrumAnalyzer
import com.lordyhas.muzik.service.SpectrumMath
import com.lordyhas.muzik.ui.theme.AccentPrimary
import com.lordyhas.muzik.ui.theme.AccentSecondary
import kotlin.math.min

private const val SEGMENTS = 16

/** Bars jump up quickly but fall back slowly, like a hardware meter. */
private const val RISE_PER_SECOND = 28f
private const val FALL_PER_SECOND = 1.6f

/** The peak mark lingers at a bar's highest point, then drops. */
private const val PEAK_HOLD_SECONDS = 0.45f
private const val PEAK_FALL_PER_SECOND = 0.9f

/**
 * Replaces the cover on Now Playing: a segmented bar graph of what is playing, in the theme's two
 * brand hues (bottom to top). Analysis runs only while this panel is on screen.
 */
@Composable
fun SpectrumPanel(modifier: Modifier = Modifier) {
    DisposableEffect(Unit) {
        SpectrumAnalyzer.enabled = true
        onDispose { SpectrumAnalyzer.enabled = false }
    }

    val bands = SpectrumMath.BANDS
    val bars = remember { FloatArray(bands) }
    val peaks = remember { FloatArray(bands) }
    val peakHold = remember { FloatArray(bands) }
    var frame by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        val target = FloatArray(bands)
        var last = 0L
        while (true) {
            withFrameNanos { }
            val now = System.nanoTime()
            val dt = if (last == 0L) 0f else min((now - last) / 1e9f, 0.1f)
            last = now
            if (!SpectrumAnalyzer.levelsAt(now, target)) target.fill(0f)
            for (i in 0 until bands) {
                bars[i] = if (target[i] > bars[i]) {
                    bars[i] + (target[i] - bars[i]) * min(1f, dt * RISE_PER_SECOND)
                } else {
                    maxOf(target[i], bars[i] - dt * FALL_PER_SECOND)
                }
                if (bars[i] >= peaks[i]) {
                    peaks[i] = bars[i]
                    peakHold[i] = PEAK_HOLD_SECONDS
                } else if (peakHold[i] > 0f) {
                    peakHold[i] -= dt
                } else {
                    peaks[i] = maxOf(bars[i], peaks[i] - dt * PEAK_FALL_PER_SECOND)
                }
            }
            frame = now
        }
    }

    val bottom = AccentPrimary
    val top = AccentSecondary
    val rowColors = remember(bottom, top) {
        List(SEGMENTS) { row -> lerp(bottom, top, row / (SEGMENTS - 1f)) }
    }
    val unlit = Color.White.copy(alpha = 0.06f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Black.copy(alpha = 0.28f))
            .padding(20.dp)
            .semantics { contentDescription = "Audio visualizer" }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Reading the frame state redraws the canvas on every animation frame
            if (frame == Long.MIN_VALUE) return@Canvas

            val gapX = size.width / bands * 0.22f
            val cellW = (size.width - gapX * (bands - 1)) / bands
            val gapY = size.height / SEGMENTS * 0.22f
            val cellH = (size.height - gapY * (SEGMENTS - 1)) / SEGMENTS
            val radius = CornerRadius(min(cellW, cellH) * 0.18f)
            val cell = Size(cellW, cellH)

            for (b in 0 until bands) {
                val x = b * (cellW + gapX)
                val lit = bars[b] * SEGMENTS
                val peakRow = (peaks[b] * SEGMENTS).toInt().coerceAtMost(SEGMENTS - 1)
                val showPeak = peaks[b] > 0.02f && peakRow.toFloat() >= lit
                for (row in 0 until SEGMENTS) {
                    val y = size.height - (row + 1) * cellH - row * gapY
                    val fill = (lit - row).coerceIn(0f, 1f)
                    val color = when {
                        showPeak && row == peakRow -> lerp(rowColors[row], Color.White, 0.3f)
                        fill > 0f -> lerp(unlit, rowColors[row], fill)
                        else -> unlit
                    }
                    drawRoundRect(color = color, topLeft = Offset(x, y), size = cell, cornerRadius = radius)
                }
            }
        }
    }
}
