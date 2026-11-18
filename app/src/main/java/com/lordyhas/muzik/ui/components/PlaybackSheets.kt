package com.lordyhas.muzik.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lordyhas.muzik.service.EqualizerInfo
import com.lordyhas.muzik.service.EqualizerSettings
import com.lordyhas.muzik.service.SleepTimer
import com.lordyhas.muzik.ui.theme.OnAccentFill
import com.lordyhas.muzik.ui.theme.AccentFill
import com.lordyhas.muzik.ui.theme.AccentPrimary
import com.lordyhas.muzik.ui.theme.StrokeSubtle
import com.lordyhas.muzik.ui.theme.SurfaceCard
import com.lordyhas.muzik.ui.theme.SurfaceDark
import com.lordyhas.muzik.ui.theme.SurfaceElevated
import com.lordyhas.muzik.ui.theme.TextMuted
import com.lordyhas.muzik.ui.theme.TextPrimary
import com.lordyhas.muzik.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.util.Locale

/** Milliseconds left on a running timer, ticking every second; null when no timed sleep is set. */
@Composable
fun rememberSleepRemaining(timer: SleepTimer): Long? {
    if (timer !is SleepTimer.At) return null
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(timer) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }
    return (timer.endsAtMillis - now).coerceAtLeast(0L)
}

fun formatCountdown(ms: Long): String {
    val totalSeconds = ms / 1000
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return if (h > 0) String.format(Locale.US, "%d:%02d:%02d", h, m, s)
    else String.format(Locale.US, "%d:%02d", m, s)
}

@Composable
internal fun SheetDragHandle() {
    Box(
        modifier = Modifier
            .padding(vertical = 12.dp)
            .size(width = 40.dp, height = 4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(TextSecondary.copy(alpha = 0.4f))
    )
}

// ─── Sleep timer ────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepTimerSheet(
    timer: SleepTimer,
    onSetMinutes: (Int) -> Unit,
    onEndOfTrack: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val remaining = rememberSleepRemaining(timer)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceDark,
        dragHandle = { SheetDragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp)
        ) {
            Text("Sleep timer", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
            Text(
                text = when (timer) {
                    is SleepTimer.At -> "Music stops in ${formatCountdown(remaining ?: 0)}"
                    SleepTimer.EndOfTrack -> "Music stops when this song ends"
                    SleepTimer.Off -> "Music fades out and pauses when the timer ends"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (timer == SleepTimer.Off) TextSecondary else AccentPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            listOf(5, 15, 30, 45, 60, 90).forEach { minutes ->
                SheetOption(
                    icon = Icons.Default.Bedtime,
                    label = if (minutes < 60) "$minutes minutes" else if (minutes == 60) "1 hour" else "1 hour 30 minutes",
                    selected = timer is SleepTimer.At && timer.totalMillis == minutes * 60_000L,
                    onClick = {
                        onSetMinutes(minutes)
                        onDismiss()
                    }
                )
            }
            SheetOption(
                icon = Icons.Default.MusicNote,
                label = "End of current song",
                selected = timer == SleepTimer.EndOfTrack,
                onClick = {
                    onEndOfTrack()
                    onDismiss()
                }
            )
            if (timer != SleepTimer.Off) {
                SheetOption(
                    icon = Icons.Default.TimerOff,
                    label = "Turn off timer",
                    selected = false,
                    tint = TextSecondary,
                    onClick = {
                        onCancel()
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
private fun SheetOption(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    tint: Color = AccentPrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) AccentPrimary.copy(alpha = 0.12f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Icon(Icons.Default.Check, contentDescription = "Selected", tint = AccentPrimary, modifier = Modifier.size(20.dp))
        }
    }
}

// ─── Audio effects: speed + equalizer ───────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioEffectsSheet(
    speed: Float,
    onSpeedChange: (Float) -> Unit,
    equalizer: EqualizerSettings,
    equalizerInfo: EqualizerInfo?,
    onEqualizerEnabled: (Boolean) -> Unit,
    onPresetSelected: (Int) -> Unit,
    onBandLevelChange: (band: Int, level: Int) -> Unit,
    onResetEqualizer: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceDark,
        dragHandle = { SheetDragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
        ) {
            // ── Speed ────────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Playback speed",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatSpeed(speed),
                    style = MaterialTheme.typography.titleMedium,
                    color = AccentPrimary
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 1.75f, 2f).forEach { option ->
                    FilterChip(
                        selected = speed == option,
                        onClick = { onSpeedChange(option) },
                        label = { Text(formatSpeed(option)) },
                        shape = CircleShape,
                        colors = chipColors(),
                        border = null
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Equalizer ────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Equalizer", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                    Text(
                        text = if (equalizerInfo == null) "Not available on this device" else "Shape the sound to your taste",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Switch(
                    checked = equalizer.enabled && equalizerInfo != null,
                    onCheckedChange = onEqualizerEnabled,
                    enabled = equalizerInfo != null,
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = AccentPrimary,
                        checkedThumbColor = Color.White,
                        uncheckedTrackColor = SurfaceElevated,
                        uncheckedBorderColor = StrokeSubtle
                    )
                )
            }

            if (equalizerInfo != null) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = equalizer.preset == -1,
                        onClick = onResetEqualizer,
                        label = { Text("Custom") },
                        shape = CircleShape,
                        colors = chipColors(),
                        border = null
                    )
                    equalizerInfo.presets.forEachIndexed { index, name ->
                        FilterChip(
                            selected = equalizer.preset == index,
                            onClick = { onPresetSelected(index) },
                            label = { Text(name) },
                            shape = CircleShape,
                            colors = chipColors(),
                            border = null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val levels = equalizer.bandLevels
                    .takeIf { it.size == equalizerInfo.bandFrequenciesHz.size }
                    ?: List(equalizerInfo.bandFrequenciesHz.size) { 0 }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(SurfaceCard)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    equalizerInfo.bandFrequenciesHz.forEachIndexed { band, freq ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = formatFrequency(freq),
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary,
                                modifier = Modifier.width(64.dp)
                            )
                            ThinSlider(
                                value = levels[band].toFloat(),
                                onValueChange = { onBandLevelChange(band, it.toInt()) },
                                onValueChangeFinished = {},
                                valueRange = equalizerInfo.minLevel.toFloat()..equalizerInfo.maxLevel.toFloat(),
                                thumbColor = if (equalizer.enabled) AccentPrimary else TextMuted,
                                activeTrackColor = if (equalizer.enabled) AccentPrimary else TextMuted,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = formatDb(levels[band]),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = TextMuted,
                                modifier = Modifier.width(52.dp).padding(start = 8.dp)
                            )
                        }
                    }
                }

                TextButton(onClick = onResetEqualizer, modifier = Modifier.align(Alignment.End)) {
                    Text("Reset to flat", color = TextSecondary)
                }
            }
        }
    }
}

@Composable
internal fun chipColors() = FilterChipDefaults.filterChipColors(
    containerColor = SurfaceCard,
    labelColor = TextPrimary,
    selectedContainerColor = AccentFill,
    selectedLabelColor = OnAccentFill
)

fun formatSpeed(speed: Float): String =
    if (speed % 1f == 0f) "${speed.toInt()}x" else "${speed}x".replace(".0x", "x")

private fun formatFrequency(hz: Int): String =
    if (hz >= 1000) "${hz / 1000}${if (hz % 1000 >= 100) "." + (hz % 1000) / 100 else ""} kHz" else "$hz Hz"

/** Equalizer levels are in millibels. */
private fun formatDb(millibels: Int): String {
    val db = millibels / 100f
    return (if (db > 0) "+" else "") + String.format(Locale.US, "%.0f dB", db)
}
