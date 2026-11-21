package com.lordyhas.muzik.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.SearchX
import com.composables.icons.lucide.UserRound
import com.lordyhas.muzik.data.model.Artist
import com.lordyhas.muzik.data.model.ArtistSort
import com.lordyhas.muzik.data.model.indexLetter
import com.lordyhas.muzik.ui.components.AlphabetFastScroller
import com.lordyhas.muzik.ui.components.Artwork
import com.lordyhas.muzik.ui.components.EmptyState
import com.lordyhas.muzik.ui.components.LoadingState
import com.lordyhas.muzik.ui.components.MuzikSearchField
import com.lordyhas.muzik.ui.components.NowPlayingBadge
import com.lordyhas.muzik.ui.components.ScreenHeader
import com.lordyhas.muzik.ui.components.SortMenuButton
import com.lordyhas.muzik.ui.components.pluralize
import com.lordyhas.muzik.ui.theme.AccentPrimary
import com.lordyhas.muzik.ui.theme.AccentSecondary
import com.lordyhas.muzik.ui.theme.BackgroundDark
import com.lordyhas.muzik.ui.theme.StrokeSubtle
import com.lordyhas.muzik.ui.theme.TextMuted
import com.lordyhas.muzik.ui.theme.TextPrimary
import com.lordyhas.muzik.ui.theme.TextSecondary
import com.lordyhas.muzik.ui.viewmodel.MusicViewModel

@UnstableApi
@Composable
fun ArtistsScreen(
    viewModel: MusicViewModel,
    onArtistClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val artists by viewModel.sortedArtists.collectAsState()
    val artistSort by viewModel.artistSort.collectAsState()
    val listState = rememberLazyListState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    var searchQuery by rememberSaveable { mutableStateOf("") }

    BackHandler(enabled = searchQuery.isNotBlank()) {
        searchQuery = ""
    }

    val filteredArtists = if (searchQuery.isBlank()) {
        artists
    } else {
        val query = searchQuery.trim().lowercase()
        artists.filter { it.name.lowercase().contains(query) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        ScreenHeader(title = "Artists", subtitle = pluralize(artists.size, "artist")) {
            SortMenuButton(
                options = ArtistSort.entries,
                selected = artistSort,
                label = { it.label },
                onSelect = { viewModel.setArtistSort(it) }
            )
        }

        MuzikSearchField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = "Search artists"
        )

        when {
            isLoading -> LoadingState()
            filteredArtists.isEmpty() && searchQuery.isNotBlank() -> EmptyState(
                icon = Lucide.SearchX,
                title = "No artists match \"$searchQuery\""
            )
            filteredArtists.isEmpty() -> EmptyState(
                icon = Lucide.UserRound,
                title = "No artists yet",
                message = "Artists appear here once music is on your device."
            )
            else -> Box(modifier = Modifier.fillMaxSize()) {
            val scroller = showScroller(artistSort.alphabetical, filteredArtists.size, searchQuery)
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(end = if (scroller) 18.dp else 0.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)
            ) {
                items(filteredArtists, key = { it.name }) { artist ->
                    ArtistListItem(
                        artist = artist,
                        isCurrent = currentSong?.artist == artist.name,
                        isPlaying = isPlaying,
                        onClick = { onArtistClick(artist.name) }
                    )
                }
            }
            if (scroller) {
                AlphabetFastScroller(
                    letters = remember(filteredArtists) { filteredArtists.map { indexLetter(it.name) } },
                    onScrollToIndex = { listState.scrollToItem(it) },
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
            }
        }
    }
}

@Composable
private fun ArtistListItem(
    artist: Artist,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Artwork(
                uri = artist.artworkUri,
                shape = CircleShape,
                placeholderIcon = Lucide.UserRound,
                iconSize = 28.dp,
                placeholderTint = AccentSecondary,
                contentDescription = artist.name,
                modifier = Modifier
                    .size(56.dp)
                    .border(1.dp, if (isCurrent) AccentPrimary else StrokeSubtle, CircleShape)
            )
            if (isCurrent) {
                NowPlayingBadge(
                    isPlaying = isPlaying,
                    size = 22.dp,
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = artist.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                color = if (isCurrent) AccentPrimary else TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${pluralize(artist.songCount, "song")} • ${pluralize(artist.albumCount, "album")}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            imageVector = Lucide.ChevronRight,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(22.dp)
        )
    }
}
