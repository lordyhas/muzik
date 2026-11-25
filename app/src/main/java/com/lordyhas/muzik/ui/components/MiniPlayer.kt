package com.lordyhas.muzik.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.lordyhas.muzik.data.model.Song
import com.lordyhas.muzik.ui.theme.BackgroundDark
import com.lordyhas.muzik.ui.theme.StrokeSubtle
import com.lordyhas.muzik.ui.theme.SurfaceElevated
import com.lordyhas.muzik.ui.theme.TextPrimary
import com.lordyhas.muzik.ui.theme.TextSecondary
import com.lordyhas.muzik.ui.theme.TrackBackground
import com.lordyhas.muzik.ui.theme.rememberArtworkColor
import kotlin.math.abs
import kotlinx.coroutines.launch

@Composable
fun MiniPlayer(
    song: Song?,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onClick: () -> Unit,
    onSkipPrevious: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (song == null) return

    val accent = rememberArtworkColor(song.artworkUri)
    val progress = if (duration > 0) {
        (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
    } else 0f
    val shape = RoundedCornerShape(18.dp)

    // Swipe the mini player left/right to skip tracks
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val thresholdPx = with(LocalDensity.current) { 72.dp.toPx() }
    val currentNext by rememberUpdatedState(onSkipNext)
    val currentPrevious by rememberUpdatedState(onSkipPrevious)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .shadow(20.dp, shape, spotColor = accent.copy(alpha = 0.5f))
            .clip(shape)
            .background(SurfaceElevated)
            .background(
                Brush.horizontalGradient(
                    listOf(accent.copy(alpha = 0.38f), accent.copy(alpha = 0.10f))
                )
            )
            .border(1.dp, StrokeSubtle, shape)
            .clickable(onClick = onClick)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        val x = offsetX.value
                        scope.launch {
                            if (x < -thresholdPx) currentNext() else if (x > thresholdPx) currentPrevious()
                            offsetX.animateTo(0f, spring(dampingRatio = 0.7f))
                        }
                    },
                    onDragCancel = { scope.launch { offsetX.animateTo(0f) } },
                    onHorizontalDrag = { change, amount ->
                        change.consume()
                        scope.launch { offsetX.snapTo(offsetX.value + amount * 0.6f) }
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier.graphicsLayer {
                translationX = offsetX.value
                alpha = 1f - (abs(offsetX.value) / (thresholdPx * 3)).coerceIn(0f, 0.6f)
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 6.dp, top = 8.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Artwork(
                    uri = song.artworkUri,
                    shape = RoundedCornerShape(12.dp),
                    iconSize = 22.dp,
                    placeholderTint = accent,
                    contentDescription = "Album artwork",
                    modifier = Modifier.size(46.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Crossfade title/artist on track change
                AnimatedContent(
                    targetState = song,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    contentKey = { it.id },
                    label = "mini_title",
                    modifier = Modifier.weight(1f)
                ) { s ->
                    Column {
                        Text(
                            text = s.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = s.artist,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Lucide.PauseFilled else Lucide.PlayFilled,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = BackgroundDark,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(2.dp))

                IconButton(
                    onClick = onSkipNext,
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        imageVector = Lucide.SkipForwardFilled,
                        contentDescription = "Next",
                        tint = TextPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .padding(bottom = 6.dp)
                    .height(3.dp)
                    .clip(CircleShape),
                color = accent,
                trackColor = TrackBackground,
                strokeCap = StrokeCap.Round,
                gapSize = 0.dp,
                drawStopIndicator = {}
            )
        }
    }
}
