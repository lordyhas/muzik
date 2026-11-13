package com.lordyhas.muzik.ui.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import com.lordyhas.muzik.data.model.Song
import com.lordyhas.muzik.data.model.SmartPlaylistType
import com.lordyhas.muzik.ui.components.AlbumCard
import com.lordyhas.muzik.ui.components.ArtistCard
import com.lordyhas.muzik.ui.components.Artwork
import com.lordyhas.muzik.ui.components.CircleIconButton
import com.lordyhas.muzik.ui.components.EmptyState
import com.lordyhas.muzik.ui.components.LoadingState
import com.lordyhas.muzik.ui.components.SectionHeader
import com.lordyhas.muzik.ui.components.pluralize
import com.lordyhas.muzik.ui.theme.AccentCyan
import com.lordyhas.muzik.ui.theme.AccentPrimary
import com.lordyhas.muzik.ui.theme.AccentSecondary
import com.lordyhas.muzik.ui.theme.BackgroundDark
import com.lordyhas.muzik.ui.theme.StrokeSubtle
import com.lordyhas.muzik.ui.theme.SurfaceCard
import com.lordyhas.muzik.ui.theme.TextPrimary
import com.lordyhas.muzik.ui.theme.TextSecondary
import com.lordyhas.muzik.ui.theme.rememberArtworkColor
import com.lordyhas.muzik.ui.viewmodel.MusicViewModel
import java.util.Calendar

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
    onSmartPlaylistClick: (SmartPlaylistType) -> Unit = {}
) {
    val songs by viewModel.songs.collectAsState()
    val recentSongs by viewModel.recentlyPlayedSongs.collectAsState()
    val recentAlbums by viewModel.recentlyPlayedAlbums.collectAsState()
    val recentArtists by viewModel.recentlyPlayedArtists.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val hiddenSongCount by viewModel.hiddenSongCount.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    val greeting = greetingForNow()
    val heroSong = recentSongs.firstOrNull() ?: songs.firstOrNull()

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
                        icon = Icons.Default.Search,
                        contentDescription = "Search library",
                        onClick = onSearchClick
                    )
                    CircleIconButton(
                        icon = Icons.Default.Settings,
                        contentDescription = "Settings",
                        onClick = onSettingsClick
                    )
                }
            }

            when {
                isLoading -> LoadingState(modifier = Modifier.height(240.dp))
                // Everything filtered out: say so, and offer the way back
                songs.isEmpty() && hiddenSongCount > 0 -> EmptyState(
                    icon = Icons.Default.VisibilityOff,
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
                    icon = Icons.Default.LibraryMusic,
                    title = "No music found on this device",
                    message = "Add some audio files, then pull down to refresh.",
                    modifier = Modifier.height(320.dp)
                )
                else -> {
                    if (heroSong != null) {
                        HeroCard(
                            song = heroSong,
                            isRecent = recentSongs.isNotEmpty(),
                            onPlay = { viewModel.playSong(heroSong) },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick mixes
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionCard(
                            title = "Shuffle all",
                            subtitle = "Your whole library",
                            icon = Icons.Default.Shuffle,
                            gradient = listOf(AccentPrimary, Color(0xFFE11D48)),
                            onClick = { viewModel.playRandom() },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionCard(
                            title = "Discover",
                            subtitle = "10 random picks",
                            icon = Icons.Default.AutoAwesome,
                            gradient = listOf(AccentSecondary, AccentCyan),
                            onClick = {
                                val sample = songs.shuffled().take(10)
                                if (sample.isNotEmpty()) viewModel.playSong(sample.first(), sample)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Library shortcuts
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ShortcutChip(
                            icon = Icons.Default.Favorite,
                            label = "Liked Songs",
                            onClick = { onSmartPlaylistClick(SmartPlaylistType.LIKED) }
                        )
                        ShortcutChip(
                            icon = Icons.Default.Whatshot,
                            label = "Most Played",
                            onClick = { onSmartPlaylistClick(SmartPlaylistType.MOST_PLAYED) },
                            tint = Color(0xFFFF8A3D)
                        )
                        ShortcutChip(
                            icon = Icons.Default.NewReleases,
                            label = "Recently Added",
                            onClick = { onSmartPlaylistClick(SmartPlaylistType.RECENTLY_ADDED) },
                            tint = AccentCyan
                        )
                        ShortcutChip(icon = Icons.Default.Folder, label = "Folders", onClick = onFoldersClick, tint = AccentSecondary)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (recentSongs.isNotEmpty()) {
                        SectionHeader(title = "Recently played")
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            items(recentSongs.take(10), key = { it.contentUri.toString() }) { song ->
                                RecentSongCard(song = song, onClick = { viewModel.playSong(song) })
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (recentAlbums.isNotEmpty()) {
                        SectionHeader(title = "Albums", actionLabel = "See all", onAction = onSeeAllAlbums)
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
                        SectionHeader(title = "Artists", actionLabel = "See all", onAction = onSeeAllArtists)
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
    isRecent: Boolean,
    onPlay: () -> Unit,
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
            .clickable(onClick = onPlay)
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
                    text = if (isRecent) "JUMP BACK IN" else "START LISTENING",
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
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(50.dp)
                    .shadow(12.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = BackgroundDark,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
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

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradient: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier = modifier
            .height(68.dp)
            .clip(shape)
            .background(SurfaceCard)
            .background(Brush.horizontalGradient(listOf(gradient[0].copy(alpha = 0.28f), Color.Transparent)))
            .border(1.dp, StrokeSubtle, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(gradient)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = TextPrimary, maxLines = 1)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1)
        }
    }
}

@Composable
internal fun ShortcutChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = AccentPrimary
) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(SurfaceCard)
            .border(1.dp, StrokeSubtle, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, color = TextPrimary)
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
