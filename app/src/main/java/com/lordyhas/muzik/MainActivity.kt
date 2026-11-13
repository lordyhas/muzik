package com.lordyhas.muzik

import android.Manifest
import android.app.SearchManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import com.lordyhas.muzik.ui.screens.MainAppScreen
import com.lordyhas.muzik.ui.theme.BackgroundDark
import com.lordyhas.muzik.ui.theme.MuzikTheme
import com.lordyhas.muzik.ui.viewmodel.MusicViewModel

@UnstableApi
class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MusicViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel = androidx.lifecycle.ViewModelProvider(this)[MusicViewModel::class.java]
        if (savedInstanceState == null) handleSearchIntent(intent)

        setContent {
            MuzikTheme {
                val permissionsToRequest = getRequiredPermissions()

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { results ->
                    val hasStorage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        results[Manifest.permission.READ_MEDIA_AUDIO] == true
                    } else {
                        results[Manifest.permission.READ_EXTERNAL_STORAGE] == true
                    }
                    viewModel.setPermissionGranted(hasStorage)
                }

                LaunchedEffect(Unit) {
                    val hasPermissions = checkPermissions()
                    viewModel.setPermissionGranted(hasPermissions)
                    if (!hasPermissions) {
                        permissionLauncher.launch(permissionsToRequest)
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundDark
                ) {
                    MainAppScreen(
                        viewModel = viewModel,
                        onRequestPermissions = {
                            permissionLauncher.launch(permissionsToRequest)
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleSearchIntent(intent)
    }

    /** "Play <query> on Muzik" from the Assistant or other apps (MEDIA_PLAY_FROM_SEARCH). */
    private fun handleSearchIntent(intent: Intent?) {
        if (intent?.action != MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH) return
        viewModel.playFromSearch(intent.getStringExtra(SearchManager.QUERY).orEmpty())
    }

    override fun onResume() {
        super.onResume()
        val hasPermissions = checkPermissions()
        if (hasPermissions) {
            viewModel.setPermissionGranted(true)
            viewModel.loadSongs()
        }
    }

    private fun getRequiredPermissions(): Array<String> {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        return permissions.toTypedArray()
    }

    private fun checkPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}