package com.lordyhas.muzik.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MicVocal
import com.lordyhas.muzik.data.repository.Lyrics
import com.lordyhas.muzik.data.repository.LyricsState
import com.lordyhas.muzik.ui.theme.TextPrimary

/**
 * Replaces the cover on Now Playing. Synced lyrics follow playback (current line highlighted and
 * kept in view; tap a line to jump there). Plain lyrics simply scroll.
 */
@Composable
fun LyricsPanel(
    state: LyricsState,
    positionMs: Long,
    accent: Color,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Black.copy(alpha = 0.28f)),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            LyricsState.Hidden, LyricsState.Loading -> CircularProgressIndicator(color = accent, strokeWidth = 3.dp)
            LyricsState.NotFound -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(28.dp)
            ) {
                Icon(Lucide.MicVocal, contentDescription = null, tint = accent, modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("No lyrics found", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Embed lyrics in the song's tags, or put a .lrc file with the same name next to the song.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
            is LyricsState.Loaded -> {
                if (state.lyrics.synced) {
                    SyncedLyrics(state.lyrics, positionMs, accent, onSeek)
                } else {
                    PlainLyrics(state.lyrics)
                }
            }
        }
    }
}

@Composable
private fun SyncedLyrics(
    lyrics: Lyrics,
    positionMs: Long,
    accent: Color,
    onSeek: (Long) -> Unit
) {
    val listState = rememberLazyListState()
    // Small look-ahead so the highlight doesn't lag behind the 500 ms position updates
    val currentIndex = remember(lyrics, positionMs) {
        lyrics.lines.indexOfLast { (it.timeMs ?: 0L) <= positionMs + 250 }
    }

    LaunchedEffect(currentIndex) {
        if (currentIndex >= 0 && !listState.isScrollInProgress) {
            listState.animateScrollToItem(currentIndex)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = maxHeight * 0.35f,
                bottom = maxHeight * 0.6f
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            itemsIndexed(lyrics.lines) { index, line ->
                val isCurrent = index == currentIndex
                val color by animateColorAsState(
                    targetValue = when {
                        isCurrent -> TextPrimary
                        index < currentIndex -> TextPrimary.copy(alpha = 0.45f)
                        else -> TextPrimary.copy(alpha = 0.3f)
                    },
                    label = "lyric_color"
                )
                val scale by animateFloatAsState(if (isCurrent) 1f else 0.94f, label = "lyric_scale")
                Text(
                    text = line.text.ifBlank { "♪" },
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Bold
                    ),
                    color = if (isCurrent && line.text.isBlank()) accent else color,
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(scale)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { line.timeMs?.let(onSeek) }
                        .padding(vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun PlainLyrics(lyrics: Lyrics) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        itemsIndexed(lyrics.lines) { _, line ->
            Text(
                text = line.text,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary.copy(alpha = 0.9f)
            )
        }
    }
}
