package com.lordyhas.muzik.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.composables.icons.lucide.EllipsisVertical
import com.composables.icons.lucide.ListVideo
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Trash2
import com.lordyhas.muzik.data.model.Playlist
import com.lordyhas.muzik.ui.theme.AccentPrimary
import com.lordyhas.muzik.ui.theme.AccentSecondary
import com.lordyhas.muzik.ui.theme.DangerRed
import com.lordyhas.muzik.ui.theme.StrokeSubtle
import com.lordyhas.muzik.ui.theme.SurfaceCard
import com.lordyhas.muzik.ui.theme.SurfaceElevated
import com.lordyhas.muzik.ui.theme.TextMuted
import com.lordyhas.muzik.ui.theme.TextPrimary
import com.lordyhas.muzik.ui.theme.TextSecondary

@Composable
fun PlaylistCard(
    playlist: Playlist,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    isCurrent: Boolean = false,
    isPlaying: Boolean = false
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box {
            PlaylistCollage(
                playlist = playlist,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, if (isCurrent) AccentPrimary else StrokeSubtle, RoundedCornerShape(16.dp))
            )
            if (isCurrent) {
                NowPlayingBadge(
                    isPlaying = isPlaying,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isCurrent) AccentPrimary else TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = pluralize(playlist.songIds.size, "song"),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Box {
                IconButton(
                    onClick = { isMenuExpanded = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Lucide.EllipsisVertical,
                        contentDescription = "Playlist options",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = { isMenuExpanded = false },
                    modifier = Modifier.background(SurfaceElevated)
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit playlist", color = TextPrimary, style = MaterialTheme.typography.bodyMedium) },
                        leadingIcon = {
                            Icon(
                                Lucide.Pencil,
                                contentDescription = null,
                                tint = AccentPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            isMenuExpanded = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = DangerRed, style = MaterialTheme.typography.bodyMedium) },
                        leadingIcon = {
                            Icon(
                                Lucide.Trash2,
                                contentDescription = null,
                                tint = DangerRed,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            isMenuExpanded = false
                            confirmDelete = true
                        }
                    )
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            containerColor = SurfaceCard,
            shape = RoundedCornerShape(24.dp),
            title = { Text("Delete playlist?", style = MaterialTheme.typography.titleLarge, color = TextPrimary) },
            text = {
                Text(
                    "\"${playlist.name}\" will be permanently deleted. Your songs stay on the device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete()
                }) { Text("Delete", color = DangerRed) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancel", color = TextSecondary) }
            }
        )
    }
}

/** Cover made from up to four distinct album arts in the playlist. */
@Composable
fun PlaylistCollage(
    playlist: Playlist,
    modifier: Modifier = Modifier
) {
    val artworkUris = playlist.songs
        .mapNotNull { it.albumArtUri }
        .distinct()
        .take(4)

    Box(
        modifier = modifier.background(
            Brush.linearGradient(listOf(AccentPrimary.copy(alpha = 0.35f), AccentSecondary.copy(alpha = 0.35f), SurfaceElevated))
        ),
        contentAlignment = Alignment.Center
    ) {
        @Composable
        fun Cell(index: Int, cellModifier: Modifier) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(artworkUris[index]).crossfade(true).build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = cellModifier
            )
        }

        when {
            artworkUris.size >= 4 -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(modifier = Modifier.weight(1f)) {
                        Cell(0, Modifier.weight(1f).fillMaxSize())
                        Cell(1, Modifier.weight(1f).fillMaxSize())
                    }
                    Row(modifier = Modifier.weight(1f)) {
                        Cell(2, Modifier.weight(1f).fillMaxSize())
                        Cell(3, Modifier.weight(1f).fillMaxSize())
                    }
                }
            }
            artworkUris.isNotEmpty() -> Cell(0, Modifier.fillMaxSize())
            else -> {
                Icon(
                    imageVector = Lucide.ListVideo,
                    contentDescription = null,
                    tint = TextPrimary.copy(alpha = 0.8f),
                    modifier = Modifier.size(52.dp)
                )
            }
        }
    }
}
