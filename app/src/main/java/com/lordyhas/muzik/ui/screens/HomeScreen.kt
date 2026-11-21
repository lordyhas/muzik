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
import androidx.compose.foundation.lazy.items
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
import com.composables.icons.lucide.EyeOff
import com.composables.icons.lucide.Folder
import com.composables.icons.lucide.LibraryBig
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.Settings
import com.composables.icons.lucide.Shuffle
import com.lordyhas.muzik.data.model.Playlist
import com.lordyhas.muzik.data.model.SmartPlaylistType
import com.lordyhas.muzik.data.model.Song
import com.lordyhas.muzik.ui.components.AlbumCard
import com.lordyhas.muzik.ui.components.ArtistCard
import com.lordyhas.muzik.ui.components.Artwork
import com.lordyhas.muzik.ui.components.CircleIconButton
import com.lordyhas.muzik.ui.components.EmptyState
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
    onSeeAllAlbums: () -> Unit = {},
    onSeeAllArtists: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onFoldersClick: () -> Unit = {},
    onSmartPlaylistClick: (SmartPlaylistType) -> Unit = {},
    onPlaylistClick: (String) -> Unit = {},
    onSeeAllPlaylists: () -> Unit = {}
) {
    val songs by viewModel.songs.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
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
    val recentRow = recentSongs.filter { it.id != heroSong?.id }.take(10)

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

                    if (recentRow.isNotEmpty()) {
                        SectionHeader(title = "Recently played")
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            items(recentRow, key = { it.contentUri.toString() }) { song ->
                                RecentSongCard(song = song, onClick = { viewModel.playSong(song) })
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (playlists.isNotEmpty()) {
                        SectionHeader(title = "Your playlists", actionLabel = "See all", onAction = onSeeAllPlaylists)
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            items(playlists, key = { it.id }) { playlist ->
                                HomePlaylistCard(playlist = playlist, onClick = { onPlaylistClick(playlist.id) })
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (recentAlbums.isNotEmpty()) {
                        SectionHeader(title = "Recent albums", actionLabel = "See all", onAction = onSeeAllAlbums)
                        LazyRow(
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
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (recentArtists.isNotEmpty()) {
                        SectionHeader(title = "Recent artists", actionLabel = "See all", onAction = onSeeAllArtists)
                        LazyRow(
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
private fun RecentSongCard(
    song: Song,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(128.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Artwork(
            uri = song.albumArtUri,
            shape = RoundedCornerShape(14.dp),
            iconSize = 36.dp,
            contentDescription = song.title,
            modifier = Modifier
                .size(120.dp)
                .border(1.dp, StrokeSubtle, RoundedCornerShape(14.dp))
        )

        Spacer(modifier = Modifier.height(8.dp))

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

@Composable
private fun HomePlaylistCard(playlist: Playlist, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .width(128.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        PlaylistCollage(
            playlist = playlist,
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, StrokeSubtle, RoundedCornerShape(14.dp))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = playlist.name,
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = pluralize(playlist.songs.size, "song"),
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            maxLines = 1
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
