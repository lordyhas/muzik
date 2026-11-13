package com.lordyhas.muzik.ui.components

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import com.lordyhas.muzik.data.model.Song
import com.lordyhas.muzik.data.repository.LyricsState
import com.lordyhas.muzik.service.SleepTimer
import com.lordyhas.muzik.ui.theme.BackgroundDark
import com.lordyhas.muzik.ui.theme.DangerRed
import com.lordyhas.muzik.ui.theme.StrokeSubtle
import com.lordyhas.muzik.ui.theme.SurfaceElevated
import com.lordyhas.muzik.ui.theme.TextMuted
import com.lordyhas.muzik.ui.theme.TextPrimary
import com.lordyhas.muzik.ui.theme.TextSecondary
import com.lordyhas.muzik.ui.theme.rememberArtworkColor
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs

@UnstableApi
@Composable
fun NowPlayingScreen(
    song: Song?,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    isShuffle: Boolean,
    repeatMode: Int,
    queue: List<Song>,
    currentIndex: Int,
    userQueueCount: Int,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onPlayIndex: (Int) -> Unit,
    onRemoveIndex: (Int) -> Unit,
    onMoveQueueItem: (from: Int, to: Int) -> Unit,
    sleepTimer: SleepTimer,
    playbackSpeed: Float,
    onSleepTimerClick: () -> Unit,
    onAudioEffectsClick: () -> Unit,
    isLiked: Boolean,
    onToggleLike: () -> Unit,
    showLyrics: Boolean,
    lyricsState: LyricsState,
    onToggleLyrics: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onGoToAlbum: () -> Unit,
    onGoToArtist: () -> Unit,
    onShowDetails: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (song == null) return

    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()
    val accent = rememberArtworkColor(song.albumArtUri)

    // Back: from the queue page return to the player; from the player, dismiss
    BackHandler(enabled = true) {
        if (pagerState.currentPage > 0) {
            coroutineScope.launch { pagerState.animateScrollToPage(0) }
        } else {
            onDismiss()
        }
    }

    // Slider drag state
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }

    val actualProgress = if (duration > 0) {
        (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
    } else 0f
    val sliderProgress = if (isDragging) dragProgress else actualProgress

    // Album art gently shrinks when paused
    val artScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.0f else 0.88f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "art_scale"
    )

    val nextSong = queue.getOrNull(currentIndex + 1)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { /* consume clicks */ }
    ) {
        // ── Ambient backdrop: blurred artwork + accent gradient ──────────────
        if (song.albumArtUri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AsyncImage(
                model = song.albumArtUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(80.dp)
                    .alpha(0.45f)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            accent.copy(alpha = 0.55f),
                            accent.copy(alpha = 0.18f),
                            BackgroundDark.copy(alpha = 0.92f),
                            BackgroundDark
                        )
                    )
                )
        )

        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1
        ) { page ->
            when (page) {
                // ═══════════════════════════════════════════════════════════════
                // PAGE 0 — Full Player
                // ═══════════════════════════════════════════════════════════════
                0 -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // ── Top Bar ──────────────────────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircleIconButton(
                            icon = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Close player",
                            onClick = onDismiss,
                            size = 44.dp,
                            iconSize = 28.dp,
                            background = Color.White.copy(alpha = 0.08f)
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "PLAYING FROM",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextPrimary.copy(alpha = 0.6f)
                            )
                            Text(
                                text = song.album,
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                        // claude do not un comment this.
                        /*CircleIconButton(
                            icon = Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = "Open queue",
                            onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                            size = 44.dp,
                            iconSize = 22.dp,
                            background = Color.White.copy(alpha = 0.08f)
                        )*/

                        Box {
                            var isMenuExpanded by remember { mutableStateOf(false) }
                            CircleIconButton(
                                icon = Icons.Default.MoreVert,
                                contentDescription = "More options",
                                onClick = { isMenuExpanded = true },
                                size = 44.dp,
                                iconSize = 22.dp,
                                background = Color.White.copy(alpha = 0.08f)
                            )

                            DropdownMenu(
                                expanded = isMenuExpanded,
                                onDismissRequest = { isMenuExpanded = false },
                                modifier = Modifier.background(SurfaceElevated)
                            ) {
                                @Composable
                                fun MenuItem(label: String, icon: ImageVector, tint: Color, action: () -> Unit) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                label,
                                                color = TextPrimary,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
                                        },
                                        onClick = {
                                            isMenuExpanded = false
                                            action()
                                        }
                                    )
                                }

                                MenuItem("Add to playlist", Icons.AutoMirrored.Filled.PlaylistAdd, accent, onAddToPlaylist)
                                MenuItem("Queue", Icons.AutoMirrored.Filled.QueueMusic, accent) {
                                    coroutineScope.launch { pagerState.animateScrollToPage(1) }
                                }
                                MenuItem("Go to album", Icons.Default.Album, TextSecondary, onGoToAlbum)
                                MenuItem("Go to artist", Icons.Default.Person, TextSecondary, onGoToArtist)
                                MenuItem("Song details", Icons.Default.Info, TextSecondary, onShowDetails)
                            }
                        }
                    }

                    // ── Lyrics panel or album art (swipe left/right to skip) ──
                    if (showLyrics) {
                        LyricsPanel(
                            state = lyricsState,
                            positionMs = currentPosition,
                            accent = accent,
                            onSeek = onSeekTo,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                        )
                    } else SwipeableCover(
                        songKey = song.id,
                        onSwipeNext = onSkipNext,
                        onSwipePrevious = onSkipPrevious
                    ) { coverModifier ->
                    Artwork(
                        uri = song.albumArtUri,
                        shape = RoundedCornerShape(24.dp),
                        iconSize = 96.dp,
                        placeholderTint = accent,
                        contentDescription = "Album artwork",
                        modifier = coverModifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .scale(artScale)
                            .shadow(
                                elevation = 40.dp,
                                shape = RoundedCornerShape(24.dp),
                                spotColor = accent,
                                ambientColor = accent
                            )
                            .border(1.dp, StrokeSubtle, RoundedCornerShape(24.dp))
                    )
                    }

                    // ── Title, Artist & Progress ─────────────────────────
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = song.title,
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = song.artist,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextPrimary.copy(alpha = 0.7f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            LikeButton(isLiked = isLiked, accent = accent, onClick = onToggleLike)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        ThinSlider(
                            value = sliderProgress,
                            onValueChange = {
                                isDragging = true
                                dragProgress = it
                            },
                            onValueChangeFinished = {
                                onSeekTo((dragProgress * duration).toLong())
                                isDragging = false
                            },
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                            thumbSize = if (isDragging) 18.dp else 14.dp,
                            trackHeight = 4.dp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val displayed = if (isDragging) (dragProgress * duration).toLong() else currentPosition
                            Text(
                                text = formatTime(displayed),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextPrimary.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "-" + formatTime((duration - displayed).coerceAtLeast(0)),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextPrimary.copy(alpha = 0.6f)
                            )
                        }
                    }

                    // ── Playback Controls ────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ToggleControl(
                            icon = Icons.Default.Shuffle,
                            contentDescription = if (isShuffle) "Shuffle on" else "Shuffle off",
                            active = isShuffle,
                            accent = accent,
                            onClick = onToggleShuffle
                        )

                        IconButton(onClick = onSkipPrevious, modifier = Modifier.size(56.dp)) {
                            Icon(
                                Icons.Default.SkipPrevious, "Previous",
                                tint = TextPrimary,
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        IconButton(
                            onClick = onTogglePlayPause,
                            modifier = Modifier
                                .size(78.dp)
                                .shadow(24.dp, CircleShape, spotColor = accent)
                                .clip(CircleShape)
                                .background(Color.White)
                        ) {
                            Icon(
                                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                if (isPlaying) "Pause" else "Play",
                                tint = BackgroundDark,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        IconButton(onClick = onSkipNext, modifier = Modifier.size(56.dp)) {
                            Icon(
                                Icons.Default.SkipNext, "Next",
                                tint = TextPrimary,
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        val repeatIcon = if (repeatMode == Player.REPEAT_MODE_ONE) Icons.Default.RepeatOne else Icons.Default.Repeat
                        val repeatLabel = when (repeatMode) {
                            Player.REPEAT_MODE_ONE -> "Repeat one"
                            Player.REPEAT_MODE_ALL -> "Repeat all"
                            else -> "Repeat off"
                        }
                        ToggleControl(
                            icon = repeatIcon,
                            contentDescription = repeatLabel,
                            active = repeatMode != Player.REPEAT_MODE_OFF,
                            accent = accent,
                            onClick = onToggleRepeat
                        )
                    }

                    // ── Secondary actions ───────────────────────────────
                    val sleepRemaining = rememberSleepRemaining(sleepTimer)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        PlayerActionChip(
                            icon = Icons.Default.Bedtime,
                            label = when {
                                sleepRemaining != null -> formatCountdown(sleepRemaining)
                                sleepTimer == SleepTimer.EndOfTrack -> "End of song"
                                else -> "Sleep"
                            },
                            active = sleepTimer != SleepTimer.Off,
                            accent = accent,
                            onClick = onSleepTimerClick
                        )
                        PlayerActionChip(
                            icon = Icons.Default.Lyrics,
                            label = "Lyrics",
                            active = showLyrics,
                            accent = accent,
                            onClick = onToggleLyrics
                        )
                        PlayerActionChip(
                            icon = Icons.Default.Tune,
                            label = if (playbackSpeed != 1f) formatSpeed(playbackSpeed) else "Audio",
                            active = playbackSpeed != 1f,
                            accent = accent,
                            onClick = onAudioEffectsClick
                        )
                    }

                    // ── Up Next Peek ─────────────────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.06f))
                            .clickable { coroutineScope.launch { pagerState.animateScrollToPage(1) } }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "UP NEXT",
                                style = MaterialTheme.typography.labelSmall,
                                color = accent
                            )
                            Text(
                                text = nextSong?.let { "${it.title} • ${it.artist}" } ?: "End of queue",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            Icons.Default.KeyboardArrowUp, "Swipe up for queue",
                            tint = TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // ═══════════════════════════════════════════════════════════════
                // PAGE 1 — Queue with compact mini-player header
                // ═══════════════════════════════════════════════════════════════
                else -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BackgroundDark.copy(alpha = 0.85f))
                        .statusBarsPadding()
                ) {
                    // ── Compact Mini-Player Header ───────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { coroutineScope.launch { pagerState.animateScrollToPage(0) } }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Artwork(
                            uri = song.albumArtUri,
                            shape = RoundedCornerShape(12.dp),
                            iconSize = 22.dp,
                            placeholderTint = accent,
                            modifier = Modifier.size(50.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = song.artist,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = onTogglePlayPause,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        ) {
                            Icon(
                                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                if (isPlaying) "Pause" else "Play",
                                tint = BackgroundDark,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(onClick = onSkipNext, modifier = Modifier.size(42.dp)) {
                            Icon(
                                Icons.Default.SkipNext, "Next",
                                tint = TextPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    // ── Queue Track List ─────────────────────────────────
                    if (queue.isEmpty()) {
                        EmptyState(
                            icon = Icons.AutoMirrored.Filled.QueueMusic,
                            title = "Queue is empty",
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        val reorderState = rememberReorderState()
                        // Stable row identities: a song queued twice is "id#1" and "id#2", so removing
                        // or moving one song doesn't re-key every row below it (which kills animations)
                        val queueKeys = remember(queue) {
                            val seen = HashMap<Long, Int>()
                            queue.map { s ->
                                val n = (seen[s.id] ?: 0) + 1
                                seen[s.id] = n
                                "${s.id}#$n"
                            }
                        }
                        val removeAction = { index: Int ->
                            SwipeAction(
                                icon = Icons.Default.Delete,
                                label = "Remove from queue",
                                color = DangerRed,
                                dismiss = true,
                                onTrigger = { onRemoveIndex(index) }
                            )
                        }
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(bottom = 48.dp)
                        ) {
                            // 1. Now playing
                            if (currentIndex in queue.indices) {
                                item(key = "now_playing") {
                                    QueueSectionLabel("NOW PLAYING", color = accent, topPadding = 8.dp)
                                    QueueSongRow(
                                        song = queue[currentIndex],
                                        isCurrent = true,
                                        isPlaying = isPlaying,
                                        accent = accent,
                                        onClick = { onPlayIndex(currentIndex) },
                                        onRemove = null
                                    )
                                }
                            }

                            // 2. Songs the user queued manually
                            if (userQueueCount > 0 && currentIndex + 1 < queue.size) {
                                item(key = "label_user_queue") {
                                    QueueSectionLabel("NEXT IN QUEUE", color = TextPrimary, modifier = Modifier.animateItem())
                                }

                                val uqStart = currentIndex + 1
                                val uqEnd = (uqStart + userQueueCount).coerceAtMost(queue.size)

                                items(
                                    count = uqEnd - uqStart,
                                    key = { i -> queueKeys[uqStart + i] }
                                ) { i ->
                                    val absoluteIndex = uqStart + i
                                    val rowKey = queueKeys[absoluteIndex]
                                    ReorderableRow(
                                        state = reorderState,
                                        key = rowKey,
                                        onMove = { steps ->
                                            val target = (absoluteIndex + steps).coerceIn(uqStart, uqEnd - 1)
                                            if (target != absoluteIndex) onMoveQueueItem(absoluteIndex, target)
                                        },
                                        modifier = Modifier.animateItem(placementSpec = reorderState.placementSpec(rowKey))
                                    ) { handle, dragging ->
                                      SwipeActionBox(endToStart = removeAction(absoluteIndex)) {
                                        QueueSongRow(
                                            song = queue[absoluteIndex],
                                            isCurrent = false,
                                            isPlaying = false,
                                            accent = accent,
                                            onClick = { onPlayIndex(absoluteIndex) },
                                            onRemove = { onRemoveIndex(absoluteIndex) },
                                            handleModifier = handle,
                                            isDragging = dragging
                                        )
                                      }
                                    }
                                }
                            }

                            // 3. Rest of the album / playlist being played
                            val contextStart = currentIndex + 1 + userQueueCount
                            if (contextStart < queue.size) {
                                item(key = "label_context") {
                                    QueueSectionLabel(
                                        "NEXT FROM: " + song.album.ifEmpty { "Library" }.uppercase(),
                                        color = TextSecondary,
                                        modifier = Modifier.animateItem()
                                    )
                                }

                                items(
                                    count = queue.size - contextStart,
                                    key = { i -> queueKeys[contextStart + i] }
                                ) { i ->
                                    val absoluteIndex = contextStart + i
                                    val rowKey = queueKeys[absoluteIndex]
                                    ReorderableRow(
                                        state = reorderState,
                                        key = rowKey,
                                        onMove = { steps ->
                                            val target = (absoluteIndex + steps).coerceIn(contextStart, queue.size - 1)
                                            if (target != absoluteIndex) onMoveQueueItem(absoluteIndex, target)
                                        },
                                        modifier = Modifier.animateItem(placementSpec = reorderState.placementSpec(rowKey))
                                    ) { handle, dragging ->
                                      SwipeActionBox(endToStart = removeAction(absoluteIndex)) {
                                        QueueSongRow(
                                            song = queue[absoluteIndex],
                                            isCurrent = false,
                                            isPlaying = false,
                                            accent = accent,
                                            onClick = { onPlayIndex(absoluteIndex) },
                                            onRemove = { onRemoveIndex(absoluteIndex) },
                                            handleModifier = handle,
                                            isDragging = dragging
                                        )
                                      }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Building blocks ────────────────────────────────────────────────────────

/** Shuffle / repeat button with an accent dot when active. */
@Composable
private fun ToggleControl(
    icon: ImageVector,
    contentDescription: String,
    active: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick, modifier = Modifier.size(48.dp)) {
            Icon(
                icon, contentDescription,
                tint = if (active) accent else TextPrimary.copy(alpha = 0.55f),
                modifier = Modifier.size(26.dp)
            )
        }
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(if (active) accent else Color.Transparent)
        )
    }
}

@Composable
private fun QueueSectionLabel(
    text: String,
    color: Color,
    topPadding: androidx.compose.ui.unit.Dp = 20.dp,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier.padding(start = 20.dp, end = 20.dp, top = topPadding, bottom = 6.dp)
    )
}

@Composable
private fun QueueSongRow(
    song: Song,
    isCurrent: Boolean,
    isPlaying: Boolean,
    accent: Color,
    onClick: () -> Unit,
    onRemove: (() -> Unit)?,
    modifier: Modifier = Modifier,
    handleModifier: Modifier? = null,
    isDragging: Boolean = false
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 1.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                when {
                    isDragging -> SurfaceElevated
                    isCurrent -> accent.copy(alpha = 0.14f)
                    else -> Color.Transparent
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.Center) {
            Artwork(
                uri = song.albumArtUri,
                shape = RoundedCornerShape(10.dp),
                iconSize = 20.dp,
                modifier = Modifier.size(48.dp)
            )
            if (isCurrent) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    EqualizerBars(isAnimating = isPlaying, color = accent, modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold
                ),
                color = if (isCurrent) accent else TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${song.artist} • ${song.formattedDuration()}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (onRemove != null) {
            IconButton(onClick = onRemove, modifier = Modifier.size(34.dp)) {
                Icon(
                    Icons.Default.Close, "Remove from queue",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        if (handleModifier != null) {
            DragHandleIcon(modifier = handleModifier)
        }
    }
}

/** Heart that pops when toggled on. */
@Composable
private fun LikeButton(isLiked: Boolean, accent: Color, onClick: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    val scale by animateFloatAsState(
        targetValue = if (isLiked) 1f else 0.9f,
        animationSpec = spring(dampingRatio = 0.35f, stiffness = 400f),
        label = "like_scale"
    )
    IconButton(
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = Modifier.size(48.dp)
    ) {
        Icon(
            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = if (isLiked) "Remove from Liked Songs" else "Add to Liked Songs",
            tint = if (isLiked) accent else TextPrimary.copy(alpha = 0.8f),
            modifier = Modifier
                .size(28.dp)
                .scale(scale)
        )
    }
}

/** Pill button under the player controls (sleep timer, audio effects, ...). */
@Composable
private fun PlayerActionChip(
    icon: ImageVector,
    label: String,
    active: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (active) accent.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.06f))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon, contentDescription = null,
            tint = if (active) accent else TextPrimary.copy(alpha = 0.8f),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (active) accent else TextPrimary.copy(alpha = 0.8f)
        )
    }
}

/**
 * Wraps the cover so it can be flicked horizontally: left = next, right = previous.
 * The cover follows the finger and springs back on release.
 */
@Composable
private fun SwipeableCover(
    songKey: Long,
    onSwipeNext: () -> Unit,
    onSwipePrevious: () -> Unit,
    content: @Composable (Modifier) -> Unit
) {
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val thresholdPx = with(LocalDensity.current) { 96.dp.toPx() }
    val currentNext by rememberUpdatedState(onSwipeNext)
    val currentPrevious by rememberUpdatedState(onSwipePrevious)

    // New song: settle back to center
    LaunchedEffect(songKey) { offsetX.animateTo(0f, spring(dampingRatio = 0.75f)) }

    content(
        Modifier
            .graphicsLayer {
                translationX = offsetX.value
                rotationZ = offsetX.value / 60f
                alpha = 1f - (abs(offsetX.value) / (thresholdPx * 4)).coerceIn(0f, 0.5f)
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        val x = offsetX.value
                        scope.launch {
                            when {
                                x < -thresholdPx -> {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    currentNext()
                                }
                                x > thresholdPx -> {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    currentPrevious()
                                }
                            }
                            offsetX.animateTo(0f, spring(dampingRatio = 0.75f))
                        }
                    },
                    onDragCancel = { scope.launch { offsetX.animateTo(0f) } },
                    onHorizontalDrag = { change, amount ->
                        change.consume()
                        scope.launch { offsetX.snapTo(offsetX.value + amount) }
                    }
                )
            }
    )
}

// ─── Helpers ────────────────────────────────────────────────────────────────

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}
