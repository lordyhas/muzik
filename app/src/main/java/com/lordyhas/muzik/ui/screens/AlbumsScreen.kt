package com.lordyhas.muzik.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.composables.icons.lucide.Disc3
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.SearchX
import com.lordyhas.muzik.data.model.AlbumSort
import com.lordyhas.muzik.data.model.indexLetter
import com.lordyhas.muzik.ui.components.AlbumCard
import com.lordyhas.muzik.ui.components.AlphabetFastScroller
import com.lordyhas.muzik.ui.components.EmptyState
import com.lordyhas.muzik.ui.components.LoadingState
import com.lordyhas.muzik.ui.components.MuzikSearchField
import com.lordyhas.muzik.ui.components.ScreenHeader
import com.lordyhas.muzik.ui.components.SortMenuButton
import com.lordyhas.muzik.ui.components.pluralize
import com.lordyhas.muzik.ui.theme.BackgroundDark
import com.lordyhas.muzik.ui.viewmodel.MusicViewModel

@UnstableApi
@Composable
fun AlbumsScreen(
    viewModel: MusicViewModel,
    onAlbumClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val albums by viewModel.sortedAlbums.collectAsState()
    val albumSort by viewModel.albumSort.collectAsState()
    val gridState = rememberLazyGridState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    var searchQuery by rememberSaveable { mutableStateOf("") }

    BackHandler(enabled = searchQuery.isNotBlank()) {
        searchQuery = ""
    }

    val filteredAlbums = if (searchQuery.isBlank()) {
        albums
    } else {
        val query = searchQuery.trim().lowercase()
        albums.filter { it.title.lowercase().contains(query) || it.artist.lowercase().contains(query) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        ScreenHeader(title = "Albums", subtitle = pluralize(albums.size, "album")) {
            SortMenuButton(
                options = AlbumSort.entries,
                selected = albumSort,
                label = { it.label },
                onSelect = { viewModel.setAlbumSort(it) }
            )
        }

        MuzikSearchField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = "Search albums or artists"
        )

        when {
            isLoading -> LoadingState()
            filteredAlbums.isEmpty() && searchQuery.isNotBlank() -> EmptyState(
                icon = Lucide.SearchX,
                title = "No albums match \"$searchQuery\""
            )
            filteredAlbums.isEmpty() -> EmptyState(
                icon = Lucide.Disc3,
                title = "No albums yet",
                message = "Albums appear here once music is on your device."
            )
            else -> Box(modifier = Modifier.fillMaxSize()) {
            val scroller = showScroller(albumSort.alphabetical, filteredAlbums.size, searchQuery)
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                state = gridState,
                modifier = Modifier.fillMaxSize().padding(end = if (scroller) 18.dp else 0.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)
            ) {
                items(filteredAlbums, key = { it.title + it.id }) { album ->
                    AlbumCard(
                        album = album,
                        isCurrent = currentSong?.albumId == album.id,
                        isPlaying = isPlaying,
                        onClick = { onAlbumClick(album.id) }
                    )
                }
            }
            if (scroller) {
                AlphabetFastScroller(
                    letters = remember(filteredAlbums, albumSort) { filteredAlbums.map { indexLetter(albumSort.indexText(it)) } },
                    onScrollToIndex = { gridState.scrollToItem(it) },
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
            }
        }
    }
}
