package com.lordyhas.muzik.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.composables.icons.lucide.LibraryBig
import com.composables.icons.lucide.ListMusic
import com.composables.icons.lucide.ListVideo
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Pin
import com.composables.icons.lucide.PinOff
import com.lordyhas.muzik.data.model.Pin
import com.lordyhas.muzik.data.model.Playlist
import com.lordyhas.muzik.ui.components.DetailHero
import com.lordyhas.muzik.ui.components.DetailNotFound
import com.lordyhas.muzik.ui.components.DetailScaffold
import com.lordyhas.muzik.ui.components.EmptyState
import com.lordyhas.muzik.ui.components.MenuAction
import com.lordyhas.muzik.ui.components.OverflowMenu
import com.lordyhas.muzik.ui.components.PlaylistCollage
import com.lordyhas.muzik.ui.components.SongItem
import com.lordyhas.muzik.ui.components.pluralize
import com.lordyhas.muzik.ui.theme.AccentPrimary
import com.lordyhas.muzik.ui.theme.TextPrimary
import com.lordyhas.muzik.ui.viewmodel.MusicViewModel

@UnstableApi
@Composable
fun PlaylistDetailScreen(
    playlistId: String,
    viewModel: MusicViewModel,
    onBackClick: () -> Unit,
    onGoToArtist: (String) -> Unit,
    onGoToAlbum: (Long) -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = true) {
        onBackClick()
    }

    val playlists by viewModel.playlists.collectAsState()
    val playlist: Playlist? = playlists.find { it.id == playlistId }

    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val likedIds by viewModel.likedSongIds.collectAsState()
    val pins by viewModel.pins.collectAsState()

    if (playlist == null) {
        DetailNotFound(message = "Playlist not found", onBack = onBackClick, modifier = modifier)
        return
    }

    val songs = playlist.songs
    val firstArtworkUri = songs.firstOrNull { it.albumArtUri != null }?.albumArtUri
    val hasSongs = songs.isNotEmpty()

    val pin = Pin.playlist(playlist.id)
    val isPinned = pin in pins

    DetailScaffold(
        title = playlist.name,
        onBack = onBackClick,
        modifier = modifier,
        topBarArtworkUri = firstArtworkUri,
        actions = {
            OverflowMenu(
                contentDescription = "Playlist options",
                actions = listOfNotNull(
                    if (hasSongs) MenuAction("Add to queue", Lucide.ListMusic, AccentPrimary) { viewModel.addAllToQueue(songs) } else null,
                    MenuAction(if (isPinned) "Unpin from Home" else "Pin to Home", if (isPinned) Lucide.PinOff else Lucide.Pin) {
                        viewModel.togglePin(pin)
                    },
                    MenuAction("Edit playlist", Lucide.Pencil, onClick = onEditClick)
                )
            )
        }
    ) {
        item(key = "hero") {
            DetailHero(
                artworkUri = firstArtworkUri,
                eyebrow = "Playlist",
                title = playlist.name,
                meta = if (hasSongs) {
                    "${pluralize(songs.size, "song")} • ${formatTotalDuration(songs.sumOf { it.duration })}"
                } else "No songs yet",
                placeholderIcon = Lucide.ListVideo,
                cover = { coverModifier -> PlaylistCollage(playlist = playlist, modifier = coverModifier) },
                onPlay = if (hasSongs) ({ viewModel.playPlaylist(playlist) }) else null,
                onShuffle = if (hasSongs) ({ viewModel.playPlaylist(playlist, shuffle = true) }) else null,
                extra = {
                    OutlinedButton(
                        onClick = onEditClick,
                        shape = CircleShape,
                        border = BorderStroke(1.dp, TextPrimary.copy(alpha = 0.25f)),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Lucide.Pencil, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit playlist", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                    }
                }
            )
        }

        if (!hasSongs) {
            item(key = "empty") {
                EmptyState(
                    icon = Lucide.LibraryBig,
                    title = "This playlist is empty",
                    message = "Add songs from the ⋮ menu on any track, or swipe right to queue them.",
                    modifier = Modifier.fillMaxWidth().height(260.dp)
                )
            }
        } else {
            items(songs, key = { song -> song.id }) { song ->
                val isSelected = currentSong?.id == song.id
                SongItem(
                    song = song,
                    isSelected = isSelected,
                    isPlaying = isPlaying && isSelected,
                    onClick = { viewModel.playPlaylist(playlist, song) },
                    onAddToQueue = { viewModel.addToQueue(song) },
                    isLiked = song.id in likedIds,
                    onToggleLike = { viewModel.toggleLike(song) },
                    onRemoveFromPlaylist = { viewModel.removeSongFromPlaylist(playlist.id, song.id) },
                    onGoToArtist = { onGoToArtist(song.artist) },
                    onGoToAlbum = { onGoToAlbum(song.albumId) },
                    onShowDetails = { viewModel.selectSongForDetails(song) },
                    // Removing a song (menu, or Undo bringing it back) slides the list instead of jumping
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .animateItem()
                )
            }
        }
    }
}
