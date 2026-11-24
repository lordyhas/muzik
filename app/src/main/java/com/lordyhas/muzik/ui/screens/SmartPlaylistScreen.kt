package com.lordyhas.muzik.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.lordyhas.muzik.data.model.SmartPlaylistType
import com.lordyhas.muzik.ui.components.DetailHero
import com.lordyhas.muzik.ui.components.DetailScaffold
import com.lordyhas.muzik.ui.components.EmptyState
import com.lordyhas.muzik.ui.components.SmartPlaylistCover
import com.lordyhas.muzik.ui.components.SongItem
import com.lordyhas.muzik.ui.components.icon
import com.lordyhas.muzik.ui.components.pluralize
import com.lordyhas.muzik.ui.viewmodel.MusicViewModel

@UnstableApi
@Composable
fun SmartPlaylistScreen(
    type: SmartPlaylistType,
    viewModel: MusicViewModel,
    onBackClick: () -> Unit,
    onGoToArtist: (String) -> Unit,
    onGoToAlbum: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = true) { onBackClick() }

    val smartPlaylists by viewModel.smartPlaylists.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val likedIds by viewModel.likedSongIds.collectAsState()
    val songs = smartPlaylists.firstOrNull { it.type == type }?.songs.orEmpty()

    DetailScaffold(title = type.title, onBack = onBackClick, modifier = modifier) {
        item(key = "hero") {
            DetailHero(
                artworkUri = null,
                eyebrow = "Smart playlist",
                title = type.title,
                subtitle = type.description,
                meta = if (songs.isEmpty()) "No songs yet" else pluralize(songs.size, "song"),
                cover = { coverModifier -> SmartPlaylistCover(type = type, modifier = coverModifier, iconSize = 80.dp) },
                onPlay = if (songs.isNotEmpty()) ({ viewModel.playSong(songs.first(), songs) }) else null,
                onShuffle = if (songs.isNotEmpty()) ({
                    val shuffled = songs.shuffled()
                    viewModel.playSong(shuffled.first(), shuffled)
                }) else null
            )
        }

        if (songs.isEmpty()) {
            item(key = "empty") {
                EmptyState(
                    icon = type.icon,
                    title = when (type) {
                        SmartPlaylistType.LIKED -> "No liked songs yet"
                        SmartPlaylistType.MOST_PLAYED -> "Nothing played yet"
                        SmartPlaylistType.RECENTLY_ADDED -> "No recent additions"
                        SmartPlaylistType.REDISCOVER -> "Nothing to rediscover yet"
                    },
                    message = when (type) {
                        SmartPlaylistType.LIKED -> "Tap the heart on the player to save songs here."
                        SmartPlaylistType.MOST_PLAYED -> "Your most played songs will show up here."
                        SmartPlaylistType.RECENTLY_ADDED -> "New music on your device shows up here."
                        SmartPlaylistType.REDISCOVER -> "Songs you loved but haven't played in a month show up here."
                    },
                    modifier = Modifier.fillMaxWidth().height(260.dp)
                )
            }
        } else {
            items(songs, key = { it.contentUri.toString() }) { song ->
                val isSelected = currentSong?.id == song.id
                SongItem(
                    song = song,
                    isSelected = isSelected,
                    isPlaying = isPlaying && isSelected,
                    onClick = { viewModel.playSong(song, songs) },
                    onAddToQueue = { viewModel.addToQueue(song) },
                    isLiked = song.id in likedIds,
                    onToggleLike = { viewModel.toggleLike(song) },
                    onAddToPlaylist = { viewModel.setSongPendingAddToPlaylist(song) },
                    onGoToArtist = { onGoToArtist(song.artist) },
                    onGoToAlbum = { onGoToAlbum(song.albumId) },
                    onShowDetails = { viewModel.selectSongForDetails(song) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    }
}
