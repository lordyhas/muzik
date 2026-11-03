package com.lordyhas.muzik.ui.screens

import android.app.Activity
import android.widget.Toast
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lordyhas.muzik.data.model.SmartPlaylistType
import com.lordyhas.muzik.ui.components.AddToPlaylistSheet
import com.lordyhas.muzik.ui.components.AudioEffectsSheet
import com.lordyhas.muzik.ui.components.SleepTimerSheet
import com.lordyhas.muzik.ui.components.CreatePlaylistDialog
import com.lordyhas.muzik.ui.components.MiniPlayer
import com.lordyhas.muzik.ui.components.MuzikSnackbarHost
import com.lordyhas.muzik.ui.components.NowPlayingScreen
import com.lordyhas.muzik.ui.components.PermissionRequestView
import com.lordyhas.muzik.ui.components.SongDetailsDialog
import com.lordyhas.muzik.ui.theme.AccentPrimary
import com.lordyhas.muzik.ui.theme.AccentSecondary
import com.lordyhas.muzik.ui.theme.BackgroundDark
import com.lordyhas.muzik.ui.theme.SurfaceCard
import com.lordyhas.muzik.ui.theme.SurfaceDark
import com.lordyhas.muzik.ui.theme.SurfaceElevated
import com.lordyhas.muzik.ui.theme.TextMuted
import com.lordyhas.muzik.ui.theme.TextPrimary
import com.lordyhas.muzik.ui.theme.TrackBackground
import com.lordyhas.muzik.ui.viewmodel.MusicViewModel

enum class NavigationTab(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val icon: ImageVector
) {
    HOME("home", "Home", Icons.Filled.Home, Icons.Outlined.Home),
    TRACKS("tracks", "Songs", Icons.Filled.MusicNote, Icons.Outlined.MusicNote),
    ALBUMS("albums", "Albums", Icons.Filled.Album, Icons.Outlined.Album),
    ARTISTS("artists", "Artists", Icons.Filled.Person, Icons.Outlined.Person),
    PLAYLISTS("playlists", "Playlists", Icons.AutoMirrored.Filled.PlaylistPlay, Icons.AutoMirrored.Outlined.PlaylistPlay)
}

private const val PLAYLIST_EDIT_ROUTE = "playlist/{playlistId}/edit"

private val TAB_ROUTES = NavigationTab.entries.map { it.route }.toSet()

@UnstableApi
@Composable
fun MainAppScreen(
    viewModel: MusicViewModel,
    onRequestPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPermissionGranted by viewModel.isPermissionGranted.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val isShuffle by viewModel.isShuffle.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val showNowPlaying by viewModel.showNowPlaying.collectAsState()
    val currentQueue by viewModel.currentQueue.collectAsState()
    val currentQueueIndex by viewModel.currentQueueIndex.collectAsState()
    val selectedSongForDetails by viewModel.selectedSongForDetails.collectAsState()
    val songPendingAddToPlaylist by viewModel.songPendingAddToPlaylist.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val showCreatePlaylistDialog by viewModel.showCreatePlaylistDialog.collectAsState()
    val sleepTimer by viewModel.sleepTimer.collectAsState()
    val likedIds by viewModel.likedSongIds.collectAsState()
    val showLyrics by viewModel.showLyrics.collectAsState()
    val lyricsState by viewModel.lyrics.collectAsState()
    val playbackSpeed by viewModel.playbackSpeed.collectAsState()
    val equalizer by viewModel.equalizer.collectAsState()
    val equalizerInfo by viewModel.equalizerInfo.collectAsState()
    var showSleepTimerSheet by remember { mutableStateOf(false) }
    var showAudioEffectsSheet by remember { mutableStateOf(false) }
    // Height of mini player + nav bar, so confirmations appear just above them
    var bottomBarHeight by remember { mutableStateOf(0.dp) }

    val navController = rememberNavController()
    val backStack by navController.currentBackStack.collectAsState()
    val currentRoute = backStack.lastOrNull()?.destination?.route

    val context = LocalContext.current
    var backPressedTime by remember { mutableLongStateOf(0L) }

    // The tab whose stack is showing: the current route on a tab root, or the tab a
    // detail screen was opened from, so the bar keeps that tab highlighted
    val activeTab = remember(backStack) {
        backStack.lastOrNull { it.destination.route in TAB_ROUTES }?.destination?.route
    }

    // Tabs always land on their root list. The tab being left is first popped back to
    // its root, so only that root (and its scroll position) is saved for restoreState.
    fun navigateToTab(tab: NavigationTab) {
        if (tab.route == activeTab) {
            navController.popBackStack(tab.route, inclusive = false)
            return
        }
        activeTab?.let { navController.popBackStack(it, inclusive = false) }
        navController.navigate(tab.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun safePopBack() {
        if (!navController.popBackStack()) {
            navigateToTab(NavigationTab.HOME)
        }
    }

    if (!isPermissionGranted) {
        PermissionRequestView(onRequestPermission = onRequestPermissions)
        return
    }

    val isTopLevelRoute = currentRoute in TAB_ROUTES

    // Intercept back for overlays: AddToPlaylistSheet & SongDetailsDialog
    BackHandler(enabled = songPendingAddToPlaylist != null && !showCreatePlaylistDialog) {
        viewModel.setSongPendingAddToPlaylist(null)
    }

    BackHandler(enabled = selectedSongForDetails != null) {
        viewModel.selectSongForDetails(null)
    }

    // Intercept back on secondary tabs: go back to Home tab first
    BackHandler(
        enabled = isTopLevelRoute && currentRoute != NavigationTab.HOME.route && !showNowPlaying &&
                selectedSongForDetails == null && songPendingAddToPlaylist == null
    ) {
        navigateToTab(NavigationTab.HOME)
    }

    // On Home tab: double back press to exit application safely
    BackHandler(
        enabled = currentRoute == NavigationTab.HOME.route && !showNowPlaying &&
                selectedSongForDetails == null && songPendingAddToPlaylist == null && !showCreatePlaylistDialog
    ) {
        val now = System.currentTimeMillis()
        if (now - backPressedTime < 2000L) {
            (context as? Activity)?.finish()
        } else {
            backPressedTime = now
            Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
        }
    }

    fun navigateToAlbum(albumId: Long) {
        navController.navigate("album/$albumId")
    }

    fun navigateToArtist(artistName: String) {
        val encoded = URLEncoder.encode(artistName, StandardCharsets.UTF_8.toString())
        navController.navigate("artist/$encoded")
    }

    fun navigateToPlaylist(playlistId: String) {
        val encoded = URLEncoder.encode(playlistId, StandardCharsets.UTF_8.toString())
        navController.navigate("playlist/$encoded")
    }

    fun navigateToPlaylistEdit(playlistId: String) {
        val encoded = URLEncoder.encode(playlistId, StandardCharsets.UTF_8.toString())
        navController.navigate("playlist/$encoded/edit")
    }

    // The editor is full screen, like Spotify's: no mini player or tab bar under it
    val isEditingPlaylist = currentRoute == PLAYLIST_EDIT_ROUTE

    Box(modifier = modifier.fillMaxSize().background(Color.Transparent)) {
        Scaffold(
            containerColor = BackgroundDark,
            bottomBar = {
                if (!isEditingPlaylist) Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                            colors = listOf(Color.Transparent, SurfaceDark)
                        ))
                ) {
                    // Mini Player anchored above navigation bar or bottom insets
                    if (currentSong != null) {
                        MiniPlayer(
                            song = currentSong,
                            isPlaying = isPlaying,
                            currentPosition = currentPosition,
                            duration = duration,
                            onTogglePlayPause = { viewModel.togglePlayPause() },
                            onSkipNext = { viewModel.skipNext() },
                            onSkipPrevious = { viewModel.skipPrevious() },
                            onClick = { viewModel.setShowNowPlaying(true) }
                        )
                    }

                    // Bottom Navigation Bar (always visible)
                    NavigationBar(
                            containerColor = SurfaceDark,
                            tonalElevation = 0.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                        ) {
                            NavigationTab.values().forEach { tab ->
                                val isSelected = activeTab == tab.route
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = {
                                        if (currentRoute != tab.route) navigateToTab(tab)
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (isSelected) tab.selectedIcon else tab.icon,
                                            contentDescription = tab.title
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tab.title,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = AccentPrimary,
                                        selectedTextColor = TextPrimary,
                                        unselectedIconColor = TextMuted,
                                        unselectedTextColor = TextMuted,
                                        indicatorColor = AccentPrimary.copy(alpha = 0.14f)
                                    )
                                )
                        }
                    }
                }
            }
        ) { paddingValues ->
            bottomBarHeight = paddingValues.calculateBottomPadding()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding())
            ) {
                NavHost(
                    navController = navController,
                    startDestination = NavigationTab.HOME.route,
                    enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { -it / 3 }) + fadeOut() },
                    popEnterTransition = { slideInHorizontally(initialOffsetX = { -it / 3 }) + fadeIn() },
                    popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
                ) {
                    composable(NavigationTab.HOME.route) {
                        HomeScreen(
                            viewModel = viewModel,
                            onAlbumClick = { id -> navigateToAlbum(id) },
                            onArtistClick = { name -> navigateToArtist(name) },
                            onSeeAllAlbums = { navigateToTab(NavigationTab.ALBUMS) },
                            onSeeAllArtists = { navigateToTab(NavigationTab.ARTISTS) },
                            onSearchClick = { navController.navigate("search") },
                            onFoldersClick = { navController.navigate("folders") },
                            onSmartPlaylistClick = { type -> navController.navigate("smart/${type.name}") }
                        )
                    }

                    composable(NavigationTab.TRACKS.route) {
                        TracksScreen(
                            viewModel = viewModel,
                            onGoToArtist = { name -> navigateToArtist(name) },
                            onGoToAlbum = { id -> navigateToAlbum(id) }
                        )
                    }

                    composable(NavigationTab.ALBUMS.route) {
                        AlbumsScreen(
                            viewModel = viewModel,
                            onAlbumClick = { id -> navigateToAlbum(id) }
                        )
                    }

                    composable(NavigationTab.ARTISTS.route) {
                        ArtistsScreen(
                            viewModel = viewModel,
                            onArtistClick = { name -> navigateToArtist(name) }
                        )
                    }

                    composable(NavigationTab.PLAYLISTS.route) {
                        PlaylistsScreen(
                            viewModel = viewModel,
                            onSmartPlaylistClick = { type -> navController.navigate("smart/${type.name}") },
                            onPlaylistClick = { id -> navigateToPlaylist(id) },
                            onEditPlaylist = { id -> navigateToPlaylistEdit(id) }
                        )
                    }

                    // ─── Detail Destinations ─────────────────────────────────────────

                    composable("search") {
                        SearchScreen(
                            viewModel = viewModel,
                            onBackClick = { safePopBack() },
                            onAlbumClick = { id -> navigateToAlbum(id) },
                            onArtistClick = { name -> navigateToArtist(name) },
                            onPlaylistClick = { id -> navigateToPlaylist(id) },
                            onFoldersClick = { navController.navigate("folders") }
                        )
                    }

                    composable(
                        route = "smart/{type}",
                        arguments = listOf(navArgument("type") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val type = SmartPlaylistType.entries.firstOrNull {
                            it.name == backStackEntry.arguments?.getString("type")
                        } ?: SmartPlaylistType.LIKED
                        SmartPlaylistScreen(
                            type = type,
                            viewModel = viewModel,
                            onBackClick = { safePopBack() },
                            onGoToArtist = { name -> navigateToArtist(name) },
                            onGoToAlbum = { id -> navigateToAlbum(id) }
                        )
                    }

                    composable("folders") {
                        FoldersScreen(
                            viewModel = viewModel,
                            onBackClick = { safePopBack() },
                            onFolderClick = { path ->
                                navController.navigate("folder/" + URLEncoder.encode(path, StandardCharsets.UTF_8.toString()))
                            }
                        )
                    }

                    composable(
                        route = "folder/{folderPath}",
                        arguments = listOf(navArgument("folderPath") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val encoded = backStackEntry.arguments?.getString("folderPath").orEmpty()
                        FolderDetailScreen(
                            folderPath = URLDecoder.decode(encoded, StandardCharsets.UTF_8.toString()),
                            viewModel = viewModel,
                            onBackClick = { safePopBack() },
                            onGoToArtist = { name -> navigateToArtist(name) },
                            onGoToAlbum = { id -> navigateToAlbum(id) }
                        )
                    }

                    composable(
                        route = "album/{albumId}",
                        arguments = listOf(navArgument("albumId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        AlbumDetailScreen(
                            albumId = backStackEntry.arguments?.getLong("albumId") ?: 0L,
                            viewModel = viewModel,
                            onBackClick = { safePopBack() },
                            onGoToArtist = { name -> navigateToArtist(name) }
                        )
                    }

                    composable(
                        route = "artist/{artistName}",
                        arguments = listOf(navArgument("artistName") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val encodedName = backStackEntry.arguments?.getString("artistName").orEmpty()
                        val artistName = URLDecoder.decode(encodedName, StandardCharsets.UTF_8.toString())
                        ArtistDetailScreen(
                            artistName = artistName,
                            viewModel = viewModel,
                            onBackClick = { safePopBack() },
                            onGoToAlbum = { id -> navigateToAlbum(id) }
                        )
                    }

                    composable(
                        route = "playlist/{playlistId}",
                        arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val encodedId = backStackEntry.arguments?.getString("playlistId").orEmpty()
                        val playlistId = URLDecoder.decode(encodedId, StandardCharsets.UTF_8.toString())
                        PlaylistDetailScreen(
                            playlistId = playlistId,
                            viewModel = viewModel,
                            onBackClick = { safePopBack() },
                            onGoToArtist = { name -> navigateToArtist(name) },
                            onGoToAlbum = { id -> navigateToAlbum(id) },
                            onEditClick = { navigateToPlaylistEdit(playlistId) }
                        )
                    }

                    composable(
                        route = PLAYLIST_EDIT_ROUTE,
                        arguments = listOf(navArgument("playlistId") { type = NavType.StringType }),
                        enterTransition = { slideInVertically(initialOffsetY = { it }) + fadeIn() },
                        exitTransition = { fadeOut() },
                        popEnterTransition = { fadeIn() },
                        popExitTransition = { slideOutVertically(targetOffsetY = { it }) + fadeOut() }
                    ) { backStackEntry ->
                        val encodedId = backStackEntry.arguments?.getString("playlistId").orEmpty()
                        PlaylistEditScreen(
                            playlistId = URLDecoder.decode(encodedId, StandardCharsets.UTF_8.toString()),
                            viewModel = viewModel,
                            onClose = { safePopBack() }
                        )
                    }
                }
            }
        }

        // Fullscreen Now Playing Animation Sheet
        AnimatedVisibility(
            visible = showNowPlaying,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            NowPlayingScreen(
                song = currentSong,
                isPlaying = isPlaying,
                currentPosition = currentPosition,
                duration = duration,
                isShuffle = isShuffle,
                repeatMode = repeatMode,
                queue = currentQueue,
                currentIndex = currentQueueIndex,
                userQueueCount = viewModel.userQueueCount.collectAsState().value,
                onTogglePlayPause = { viewModel.togglePlayPause() },
                onSeekTo = { viewModel.seekTo(it) },
                onSkipNext = { viewModel.skipNext() },
                onSkipPrevious = { viewModel.skipPrevious() },
                onToggleShuffle = { viewModel.toggleShuffle() },
                onToggleRepeat = { viewModel.toggleRepeat() },
                onPlayIndex = { viewModel.playQueueIndex(it) },
                onRemoveIndex = { viewModel.removeQueueItem(it) },
                onMoveQueueItem = { from, to -> viewModel.moveQueueItem(from, to) },
                sleepTimer = sleepTimer,
                playbackSpeed = playbackSpeed,
                onSleepTimerClick = { showSleepTimerSheet = true },
                onAudioEffectsClick = { showAudioEffectsSheet = true },
                isLiked = currentSong?.id?.let { it in likedIds } == true,
                onToggleLike = { currentSong?.let { viewModel.toggleLike(it) } },
                showLyrics = showLyrics,
                lyricsState = lyricsState,
                onToggleLyrics = { viewModel.toggleLyrics() },
                onDismiss = { viewModel.setShowNowPlaying(false) }
            )
        }

        if (showSleepTimerSheet) {
            SleepTimerSheet(
                timer = sleepTimer,
                onSetMinutes = { viewModel.setSleepTimer(it) },
                onEndOfTrack = { viewModel.setSleepAtEndOfTrack() },
                onCancel = { viewModel.cancelSleepTimer() },
                onDismiss = { showSleepTimerSheet = false }
            )
        }

        if (showAudioEffectsSheet) {
            AudioEffectsSheet(
                speed = playbackSpeed,
                onSpeedChange = { viewModel.setPlaybackSpeed(it) },
                equalizer = equalizer,
                equalizerInfo = equalizerInfo,
                onEqualizerEnabled = { viewModel.setEqualizerEnabled(it) },
                onPresetSelected = { viewModel.selectEqualizerPreset(it) },
                onBandLevelChange = { band, level -> viewModel.setEqualizerBandLevel(band, level) },
                onResetEqualizer = { viewModel.resetEqualizer() },
                onDismiss = { showAudioEffectsSheet = false }
            )
        }

        // Song Details Dialog / Bottom Sheet
        selectedSongForDetails?.let { song ->
            SongDetailsDialog(
                song = song,
                onDismiss = { viewModel.selectSongForDetails(null) }
            )
        }

        // ─── Add To Playlist Sheet ─────────────────────────────────────────────
        songPendingAddToPlaylist?.let { song ->
            AddToPlaylistSheet(
                song = song,
                playlists = playlists,
                onAddToPlaylist = { playlist ->
                    viewModel.addSongToPlaylist(playlist.id, song.id)
                    viewModel.setSongPendingAddToPlaylist(null)
                },
                onCreateNew = {
                    // Dismiss the sheet and show create dialog; song is kept pending
                    viewModel.setShowCreatePlaylistDialog(true)
                },
                onDismiss = { viewModel.setSongPendingAddToPlaylist(null) }
            )
        }

        // Confirmations ("Added to queue", "Removed · Undo"), above every overlay
        MuzikSnackbarHost(
            messages = viewModel.messages,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .then(
                    if (showNowPlaying) Modifier.navigationBarsPadding().padding(bottom = 12.dp)
                    else Modifier.padding(bottom = bottomBarHeight)
                )
        )

        // ─── Create Playlist Dialog (global, triggered from AddToPlaylistSheet) ─
        if (showCreatePlaylistDialog && songPendingAddToPlaylist != null) {
            CreatePlaylistDialog(
                title = "New playlist",
                initialName = "",
                onConfirm = { name ->
                    // Create the playlist and add the pending song once it exists
                    viewModel.createPlaylist(name, initialSongId = songPendingAddToPlaylist?.id)
                    viewModel.setShowCreatePlaylistDialog(false)
                    viewModel.setSongPendingAddToPlaylist(null)
                },
                onDismiss = {
                    viewModel.setShowCreatePlaylistDialog(false)
                    // Don't clear pending song so sheet can be reshown
                }
            )
        }
    }
}

