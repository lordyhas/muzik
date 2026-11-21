package com.lordyhas.muzik.tageditor

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lordyhas.muzik.tageditor.ui.GroupTagEditorScreen
import com.lordyhas.muzik.tageditor.ui.SongTagEditorScreen
import com.lordyhas.muzik.tageditor.ui.TagGroupKind
import com.lordyhas.muzik.tageditor.ui.TagLibraryScreen
import com.lordyhas.muzik.ui.components.LoadingState
import com.lordyhas.muzik.ui.components.MuzikSnackbarHost
import com.lordyhas.muzik.ui.components.PermissionRequestView
import com.lordyhas.muzik.ui.theme.AppThemeStore
import com.lordyhas.muzik.ui.theme.BackgroundDark
import com.lordyhas.muzik.ui.theme.MuzikTheme
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * The Tag editor: a second "app" inside Muzik, with its own launcher icon and its own task, so it
 * shows up separately in recent apps while the player keeps playing. It shares the library and
 * the code with the player, and writes tags straight into the audio files.
 */
class TagEditorActivity : ComponentActivity() {

    private val viewModel: TagEditorViewModel by viewModels()

    /** Page to open directly ("song/<id>" or "album/<id>", from "Edit tags" in the player); consumed once opened. */
    private val openRoute = MutableStateFlow<String?>(null)
    private val hasReadPermission = MutableStateFlow(true)
    private val showConfirmationTip = MutableStateFlow(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppThemeStore.init(this)
        enableEdgeToEdge()
        if (savedInstanceState == null) openRoute.value = intent.routeExtra()

        setContent {
            MuzikTheme {
                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { granted ->
                    hasReadPermission.value = granted
                    if (granted) viewModel.load()
                }
                val hasPermission by hasReadPermission.collectAsState()
                val tip by showConfirmationTip.collectAsState()
                val routeToOpen by openRoute.collectAsState()

                LaunchedEffect(Unit) {
                    if (!hasReadPermission(this@TagEditorActivity)) permissionLauncher.launch(readPermission())
                }

                Surface(modifier = Modifier.fillMaxSize(), color = BackgroundDark) {
                    if (!hasPermission) {
                        PermissionRequestView(onRequestPermission = { permissionLauncher.launch(readPermission()) })
                    } else {
                        TagEditorApp(
                            viewModel = viewModel,
                            routeToOpen = routeToOpen,
                            onRouteOpened = { openRoute.value = null },
                            showConfirmationTip = tip,
                            onAllowWithoutConfirmation = { WriteAccess.openMediaManagementSettings(this) },
                            onDismissTip = {
                                tipPrefs().edit { putBoolean(KEY_TIP_DISMISSED, true) }
                                showConfirmationTip.value = false
                            },
                            onClose = { finish() }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.routeExtra()?.let { openRoute.value = it }
    }

    override fun onResume() {
        super.onResume()
        val granted = hasReadPermission(this)
        hasReadPermission.value = granted
        // Picks up files changed elsewhere (and the player's rescans) when coming back
        if (granted) viewModel.load()
        showConfirmationTip.value = WriteAccess.supportsSkippingConfirmation() &&
            !WriteAccess.canSkipConfirmation(this) &&
            !tipPrefs().getBoolean(KEY_TIP_DISMISSED, false)
    }

    private fun tipPrefs() = getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    companion object {
        private const val EXTRA_SONG_ID = "com.lordyhas.muzik.extra.SONG_ID"
        private const val EXTRA_ALBUM_ID = "com.lordyhas.muzik.extra.ALBUM_ID"
        private const val PREFS = "muzik_tag_editor"
        private const val KEY_TIP_DISMISSED = "confirmation_tip_dismissed"

        /** Opens the Tag editor in its own task, optionally straight on one song or one album. */
        fun intent(context: Context, songId: Long? = null, albumId: Long? = null): Intent =
            Intent(context, TagEditorActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                songId?.let { putExtra(EXTRA_SONG_ID, it) }
                albumId?.let { putExtra(EXTRA_ALBUM_ID, it) }
            }

        private fun Intent.longExtra(key: String): Long? =
            if (hasExtra(key)) getLongExtra(key, -1L).takeIf { it >= 0 } else null

        private fun Intent.routeExtra(): String? =
            longExtra(EXTRA_SONG_ID)?.let { "song/$it" } ?: longExtra(EXTRA_ALBUM_ID)?.let { "album/$it" }

        private fun readPermission() =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_AUDIO
            else Manifest.permission.READ_EXTERNAL_STORAGE

        private fun hasReadPermission(context: Context) =
            ContextCompat.checkSelfPermission(context, readPermission()) == PackageManager.PERMISSION_GRANTED
    }
}

private const val LIBRARY_ROUTE = "library"
private const val SONG_ROUTE = "song/{songId}"
private const val ALBUM_ROUTE = "album/{albumId}"
private const val ARTIST_ROUTE = "artist/{artistName}"

@Composable
private fun TagEditorApp(
    viewModel: TagEditorViewModel,
    routeToOpen: String?,
    onRouteOpened: () -> Unit,
    showConfirmationTip: Boolean,
    onAllowWithoutConfirmation: () -> Unit,
    onDismissTip: () -> Unit,
    onClose: () -> Unit
) {
    val navController = rememberNavController()

    LaunchedEffect(routeToOpen) {
        if (routeToOpen != null) {
            navController.navigate(routeToOpen) { launchSingleTop = true }
            onRouteOpened()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = LIBRARY_ROUTE,
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it / 3 }) + fadeOut() },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it / 3 }) + fadeIn() },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
        ) {
            composable(LIBRARY_ROUTE) {
                TagLibraryScreen(
                    viewModel = viewModel,
                    showConfirmationTip = showConfirmationTip,
                    onAllowWithoutConfirmation = onAllowWithoutConfirmation,
                    onDismissTip = onDismissTip,
                    onOpenAlbum = { navController.navigate("album/${it.id}") },
                    onOpenArtist = { navController.navigate("artist/${Uri.encode(it.name)}") },
                    onOpenSong = { navController.navigate("song/${it.id}") },
                    onClose = onClose
                )
            }
            composable(SONG_ROUTE, arguments = listOf(navArgument("songId") { type = NavType.LongType })) { entry ->
                val songId = entry.arguments?.getLong("songId") ?: -1L
                val all by viewModel.songs.collectAsState()
                val isLoading by viewModel.isLoading.collectAsState()
                val song = rememberFirst(all.firstOrNull { it.id == songId })
                if (song == null && isLoading) LoadingState() else SongTagEditorScreen(
                    viewModel = viewModel,
                    song = song,
                    onBack = { if (!navController.popBackStack()) onClose() }
                )
            }
            composable(ALBUM_ROUTE, arguments = listOf(navArgument("albumId") { type = NavType.LongType })) { entry ->
                val albumId = entry.arguments?.getLong("albumId") ?: -1L
                val albums by viewModel.albums.collectAsState()
                // Keeps the songs this page opened with: saving can regroup albums under new ids
                val songs = rememberFirst(albums.firstOrNull { it.id == albumId }?.songs)
                if (songs == null) LoadingState() else GroupTagEditorScreen(
                    viewModel = viewModel,
                    kind = TagGroupKind.ALBUM,
                    title = songs.firstOrNull()?.album ?: "Album",
                    songs = songs,
                    onOpenSong = { navController.navigate("song/${it.id}") },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(ARTIST_ROUTE) { entry ->
                val name = Uri.decode(entry.arguments?.getString("artistName").orEmpty())
                val all by viewModel.songs.collectAsState()
                val songs = rememberFirst(viewModel.artistSongs(name).takeIf { all.isNotEmpty() })
                if (songs == null) LoadingState() else GroupTagEditorScreen(
                    viewModel = viewModel,
                    kind = TagGroupKind.ARTIST,
                    title = name,
                    songs = songs,
                    onOpenSong = { navController.navigate("song/${it.id}") },
                    onBack = { navController.popBackStack() }
                )
            }
        }

        MuzikSnackbarHost(
            messages = viewModel.messages,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .imePadding()
                .padding(bottom = 12.dp)
        )
    }
}

/** The first non-null [value] seen, kept afterwards even if [value] changes. */
@Composable
private fun <T : Any> rememberFirst(value: T?): T? {
    var kept by remember { mutableStateOf<T?>(null) }
    if (kept == null && value != null) kept = value
    return kept
}
