package com.lordyhas.muzik.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Folder
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.SearchX
import com.lordyhas.muzik.data.model.Playlist
import com.lordyhas.muzik.ui.components.AlbumCard
import com.lordyhas.muzik.ui.components.ArtistCard
import com.lordyhas.muzik.ui.components.CircleIconButton
import com.lordyhas.muzik.ui.components.EmptyState
import com.lordyhas.muzik.ui.components.MuzikSearchField
import com.lordyhas.muzik.ui.components.PlaylistCollage
import com.lordyhas.muzik.ui.components.SectionHeader
import com.lordyhas.muzik.ui.components.SongItem
import com.lordyhas.muzik.ui.components.pluralize
import com.lordyhas.muzik.ui.theme.AccentPrimary
import com.lordyhas.muzik.ui.theme.BackgroundDark
import com.lordyhas.muzik.ui.theme.SurfaceCard
import com.lordyhas.muzik.ui.theme.TextPrimary
import com.lordyhas.muzik.ui.theme.TextSecondary
import com.lordyhas.muzik.ui.viewmodel.MusicViewModel

/** Library-wide search across songs, artists, albums and playlists. */
@UnstableApi
@Composable
fun SearchScreen(
    viewModel: MusicViewModel,
    onBackClick: () -> Unit,
    onAlbumClick: (Long) -> Unit,
    onArtistClick: (String) -> Unit,
    onPlaylistClick: (String) -> Unit,
    onFoldersClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val query by viewModel.globalQuery.collectAsState()
    val results by viewModel.searchResults.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val likedIds by viewModel.likedSongIds.collectAsState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        if (query.isEmpty()) focusRequester.requestFocus()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 16.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircleIconButton(
                icon = Lucide.ArrowLeft,
                contentDescription = "Back",
                onClick = onBackClick
            )
            Spacer(modifier = Modifier.width(8.dp))
            MuzikSearchField(
                value = query,
                onValueChange = { viewModel.updateGlobalQuery(it) },
                placeholder = "Songs, artists, albums, playlists",
                modifier = Modifier.focusRequester(focusRequester)
            )
        }

        when {
            query.isBlank() -> Column(modifier = Modifier.padding(16.dp)) {
                Text("Browse", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                Spacer(modifier = Modifier.height(10.dp))
                BrowseTile(
                    icon = Lucide.Folder,
                    title = "Folders",
                    subtitle = "Browse music by device folder",
                    onClick = onFoldersClick
                )
                EmptyState(
                    icon = Lucide.Search,
                    title = "Search your library",
                    message = "Find songs, artists, albums and playlists in one place."
                )
            }
            results.isEmpty -> EmptyState(
                icon = Lucide.SearchX,
                title = "No results for \"$query\"",
                message = "Check the spelling or try another keyword."
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (results.artists.isNotEmpty()) {
                    item(key = "artists_header") { SectionHeader("Artists") }
                    item(key = "artists") {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(results.artists, key = { it.name }) { artist ->
                                ArtistCard(
                                    artist = artist,
                                    isCurrent = currentSong?.artist == artist.name,
                                    isPlaying = isPlaying,
                                    onClick = { onArtistClick(artist.name) }
                                )
                            }
                        }
                    }
                }

                if (results.albums.isNotEmpty()) {
                    item(key = "albums_header") { SectionHeader("Albums") }
                    item(key = "albums") {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(results.albums, key = { it.title + it.id }) { album ->
                                AlbumCard(
                                    album = album,
                                    isCurrent = currentSong?.albumId == album.id,
                                    isPlaying = isPlaying,
                                    onClick = { onAlbumClick(album.id) },
                                    modifier = Modifier.width(140.dp)
                                )
                            }
                        }
                    }
                }

                if (results.playlists.isNotEmpty()) {
                    item(key = "playlists_header") { SectionHeader("Playlists") }
                    items(results.playlists, key = { "pl_" + it.id }) { playlist ->
                        PlaylistRow(playlist = playlist, onClick = { onPlaylistClick(playlist.id) })
                    }
                }

                if (results.songs.isNotEmpty()) {
                    item(key = "songs_header") {
                        SectionHeader("Songs", modifier = Modifier.padding(top = 8.dp))
                    }
                    items(results.songs, key = { "song_" + it.contentUri }) { song ->
                        val isSelected = currentSong?.id == song.id
                        SongItem(
                            song = song,
                            isSelected = isSelected,
                            isPlaying = isPlaying && isSelected,
                            onClick = { viewModel.playSong(song, results.songs) },
                            onAddToQueue = { viewModel.addToQueue(song) },
                            isLiked = song.id in likedIds,
                            onToggleLike = { viewModel.toggleLike(song) },
                            onAddToPlaylist = { viewModel.setSongPendingAddToPlaylist(song) },
                            onGoToArtist = { onArtistClick(song.artist) },
                            onGoToAlbum = { onAlbumClick(song.albumId) },
                            onShowDetails = { viewModel.selectSongForDetails(song) },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistRow(playlist: Playlist, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlaylistCollage(
            playlist = playlist,
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(10.dp))
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(playlist.name, style = MaterialTheme.typography.titleSmall, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("Playlist • ${pluralize(playlist.songIds.size, "song")}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}

@Composable
internal fun BrowseTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceCard)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = AccentPrimary, modifier = Modifier.size(26.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}
