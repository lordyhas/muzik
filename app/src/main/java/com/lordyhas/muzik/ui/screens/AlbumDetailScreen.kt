package com.lordyhas.muzik.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.lordyhas.muzik.data.model.Album
import com.lordyhas.muzik.data.model.Song
import com.lordyhas.muzik.ui.components.DetailHero
import com.lordyhas.muzik.ui.components.DetailNotFound
import com.lordyhas.muzik.ui.components.DetailScaffold
import com.lordyhas.muzik.ui.components.SongItem
import com.lordyhas.muzik.ui.components.pluralize
import com.lordyhas.muzik.ui.theme.AccentSecondary
import com.lordyhas.muzik.ui.theme.SurfaceElevated
import com.lordyhas.muzik.ui.theme.TextMuted
import com.lordyhas.muzik.ui.theme.TextPrimary
import com.lordyhas.muzik.ui.viewmodel.MusicViewModel

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(UnstableApi::class)
@Composable
fun AlbumDetailScreen(
    albumId: Long,
    viewModel: MusicViewModel,
    onBackClick: () -> Unit,
    onGoToArtist: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = true) {
        onBackClick()
    }

    val albums by viewModel.albums.collectAsState()
    val album: Album? = albums.find { it.id == albumId }

    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val likedIds by viewModel.likedSongIds.collectAsState()

    if (album == null) {
        DetailNotFound(message = "Album not found", onBack = onBackClick, modifier = modifier)
        return
    }

    val songs = album.songs
    val songsByDisc = remember(songs) {
        songs.groupBy { it.discNumber ?: 1 }.toSortedMap()
    }
    val hasMultipleDiscs = songsByDisc.size > 1
    val totalDuration = remember(songs) { songs.sumOf { it.duration } }

    DetailScaffold(title = album.title, onBack = onBackClick, modifier = modifier) {
        item(key = "hero") {
            DetailHero(
                artworkUri = album.albumArtUri,
                eyebrow = "Album",
                title = album.title,
                subtitle = album.artist,
                onSubtitleClick = { onGoToArtist(album.artist) },
                meta = "${pluralize(songs.size, "song")} • ${formatTotalDuration(totalDuration)}",
                placeholderIcon = Icons.Default.Album,
                onPlay = { viewModel.playAlbum(album) },
                onAddToQueue = { viewModel.addAllToQueue(songs) },
                onShuffle = {
                    val shuffled = songs.shuffled()
                    if (shuffled.isNotEmpty()) viewModel.playSong(shuffled.first(), shuffled)
                }
            )
        }

        val songRow: @Composable (Song) -> Unit = { song ->
            val isSelected = currentSong?.id == song.id
            SongItem(
                song = song,
                isSelected = isSelected,
                isPlaying = isPlaying && isSelected,
                onClick = { viewModel.playAlbum(album, song) },
                onAddToQueue = { viewModel.addToQueue(song) },
                isLiked = song.id in likedIds,
                onToggleLike = { viewModel.toggleLike(song) },
                onAddToPlaylist = { viewModel.setSongPendingAddToPlaylist(song) },
                onGoToArtist = { onGoToArtist(song.artist) },
                onShowDetails = { viewModel.selectSongForDetails(song) },
                showTrackNumber = true,
                showArtwork = false,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        if (hasMultipleDiscs) {
            songsByDisc.forEach { (discNumber, discSongs) ->
                item(key = "disc_header_$discNumber") {
                    DiscHeader(discNumber = discNumber, trackCount = discSongs.size)
                }
                items(discSongs, key = { it.contentUri.toString() }) { song -> songRow(song) }
            }
        } else {
            items(songs, key = { it.contentUri.toString() }) { song -> songRow(song) }
        }
    }
}

@Composable
private fun DiscHeader(
    discNumber: Int,
    trackCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 4.dp, start = 20.dp, end = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(AccentSecondary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Album,
                contentDescription = null,
                tint = AccentSecondary,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = "Disc $discNumber",
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "• ${pluralize(trackCount, "track")}",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )

        Spacer(modifier = Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(Brush.horizontalGradient(listOf(SurfaceElevated, Color.Transparent)))
        )
    }
}

/** "42 min", "1 hr 12 min" */
internal fun formatTotalDuration(ms: Long): String {
    val totalMinutes = ms / 60_000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "$hours hr $minutes min" else "$minutes min"
}
