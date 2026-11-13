package com.lordyhas.muzik.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.lordyhas.muzik.data.model.Playlist
import com.lordyhas.muzik.data.model.Song
import com.lordyhas.muzik.ui.components.Artwork
import com.lordyhas.muzik.ui.components.DetailNotFound
import com.lordyhas.muzik.ui.components.DragHandleIcon
import com.lordyhas.muzik.ui.components.PlaylistCollage
import com.lordyhas.muzik.ui.components.ReorderableRow
import com.lordyhas.muzik.ui.components.pluralize
import com.lordyhas.muzik.ui.components.placementSpec
import com.lordyhas.muzik.ui.components.rememberReorderState
import com.lordyhas.muzik.ui.theme.AccentPrimary
import com.lordyhas.muzik.ui.theme.BackgroundDark
import com.lordyhas.muzik.ui.theme.DangerRed
import com.lordyhas.muzik.ui.theme.StrokeSubtle
import com.lordyhas.muzik.ui.theme.SurfaceCard
import com.lordyhas.muzik.ui.theme.SurfaceElevated
import com.lordyhas.muzik.ui.theme.TextMuted
import com.lordyhas.muzik.ui.theme.TextPrimary
import com.lordyhas.muzik.ui.theme.TextSecondary
import com.lordyhas.muzik.ui.viewmodel.MusicViewModel

/**
 * Spotify-style playlist editor: rename, remove songs and drag to reorder on a working copy.
 * Nothing is written until Save; leaving with changes asks before discarding them.
 */
@UnstableApi
@Composable
fun PlaylistEditScreen(
    playlistId: String,
    viewModel: MusicViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playlists by viewModel.playlists.collectAsState()
    val playlist = playlists.find { it.id == playlistId }

    if (playlist == null) {
        BackHandler(enabled = true) { onClose() }
        DetailNotFound(message = "Playlist not found", onBack = onClose, modifier = modifier)
        return
    }

    // Working copy, taken once: later Room updates don't overwrite what the user is editing
    var name by rememberSaveable(playlistId) { mutableStateOf(playlist.name) }
    val songs = remember(playlistId) { playlist.songs.toMutableStateList() }
    var confirmDiscard by remember { mutableStateOf(false) }

    val hasChanges = name.trim() != playlist.name || songs.map { it.id } != playlist.songs.map { it.id }
    val canSave = hasChanges && name.isNotBlank()

    fun close() {
        if (hasChanges) confirmDiscard = true else onClose()
    }

    BackHandler(enabled = true) { close() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { close() }) {
                Icon(Icons.Default.Close, contentDescription = "Cancel", tint = TextPrimary)
            }
            Text(
                text = "Edit playlist",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            TextButton(
                onClick = {
                    viewModel.savePlaylistEdits(playlist, name.trim(), songs.toList())
                    onClose()
                },
                enabled = canSave
            ) {
                Text(
                    "Save",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (canSave) AccentPrimary else TextMuted
                )
            }
        }

        val reorderState = rememberReorderState()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item(key = "header") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PlaylistCollage(
                        playlist = playlist.copy(songs = songs.toList()),
                        modifier = Modifier
                            .size(150.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceCard)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        singleLine = true,
                        placeholder = {
                            Text(
                                "Playlist name",
                                style = MaterialTheme.typography.headlineSmall,
                                color = TextMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        textStyle = MaterialTheme.typography.headlineSmall.copy(textAlign = TextAlign.Center),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = AccentPrimary,
                            focusedIndicatorColor = AccentPrimary,
                            unfocusedIndicatorColor = StrokeSubtle
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (songs.isEmpty()) "No songs" else pluralize(songs.size, "song"),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
                ReorderableRow(
                    state = reorderState,
                    key = song.id,
                    onMove = { steps ->
                        val target = (index + steps).coerceIn(0, songs.lastIndex)
                        if (target != index) songs.add(target, songs.removeAt(index))
                    },
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .animateItem(placementSpec = reorderState.placementSpec(song.id))
                ) { handle, dragging ->
                    EditableSongRow(
                        song = song,
                        onRemove = { songs.remove(song) },
                        handleModifier = handle,
                        isDragging = dragging
                    )
                }
            }
        }
    }

    if (confirmDiscard) {
        AlertDialog(
            onDismissRequest = { confirmDiscard = false },
            containerColor = SurfaceCard,
            shape = RoundedCornerShape(24.dp),
            title = { Text("Discard changes?", style = MaterialTheme.typography.titleLarge, color = TextPrimary) },
            text = {
                Text(
                    "Your edits to this playlist won't be saved.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmDiscard = false
                    onClose()
                }) { Text("Discard", color = DangerRed) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDiscard = false }) { Text("Keep editing", color = TextSecondary) }
            }
        )
    }
}

@Composable
private fun EditableSongRow(
    song: Song,
    onRemove: () -> Unit,
    handleModifier: Modifier,
    isDragging: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isDragging) SurfaceElevated else Color.Transparent, RoundedCornerShape(14.dp))
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onRemove) {
            Icon(
                Icons.Default.RemoveCircle,
                contentDescription = "Remove ${song.title}",
                tint = TextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
        Artwork(
            uri = song.albumArtUri,
            shape = RoundedCornerShape(8.dp),
            iconSize = 20.dp,
            modifier = Modifier.size(44.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                song.title,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                song.artist,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Box(contentAlignment = Alignment.Center) {
            DragHandleIcon(modifier = handleModifier)
        }
    }
}
