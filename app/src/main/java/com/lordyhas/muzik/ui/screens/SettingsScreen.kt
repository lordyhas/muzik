package com.lordyhas.muzik.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.lordyhas.muzik.service.SleepTimer
import com.lordyhas.muzik.ui.components.AudioEffectsSheet
import com.lordyhas.muzik.ui.components.CircleIconButton
import com.lordyhas.muzik.ui.components.LibraryFiltersSheet
import com.lordyhas.muzik.ui.components.ScreenHeader
import com.lordyhas.muzik.ui.components.SettingsGroup
import com.lordyhas.muzik.ui.components.SettingsRow
import com.lordyhas.muzik.ui.components.SettingsRowTrailing
import com.lordyhas.muzik.ui.components.SleepTimerSheet
import com.lordyhas.muzik.ui.components.pluralize
import com.lordyhas.muzik.ui.components.rememberSleepRemaining
import com.lordyhas.muzik.ui.theme.AccentCyan
import com.lordyhas.muzik.ui.theme.AccentSecondary
import com.lordyhas.muzik.ui.theme.BackgroundDark
import com.lordyhas.muzik.ui.viewmodel.MusicViewModel

/**
 * One place for everything that changes how Muzik behaves. Each row opens the same sheet or
 * screen that's also reachable from where it's used (Folders, the Now Playing "Sleep" and
 * "Audio" buttons), so nothing has to be configured twice.
 */
@UnstableApi
@Composable
fun SettingsScreen(
    viewModel: MusicViewModel,
    onBackClick: () -> Unit,
    onFoldersClick: () -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val songs by viewModel.songs.collectAsState()
    val hiddenSongCount by viewModel.hiddenSongCount.collectAsState()
    val folders by viewModel.allFolders.collectAsState()
    val filter by viewModel.libraryFilter.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val sleepTimer by viewModel.sleepTimer.collectAsState()
    val playbackSpeed by viewModel.playbackSpeed.collectAsState()
    val equalizer by viewModel.equalizer.collectAsState()
    val equalizerInfo by viewModel.equalizerInfo.collectAsState()

    var showFilters by remember { mutableStateOf(false) }
    var showSleepTimer by remember { mutableStateOf(false) }
    var showAudioEffects by remember { mutableStateOf(false) }

    val hiddenFolderCount = folders.count { it.hidden }
    val sleepRemaining = rememberSleepRemaining(sleepTimer)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .navigationBarsPadding()
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                CircleIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    onClick = onBackClick
                )
            }
            ScreenHeader(title = "Settings", subtitle = null)
        }

        SettingsGroup(title = "Library") {
            SettingsRow(
                icon = Icons.Default.Folder,
                title = "Folders",
                subtitle = if (hiddenFolderCount > 0) {
                    pluralize(hiddenFolderCount, "hidden folder")
                } else {
                    "Choose which folders show up in Muzik"
                },
                onClick = onFoldersClick
            )
            SettingsRow(
                icon = Icons.Default.Tune,
                title = "Library filters",
                subtitle = libraryFiltersSummary(filter.hideRecordings, filter.minDurationMs),
                onClick = { showFilters = true }
            )
            SettingsRow(
                icon = Icons.Default.Refresh,
                title = if (isRefreshing) "Scanning…" else "Rescan library",
                subtitle = pluralize(songs.size, "song") +
                    if (hiddenSongCount > 0) " · $hiddenSongCount hidden" else "",
                onClick = { viewModel.refreshLibrary(announce = true) },
                trailing = SettingsRowTrailing.None,
                showDivider = false
            )
        }

        SettingsGroup(title = "Playback") {
            SettingsRow(
                icon = Icons.Default.Bedtime,
                title = "Sleep timer",
                subtitle = when (sleepTimer) {
                    SleepTimer.Off -> "Off"
                    SleepTimer.EndOfTrack -> "Stops at the end of this song"
                    is SleepTimer.At -> "Stops in " + formatRemaining(sleepRemaining ?: 0L)
                },
                iconTint = AccentSecondary,
                onClick = { showSleepTimer = true }
            )
            SettingsRow(
                icon = Icons.Default.Equalizer,
                title = "Equalizer & speed",
                subtitle = (if (equalizer.enabled) "Equalizer on" else "Equalizer off") +
                    " · " + formatSpeed(playbackSpeed),
                iconTint = AccentSecondary,
                onClick = { showAudioEffects = true },
                showDivider = false
            )
        }

        SettingsGroup(title = "About") {
            SettingsRow(
                icon = Icons.Default.Info,
                title = "About Muzik",
                subtitle = "Version ${viewModel.appVersion.name}",
                iconTint = AccentCyan,
                onClick = onAboutClick,
                showDivider = false
            )
        }
    }

    if (showFilters) {
        LibraryFiltersSheet(
            filter = filter,
            hiddenSongCount = hiddenSongCount,
            onHideRecordings = viewModel::setHideRecordings,
            onMinDuration = viewModel::setMinDuration,
            onDismiss = { showFilters = false }
        )
    }

    if (showSleepTimer) {
        SleepTimerSheet(
            timer = sleepTimer,
            onSetMinutes = { viewModel.setSleepTimer(it) },
            onEndOfTrack = { viewModel.setSleepAtEndOfTrack() },
            onCancel = { viewModel.cancelSleepTimer() },
            onDismiss = { showSleepTimer = false }
        )
    }

    if (showAudioEffects) {
        AudioEffectsSheet(
            speed = playbackSpeed,
            onSpeedChange = { viewModel.setPlaybackSpeed(it) },
            equalizer = equalizer,
            equalizerInfo = equalizerInfo,
            onEqualizerEnabled = { viewModel.setEqualizerEnabled(it) },
            onPresetSelected = { viewModel.selectEqualizerPreset(it) },
            onBandLevelChange = { band, level -> viewModel.setEqualizerBandLevel(band, level) },
            onResetEqualizer = { viewModel.resetEqualizer() },
            onDismiss = { showAudioEffects = false }
        )
    }
}

internal fun libraryFiltersSummary(hideRecordings: Boolean, minDurationMs: Long): String {
    val parts = buildList {
        if (hideRecordings) add("Recordings hidden")
        if (minDurationMs > 0) add("Skipping clips under ${minDurationMs / 1000}s")
    }
    return parts.joinToString(" · ").ifEmpty { "Showing all audio" }
}

internal fun formatSpeed(speed: Float): String {
    val text = if (speed % 1f == 0f) speed.toInt().toString() else speed.toString().trimEnd('0')
    return "${text}x speed"
}

private fun formatRemaining(millis: Long): String {
    val minutes = ((millis + 59_999) / 60_000).coerceAtLeast(1)
    return pluralize(minutes.toInt(), "minute")
}
