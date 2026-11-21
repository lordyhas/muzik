package com.lordyhas.muzik.tageditor.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.BadgeCheck
import com.composables.icons.lucide.Disc3
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Music
import com.composables.icons.lucide.SearchX
import com.composables.icons.lucide.UserRound
import com.lordyhas.muzik.data.model.Album
import com.lordyhas.muzik.data.model.Song
import com.lordyhas.muzik.tageditor.TagArtist
import com.lordyhas.muzik.tageditor.TagEditorViewModel
import com.lordyhas.muzik.tageditor.needsTags
import com.lordyhas.muzik.ui.components.Artwork
import com.lordyhas.muzik.ui.components.CircleIconButton
import com.lordyhas.muzik.ui.components.EmptyState
import com.lordyhas.muzik.ui.components.LoadingState
import com.lordyhas.muzik.ui.components.MuzikSearchField
import com.lordyhas.muzik.ui.components.ScreenHeader
import com.lordyhas.muzik.ui.components.chipColors
import com.lordyhas.muzik.ui.components.pluralize
import com.lordyhas.muzik.ui.theme.AccentPrimary
import com.lordyhas.muzik.ui.theme.AccentSecondary
import com.lordyhas.muzik.ui.theme.BackgroundDark
import com.lordyhas.muzik.ui.theme.SurfaceCard
import com.lordyhas.muzik.ui.theme.TextPrimary
import com.lordyhas.muzik.ui.theme.TextSecondary

enum class TagTab(val label: String) { ALBUMS("Albums"), ARTISTS("Artists"), SONGS("Songs") }

/** Entry page of the Tag editor: search the library and pick an album, artist or song to edit. */
@Composable
fun TagLibraryScreen(
    viewModel: TagEditorViewModel,
    showConfirmationTip: Boolean,
    onAllowWithoutConfirmation: () -> Unit,
    onDismissTip: () -> Unit,
    onOpenAlbum: (Album) -> Unit,
    onOpenArtist: (TagArtist) -> Unit,
    onOpenSong: (Song) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val library by viewModel.library.collectAsState()
    val songs by viewModel.songs.collectAsState()
    val missingCount by viewModel.missingCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val query by viewModel.query.collectAsState()
    val onlyMissing by viewModel.onlyMissing.collectAsState()
    var tab by rememberSaveable { mutableStateOf(TagTab.ALBUMS) }

    Column(modifier = modifier.fillMaxSize().background(BackgroundDark).statusBarsPadding()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(modifier = Modifier.padding(top = 8.dp)) {
                CircleIconButton(icon = Lucide.ArrowLeft, contentDescription = "Close", onClick = onClose)
            }
            ScreenHeader(
                title = "Tag editor",
                subtitle = if (songs.isEmpty()) null else buildString {
                    append(pluralize(songs.size, "song"))
                    if (missingCount > 0) append(" · $missingCount missing info")
                }
            )
            if (showConfirmationTip) {
                ConfirmationTip(onAllow = onAllowWithoutConfirmation, onDismiss = onDismissTip)
                Spacer(modifier = Modifier.size(12.dp))
            }
            MuzikSearchField(value = query, onValueChange = viewModel::setQuery, placeholder = "Search songs, albums, artists")
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TagTab.entries.forEach { option ->
                    FilterChip(
                        selected = tab == option,
                        onClick = { tab = option },
                        label = { Text(option.label) },
                        shape = CircleShape,
                        colors = chipColors(),
                        border = null
                    )
                }
                FilterChip(
                    selected = onlyMissing,
                    onClick = { viewModel.setOnlyMissing(!onlyMissing) },
                    label = { Text("Missing info") },
                    shape = CircleShape,
                    colors = chipColors(),
                    border = null
                )
            }
        }

        val isEmpty = when (tab) {
            TagTab.ALBUMS -> library.albums.isEmpty()
            TagTab.ARTISTS -> library.artists.isEmpty()
            TagTab.SONGS -> library.songs.isEmpty()
        }
        when {
            isLoading -> LoadingState()
            isEmpty && onlyMissing && query.isBlank() -> EmptyState(
                icon = Lucide.BadgeCheck,
                title = "Everything is tagged",
                message = "No song is missing its title, artist or album."
            )
            isEmpty -> EmptyState(icon = Lucide.SearchX, title = "Nothing found")
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 24.dp)
            ) {
                when (tab) {
                    TagTab.ALBUMS -> items(library.albums, key = { it.id }) { album ->
                        LibraryRow(
                            artworkUri = album.albumArtUri,
                            placeholder = Lucide.Disc3,
                            title = album.title,
                            subtitle = "${album.artist} · ${pluralize(album.songCount, "song")}",
                            missing = album.songs.any { it.needsTags() },
                            onClick = { onOpenAlbum(album) }
                        )
                    }
                    TagTab.ARTISTS -> items(library.artists, key = { it.name }) { artist ->
                        LibraryRow(
                            artworkUri = artist.songs.firstOrNull { it.albumArtUri != null }?.albumArtUri,
                            placeholder = Lucide.UserRound,
                            title = artist.name,
                            subtitle = "${pluralize(artist.albumCount, "album")} · ${pluralize(artist.songs.size, "song")}",
                            missing = artist.songs.any { it.needsTags() },
                            circular = true,
                            onClick = { onOpenArtist(artist) }
                        )
                    }
                    TagTab.SONGS -> items(library.songs, key = { it.id }) { song ->
                        LibraryRow(
                            artworkUri = song.albumArtUri,
                            placeholder = Lucide.Music,
                            title = song.title,
                            subtitle = "${song.artist} · ${song.album}",
                            missing = song.needsTags(),
                            onClick = { onOpenSong(song) }
                        )
                    }
                }
                item { Spacer(modifier = Modifier.navigationBarsPadding()) }
            }
        }
    }
}

@Composable
private fun LibraryRow(
    artworkUri: Uri?,
    placeholder: ImageVector,
    title: String,
    subtitle: String,
    missing: Boolean,
    onClick: () -> Unit,
    circular: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Artwork(
            uri = artworkUri,
            shape = if (circular) CircleShape else RoundedCornerShape(10.dp),
            placeholderIcon = placeholder,
            placeholderTint = AccentSecondary,
            modifier = Modifier.size(52.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (missing) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Missing info",
                style = MaterialTheme.typography.labelSmall,
                color = AccentPrimary,
                modifier = Modifier
                    .background(AccentPrimary.copy(alpha = 0.12f), CircleShape)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}

/** Offers Android 12+'s "media management" access, so saves stop asking for confirmation. */
@Composable
private fun ConfirmationTip(onAllow: () -> Unit, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceCard)
            .padding(start = 14.dp, end = 6.dp, top = 12.dp, bottom = 4.dp)
    ) {
        Column {
            Text("Skip the confirmation on every save", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
            Text(
                "Android asks before Muzik changes a file. Allow media management to save straight away.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(top = 2.dp, end = 8.dp)
            )
            Row(modifier = Modifier.align(Alignment.End)) {
                TextButton(onClick = onDismiss) { Text("Not now", color = TextSecondary) }
                TextButton(onClick = onAllow) { Text("Allow", color = AccentPrimary) }
            }
        }
    }
}
