package com.lordyhas.muzik.ui.screens

import android.os.Build
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import com.composables.icons.lucide.Disc3
import com.composables.icons.lucide.EyeOff
import com.composables.icons.lucide.Folder
import com.composables.icons.lucide.LibraryBig
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Play
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.Settings
import com.composables.icons.lucide.Shuffle
import com.lordyhas.muzik.data.model.ListeningStats
import com.lordyhas.muzik.data.model.PinnedItem
import com.lordyhas.muzik.data.model.SmartPlaylistType
import com.lordyhas.muzik.data.model.Song
import com.lordyhas.muzik.data.model.formatListenedTime
import com.lordyhas.muzik.ui.components.AlbumCard
import com.lordyhas.muzik.ui.components.ArtistCard
import com.lordyhas.muzik.ui.components.Artwork
import com.lordyhas.muzik.ui.components.CircleIconButton
import com.lordyhas.muzik.ui.components.EmptyState
import com.lordyhas.muzik.ui.components.HorizontalScrollIndicator
import com.lordyhas.muzik.ui.components.LoadingState
import com.lordyhas.muzik.ui.components.PauseFilled
import com.lordyhas.muzik.ui.components.PlayFilled
import com.lordyhas.muzik.ui.components.PlaylistCollage
import com.lordyhas.muzik.ui.components.SectionHeader
import com.lordyhas.muzik.ui.components.gradient
import com.lordyhas.muzik.ui.components.icon
import com.lordyhas.muzik.ui.components.pluralize
import com.lordyhas.muzik.ui.theme.AccentPrimary
import com.lordyhas.muzik.ui.theme.AccentSecondary
import com.lordyhas.muzik.ui.theme.BackgroundDark
import com.lordyhas.muzik.ui.theme.BrandGradient
import com.lordyhas.muzik.ui.theme.StrokeSubtle
import com.lordyhas.muzik.ui.theme.SurfaceCard
import com.lordyhas.muzik.ui.theme.TextPrimary
import com.lordyhas.muzik.ui.theme.TextSecondary
import com.lordyhas.muzik.ui.theme.contentColorOn
import com.lordyhas.muzik.ui.theme.rememberArtworkColor
import com.lordyhas.muzik.ui.viewmodel.MusicViewModel
import java.util.Calendar
import kotlinx.coroutines.flow.StateFlow

@UnstableApi
@Composable
fun HomeScreen(
    viewModel: MusicViewModel,
    onAlbumClick: (Long) -> Unit,
    onArtistClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onSearchClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onFoldersClick: () -> Unit = {},
    onSmartPlaylistClick: (SmartPlaylistType) -> Unit = {},
    onPlaylistClick: (String) -> Unit = {}
) {
    val songs by viewModel.songs.collectAsState()
    val pinnedItems by viewModel.pinnedItems.collectAsState()
    val listeningStats by viewModel.listeningStats.collectAsState()
    val recentSongs by viewModel.recentlyPlayedSongs.collectAsState()
    val recentAlbums by viewModel.recentlyPlayedAlbums.collectAsState()
    val recentArtists by viewModel.recentlyPlayedArtists.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val hiddenSongCount by viewModel.hiddenSongCount.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    val greeting = greetingForNow()
    // The hero continues what the player has loaded; otherwise it offers the last song played.
    val loadedSong = currentSong
    val heroSong = loadedSong ?: recentSongs.firstOrNull() ?: songs.firstOrNull()
    val heroLabel = when {
        loadedSong != null && isPlaying -> "NOW PLAYING"
        loadedSong != null -> "CONTINUE LISTENING"
        recentSongs.isNotEmpty() -> "JUMP BACK IN"
        else -> "START LISTENING"
    }
    // Recently played without the hero's song, so it isn't shown twice
    val recentRow = recentSongs.filter { it.id != heroSong?.id }.take(12)

    Box(modifier = modifier.fillMaxSize().background(BackgroundDark)) {
        // Soft brand glow behind the header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(AccentSecondary.copy(alpha = 0.22f), AccentPrimary.copy(alpha = 0.06f), Color.Transparent)
                    )
                )
        )

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refreshLibrary() },
            modifier = Modifier.fillMaxSize().statusBarsPadding()
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 16.dp, top = 16.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.displaySmall,
                        color = TextPrimary
                    )
                    Text(
                        text = pluralize(songs.size, "song") + " in your library",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircleIconButton(
                        icon = Lucide.Search,
                        contentDescription = "Search library",
                        onClick = onSearchClick
                    )
                    CircleIconButton(
                        icon = Lucide.Settings,
                        contentDescription = "Settings",
                        onClick = onSettingsClick
                    )
                }
            }

            when {
                isLoading -> LoadingState(modifier = Modifier.height(240.dp))
                // Everything filtered out: say so, and offer the way back
                songs.isEmpty() && hiddenSongCount > 0 -> EmptyState(
                    icon = Lucide.EyeOff,
                    title = "All your music is hidden",
                    message = "${pluralize(hiddenSongCount, "audio file")} hidden by your folder and library filters.",
                    modifier = Modifier.height(360.dp),
                    action = {
                        Button(
                            onClick = onFoldersClick,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary)
                        ) { Text("Manage folders") }
                    }
                )
                songs.isEmpty() -> EmptyState(
                    icon = Lucide.LibraryBig,
                    title = "No music found on this device",
                    message = "Add some audio files, then pull down to refresh.",
                    modifier = Modifier.height(320.dp)
                )
                else -> {
                    if (heroSong != null) {
                        val isLoaded = loadedSong != null
                        HeroCard(
                            song = heroSong,
                            label = heroLabel,
                            isPlaying = isLoaded && isPlaying,
                            progress = if (isLoaded) viewModel.currentPosition to viewModel.duration else null,
                            onOpen = { if (isLoaded) viewModel.setShowNowPlaying(true) else viewModel.playSong(heroSong) },
                            onPlayPause = { if (isLoaded) viewModel.togglePlayPause() else viewModel.playSong(heroSong) },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Library shortcuts: a 2x3 grid, everything visible at once
                    ShortcutGrid(
                        tiles = listOf(
                            ShortcutTile("Shuffle all", Lucide.Shuffle, Brush.linearGradient(BrandGradient), contentColorOn(BrandGradient)) {
                                viewModel.playRandom()
                            },
                            smartTile(SmartPlaylistType.LIKED, onSmartPlaylistClick),
                            smartTile(SmartPlaylistType.MOST_PLAYED, onSmartPlaylistClick),
                            smartTile(SmartPlaylistType.RECENTLY_ADDED, onSmartPlaylistClick),
                            smartTile(SmartPlaylistType.REDISCOVER, onSmartPlaylistClick),
                            ShortcutTile("Folders", Lucide.Folder, SolidColor(StrokeSubtle), TextPrimary, onFoldersClick)
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Pinned albums and playlists; the row disappears when nothing is pinned
                    if (pinnedItems.isNotEmpty()) {
                        SectionHeader(title = "Pinned")
                        val pinnedState = rememberLazyListState()
                        LazyRow(
                            state = pinnedState,
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            items(pinnedItems, key = { it.pin.encode() }) { item ->
                                when (item) {
                                    is PinnedItem.PinnedAlbum -> HomeCollectionCard(
                                        title = item.album.title,
                                        subtitle = item.album.artist,
                                        onClick = { onAlbumClick(item.album.id) }
                                    ) { coverModifier ->
                                        Artwork(
                                            uri = item.album.albumArtUri,
                                            shape = RoundedCornerShape(14.dp),
                                            placeholderIcon = Lucide.Disc3,
                                            iconSize = 36.dp,
                                            contentDescription = null,
                                            modifier = coverModifier
                                        )
                                    }
                                    is PinnedItem.PinnedPlaylist -> HomeCollectionCard(
                                        title = item.playlist.name,
                                        subtitle = "Playlist · " + pluralize(item.playlist.songs.size, "song"),
                                        onClick = { onPlaylistClick(item.playlist.id) }
                                    ) { coverModifier -> PlaylistCollage(playlist = item.playlist, modifier = coverModifier) }
                                }
                            }
                        }
                        HorizontalScrollIndicator(pinnedState)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (recentRow.isNotEmpty()) {
                        SectionHeader(title = "Recently played")
                        // Compact song rows (3 per column) so they read as tracks to play,
                        // unlike the big square covers of the album row below.
                        val recentState = rememberLazyGridState()
                        LazyHorizontalGrid(
                            state = recentState,
                            rows = GridCells.Fixed(RECENT_ROWS),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .height(RecentRowHeight * RECENT_ROWS + 8.dp * (RECENT_ROWS - 1))
                        ) {
                            gridItems(recentRow, key = { it.contentUri.toString() }) { song ->
                                RecentSongRow(song = song, onClick = { viewModel.playSong(song) })
                            }
                        }
                        HorizontalScrollIndicator(recentState, rows = RECENT_ROWS)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (recentAlbums.isNotEmpty()) {
                        SectionHeader(title = "Recent albums")
                        val albumsState = rememberLazyListState()
                        LazyRow(
                            state = albumsState,
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            items(recentAlbums, key = { it.title + it.id }) { album ->
                                AlbumCard(
                                    album = album,
                                    isCurrent = currentSong?.albumId == album.id,
                                    isPlaying = isPlaying,
                                    onClick = { onAlbumClick(album.id) },
                                    modifier = Modifier.width(150.dp)
                                )
                            }
                        }
                        HorizontalScrollIndicator(albumsState)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (recentArtists.isNotEmpty()) {
                        SectionHeader(title = "Recent artists")
                        val artistsState = rememberLazyListState()
                        LazyRow(
                            state = artistsState,
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            items(recentArtists, key = { it.name }) { artist ->
                                ArtistCard(
                                    artist = artist,
                                    isCurrent = currentSong?.artist == artist.name,
                                    isPlaying = isPlaying,
                                    onClick = { onArtistClick(artist.name) }
                                )
                            }
                        }
                        HorizontalScrollIndicator(artistsState)
                    }

                    // Listening stats; hidden until something has been played
                    listeningStats?.let { stats ->
                        Spacer(modifier = Modifier.height(24.dp))
                        ListeningStatsCard(stats = stats, modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun HeroCard(
    song: Song,
    label: String,
    isPlaying: Boolean,
    progress: Pair<StateFlow<Long>, StateFlow<Long>>?,
    onOpen: () -> Unit,
    onPlayPause: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = rememberArtworkColor(song.albumArtUri)
    val shape = RoundedCornerShape(24.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .shadow(24.dp, shape, spotColor = accent.copy(alpha = 0.6f))
            .clip(shape)
            .background(SurfaceCard)
            .border(1.dp, StrokeSubtle, shape)
            .clickable(onClick = onOpen)
    ) {
        // Artwork backdrop
        if (song.albumArtUri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AsyncImage(
                model = song.albumArtUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .blur(40.dp)
                    .alpha(0.55f)
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(accent.copy(alpha = 0.55f), accent.copy(alpha = 0.15f), Color.Black.copy(alpha = 0.25f))
                    )
                )
        )

        Row(
            modifier = Modifier
                .matchParentSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Artwork(
                uri = song.albumArtUri,
                shape = RoundedCornerShape(16.dp),
                iconSize = 40.dp,
                placeholderTint = accent,
                contentDescription = song.title,
                modifier = Modifier
                    .size(118.dp)
                    .shadow(16.dp, RoundedCornerShape(16.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPrimary.copy(alpha = 0.75f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary.copy(alpha = 0.75f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (progress != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    HeroProgress(position = progress.first, duration = progress.second)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(50.dp)
                    .shadow(12.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable(onClick = onPlayPause),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Lucide.PauseFilled else Lucide.PlayFilled,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = BackgroundDark,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

/** Thin progress line. It collects the position itself so only it recomposes on each tick. */
@Composable
private fun HeroProgress(position: StateFlow<Long>, duration: StateFlow<Long>) {
    val positionMs by position.collectAsState()
    val durationMs by duration.collectAsState()
    val fraction = if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(3.dp)
            .clip(CircleShape)
            .background(TextPrimary.copy(alpha = 0.2f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .height(3.dp)
                .clip(CircleShape)
                .background(TextPrimary)
        )
    }
}

@Composable
private fun ListeningStatsCard(stats: ListeningStats, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(SurfaceCard)
            .border(1.dp, StrokeSubtle, shape)
            .padding(20.dp)
    ) {
        Text(
            text = "YOUR LIBRARY",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing * 3),
            color = AccentPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Listening stats", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
        Spacer(modifier = Modifier.height(18.dp))
        Row {
            StatCell(value = stats.plays.toString(), label = "Plays", modifier = Modifier.weight(1f))
            StatCell(value = formatListenedTime(stats.listenedMs), label = "Listened", modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            StatCell(
                value = stats.explored.toString(),
                label = "Explored · of ${stats.librarySize}",
                modifier = Modifier.weight(1f)
            )
            StatCell(
                value = stats.topSong?.title ?: "—",
                label = "Top track · " + pluralize(stats.topSongPlays, "play"),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCell(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(end = 12.dp)) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing * 2),
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private const val RECENT_ROWS = 3
private val RecentRowHeight = 60.dp

/** One recently played song: small cover, title and artist, and a play hint. */
@Composable
private fun RecentSongRow(
    song: Song,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // A quiet card (same stroke as the shortcut tiles) ties the play hint to its song.
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = modifier
            .width(300.dp)
            .height(RecentRowHeight)
            .clip(shape)
            .background(SurfaceCard.copy(alpha = 0.5f))
            .border(1.dp, StrokeSubtle, shape)
            .clickable(onClick = onClick)
            .padding(start = 6.dp, end = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Artwork(
            uri = song.albumArtUri,
            shape = RoundedCornerShape(10.dp),
            iconSize = 20.dp,
            contentDescription = null,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Lucide.Play,
            contentDescription = "Play ${song.title}",
            tint = TextSecondary,
            modifier = Modifier.size(18.dp)
        )
    }
}

private class ShortcutTile(
    val label: String,
    val icon: ImageVector,
    val iconBackground: Brush,
    val iconTint: Color,
    val onClick: () -> Unit
)

private fun smartTile(type: SmartPlaylistType, onClick: (SmartPlaylistType) -> Unit) =
    ShortcutTile(type.title, type.icon, Brush.linearGradient(type.gradient), contentColorOn(type.gradient)) { onClick(type) }

/** Library shortcuts in two columns, so every one is visible without scrolling sideways. */
@Composable
private fun ShortcutGrid(tiles: List<ShortcutTile>, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        tiles.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { tile -> ShortcutTileView(tile, Modifier.weight(1f)) }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ShortcutTileView(tile: ShortcutTile, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = modifier
            .height(52.dp)
            .clip(shape)
            .background(SurfaceCard)
            .border(1.dp, StrokeSubtle, shape)
            .clickable(onClick = tile.onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(tile.iconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(tile.icon, contentDescription = null, tint = tile.iconTint, modifier = Modifier.size(22.dp))
        }
        Text(
            text = tile.label,
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}

/** Square cover with a title and subtitle: playlists and pinned albums/playlists on Home. */
@Composable
private fun HomeCollectionCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cover: @Composable (Modifier) -> Unit
) {
    Column(
        modifier = modifier
            .width(128.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        cover(
            Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, StrokeSubtle, RoundedCornerShape(14.dp))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun greetingForNow(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "Good morning"
        in 12..17 -> "Good afternoon"
        else -> "Good evening"
    }
}
