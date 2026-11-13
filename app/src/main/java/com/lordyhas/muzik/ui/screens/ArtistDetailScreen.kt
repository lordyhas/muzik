package com.lordyhas.muzik.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.lordyhas.muzik.data.model.Artist
import com.lordyhas.muzik.ui.components.AlbumCard
import com.lordyhas.muzik.ui.components.DetailHero
import com.lordyhas.muzik.ui.components.DetailNotFound
import com.lordyhas.muzik.ui.components.DetailScaffold
import com.lordyhas.muzik.ui.components.SectionHeader
import com.lordyhas.muzik.ui.components.SongItem
import com.lordyhas.muzik.ui.components.pluralize
import com.lordyhas.muzik.ui.viewmodel.MusicViewModel

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(UnstableApi::class)
@Composable
fun ArtistDetailScreen(
    artistName: String,
    viewModel: MusicViewModel,
    onBackClick: () -> Unit,
    onGoToAlbum: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = true) {
        onBackClick()
    }

    val artists by viewModel.artists.collectAsState()
    val allAlbums by viewModel.albums.collectAsState()
    val artist: Artist? = artists.find { it.name.equals(artistName, ignoreCase = true) }
        ?: artists.find { it.name.contains(artistName, ignoreCase = true) }

    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val likedIds by viewModel.likedSongIds.collectAsState()

    if (artist == null) {
        DetailNotFound(message = "Artist not found", onBack = onBackClick, modifier = modifier)
        return
    }

    val songs = artist.songs
    val artistAlbums = remember(songs, allAlbums) {
        val albumIds = songs.map { it.albumId }.toSet()
        allAlbums.filter { it.id in albumIds }
    }

    DetailScaffold(title = artist.name, onBack = onBackClick, modifier = modifier) {
        item(key = "hero") {
            DetailHero(
                artworkUri = artist.artworkUri,
                eyebrow = "Artist",
                title = artist.name,
                meta = "${pluralize(artist.songCount, "song")} • ${pluralize(artist.albumCount, "album")}",
                circular = true,
                placeholderIcon = Icons.Default.Person,
                onPlay = { viewModel.playArtist(artist) },
                onAddToQueue = { viewModel.addAllToQueue(songs) },
                onShuffle = {
                    val shuffled = songs.shuffled()
                    if (shuffled.isNotEmpty()) viewModel.playSong(shuffled.first(), shuffled)
                }
            )
        }

        if (artistAlbums.isNotEmpty()) {
            item(key = "albums_header") { SectionHeader(title = "Albums") }
            item(key = "albums_row") {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(artistAlbums, key = { it.title + it.id }) { album ->
                        AlbumCard(
                            album = album,
                            isCurrent = currentSong?.albumId == album.id,
                            isPlaying = isPlaying,
                            onClick = { onGoToAlbum(album.id) },
                            modifier = Modifier.width(150.dp)
                        )
                    }
                }
            }
        }

        item(key = "songs_header") {
            SectionHeader(title = "Songs", modifier = Modifier.padding(top = 8.dp))
        }

        items(songs, key = { it.contentUri.toString() }) { song ->
            val isSelected = currentSong?.id == song.id
            SongItem(
                song = song,
                isSelected = isSelected,
                isPlaying = isPlaying && isSelected,
                onClick = { viewModel.playArtist(artist, song) },
                onAddToQueue = { viewModel.addToQueue(song) },
                isLiked = song.id in likedIds,
                onToggleLike = { viewModel.toggleLike(song) },
                onAddToPlaylist = { viewModel.setSongPendingAddToPlaylist(song) },
                onGoToAlbum = { onGoToAlbum(song.albumId) },
                onShowDetails = { viewModel.selectSongForDetails(song) },
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}
