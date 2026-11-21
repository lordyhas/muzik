package com.lordyhas.muzik.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.composables.icons.lucide.Disc3
import com.composables.icons.lucide.ListMusic
import com.composables.icons.lucide.ListPlus
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pin
import com.composables.icons.lucide.PinOff
import com.composables.icons.lucide.Tags
import com.composables.icons.lucide.UserRound
import com.lordyhas.muzik.data.model.Album
import com.lordyhas.muzik.data.model.Pin
import com.lordyhas.muzik.data.model.Song
import com.lordyhas.muzik.tageditor.TagEditorActivity
import com.lordyhas.muzik.ui.components.AlbumCard
import com.lordyhas.muzik.ui.components.DetailHero
import com.lordyhas.muzik.ui.components.DetailNotFound
import com.lordyhas.muzik.ui.components.DetailScaffold
import com.lordyhas.muzik.ui.components.MenuAction
import com.lordyhas.muzik.ui.components.OverflowMenu
import com.lordyhas.muzik.ui.components.SectionHeader
import com.lordyhas.muzik.ui.components.SongItem
import com.lordyhas.muzik.ui.components.pluralize
import com.lordyhas.muzik.ui.theme.AccentPrimary
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
    modifier: Modifier = Modifier,
    onAlbumClick: (Long) -> Unit = {}
) {
    BackHandler(enabled = true) {
        onBackClick()
    }

    val albums by viewModel.albums.collectAsState()
    val album: Album? = albums.find { it.id == albumId }

    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val likedIds by viewModel.likedSongIds.collectAsState()
    val pins by viewModel.pins.collectAsState()
    val context = LocalContext.current

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
    // Other albums credited to the same album artist; the section is hidden when there are none
    val moreFromArtist = remember(albums, album) { albums.filter { it.artist == album.artist && it.id != album.id } }
    val pin = Pin.album(album.id)
    val isPinned = pin in pins

    DetailScaffold(
        title = album.title,
        onBack = onBackClick,
        modifier = modifier,
        topBarArtworkUri = album.albumArtUri,
        actions = {
            OverflowMenu(
                contentDescription = "Album options",
                actions = listOf(
                    MenuAction("Add to queue", Lucide.ListMusic, AccentPrimary) { viewModel.addAllToQueue(songs) },
                    MenuAction("Add to playlist", Lucide.ListPlus, AccentPrimary) { viewModel.setSongsPendingAddToPlaylist(songs) },
                    MenuAction(if (isPinned) "Unpin from Home" else "Pin to Home", if (isPinned) Lucide.PinOff else Lucide.Pin) {
                        viewModel.togglePin(pin)
                    },
                    MenuAction("Go to artist", Lucide.UserRound) { onGoToArtist(album.artist) },
                    MenuAction("Edit tags", Lucide.Tags) {
                        context.startActivity(TagEditorActivity.intent(context, albumId = album.id))
                    }
                )
            )
        }
    ) {
        item(key = "hero") {
            DetailHero(
                artworkUri = album.albumArtUri,
                eyebrow = "",
                title = album.title,
                subtitle = album.artist,
                onSubtitleClick = { onGoToArtist(album.artist) },
                meta = listOfNotNull(
                    album.year?.toString(),
                    pluralize(songs.size, "song"),
                    formatTotalDuration(totalDuration)
                ).joinToString("  ·  "),
                placeholderIcon = Lucide.Disc3,
                onPlay = { viewModel.playAlbum(album) },
                onShuffle = {
                    val shuffled = songs.shuffled()
                    if (shuffled.isNotEmpty()) viewModel.playSong(shuffled.first(), shuffled)
                }
            )
        }

        item(key = "songs_header") {
            SectionHeader(title = "Songs", modifier = Modifier.padding(top = 8.dp))
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

        if (moreFromArtist.isNotEmpty()) {
            item(key = "more_from_artist") {
                Column(modifier = Modifier.padding(top = 20.dp)) {
                    SectionHeader(title = "More from ${album.artist}")
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        items(moreFromArtist, key = { it.id }) { other ->
                            AlbumCard(
                                album = other,
                                isCurrent = currentSong?.albumId == other.id,
                                isPlaying = isPlaying,
                                onClick = { onAlbumClick(other.id) },
                                modifier = Modifier.width(150.dp)
                            )
                        }
                    }
                }
            }
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
                imageVector = Lucide.Disc3,
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
