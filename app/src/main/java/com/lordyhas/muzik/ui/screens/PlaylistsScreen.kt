package com.lordyhas.muzik.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.composables.icons.lucide.ListVideo
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.lordyhas.muzik.data.model.Pin
import com.lordyhas.muzik.data.model.SmartPlaylistType
import com.lordyhas.muzik.ui.components.CreatePlaylistDialog
import com.lordyhas.muzik.ui.components.EmptyState
import com.lordyhas.muzik.ui.components.LoadingState
import com.lordyhas.muzik.ui.components.PlaylistCard
import com.lordyhas.muzik.ui.components.ScreenHeader
import com.lordyhas.muzik.ui.components.SmartPlaylistTile
import com.lordyhas.muzik.ui.components.pluralize
import com.lordyhas.muzik.ui.theme.AccentPrimary
import com.lordyhas.muzik.ui.theme.BackgroundDark
import com.lordyhas.muzik.ui.theme.TextPrimary
import com.lordyhas.muzik.ui.viewmodel.MusicViewModel

@UnstableApi
@Composable
fun PlaylistsScreen(
    viewModel: MusicViewModel,
    onSmartPlaylistClick: (SmartPlaylistType) -> Unit,
    onPlaylistClick: (String) -> Unit,
    onEditPlaylist: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val playlists by viewModel.playlists.collectAsState()
    val smartPlaylists by viewModel.smartPlaylists.collectAsState()
    val showCreateDialog by viewModel.showCreatePlaylistDialog.collectAsState()
    val songsPendingAddToPlaylist by viewModel.songsPendingAddToPlaylist.collectAsState()
    val pins by viewModel.pins.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val playingPlaylistId by viewModel.playingPlaylistId.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            ScreenHeader(title = "Playlists", subtitle = pluralize(playlists.size, "playlist"))

            if (isLoading) {
                LoadingState()
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
                ) {
                    // Smart playlists carousel
                    item(span = { GridItemSpan(maxLineSpan) }, key = "smart") {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(smartPlaylists, key = { it.type.name }) { smart ->
                                SmartPlaylistTile(
                                    playlist = smart,
                                    onClick = { onSmartPlaylistClick(smart.type) }
                                )
                            }
                        }
                    }

                    item(span = { GridItemSpan(maxLineSpan) }, key = "yours") {
                        Text(
                            text = "Your playlists",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    if (playlists.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }, key = "empty") {
                            EmptyState(
                                icon = Lucide.ListVideo,
                                title = "No playlists yet",
                                message = "Tap \"New playlist\" to create your first one.",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp)
                            )
                        }
                    } else {
                        items(playlists, key = { it.id }) { playlist ->
                            PlaylistCard(
                                playlist = playlist,
                                isCurrent = playlist.id == playingPlaylistId,
                                isPlaying = isPlaying,
                                onClick = { onPlaylistClick(playlist.id) },
                                onEdit = { onEditPlaylist(playlist.id) },
                                onDelete = { viewModel.deletePlaylist(playlist.id) },
                                isPinned = Pin.playlist(playlist.id) in pins,
                                onTogglePin = { viewModel.togglePin(Pin.playlist(playlist.id)) }
                            )
                        }
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { viewModel.setShowCreatePlaylistDialog(true) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 24.dp),
            shape = CircleShape,
            containerColor = AccentPrimary,
            contentColor = Color.White,
            icon = { Icon(Lucide.Plus, contentDescription = null) },
            text = { Text("New playlist") }
        )
    }

    // The global dialog in MainAppScreen handles creation from the add-to-playlist sheet
    if (showCreateDialog && songsPendingAddToPlaylist.isEmpty()) {
        CreatePlaylistDialog(
            title = "New playlist",
            initialName = "",
            onConfirm = { name ->
                viewModel.createPlaylist(name)
                viewModel.setShowCreatePlaylistDialog(false)
            },
            onDismiss = { viewModel.setShowCreatePlaylistDialog(false) }
        )
    }
}
