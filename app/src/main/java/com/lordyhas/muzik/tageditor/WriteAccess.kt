package com.lordyhas.muzik.tageditor

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.net.toUri

/** Asks Android for permission to modify some audio files, then runs the pending save. */
class WriteAccess internal constructor(
    private val context: Context,
    private val launchWriteRequest: (IntentSenderRequest) -> Unit,
    private val launchLegacyPermission: () -> Unit,
    private val setPending: ((Boolean) -> Unit) -> Unit
) {
    /**
     * Calls [onResult] with true once the app may write [uris]. Android 11+ shows one system
     * dialog for all of them (skipped when "media management" access is on). Android 10 and
     * older use the storage write permission.
     */
    fun request(uris: List<Uri>, onResult: (Boolean) -> Unit) {
        if (uris.isEmpty()) return onResult(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            setPending(onResult)
            val sender = MediaStore.createWriteRequest(context.contentResolver, uris).intentSender
            launchWriteRequest(IntentSenderRequest.Builder(sender).build())
        } else if (hasLegacyWritePermission(context)) {
            onResult(true)
        } else {
            setPending(onResult)
            launchLegacyPermission()
        }
    }

    companion object {
        private fun hasLegacyWritePermission(context: Context) =
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED

        /**
         * Android 12+ "media management" special access lets the app edit files without a
         * confirmation dialog for every save.
         */
        fun canSkipConfirmation(context: Context): Boolean =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && MediaStore.canManageMedia(context)

        fun supportsSkippingConfirmation(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

        fun openMediaManagementSettings(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
            val intent = Intent(Settings.ACTION_REQUEST_MANAGE_MEDIA, "package:${context.packageName}".toUri())
            runCatching { context.startActivity(intent) }
        }
    }
}

@Composable
fun rememberWriteAccess(): WriteAccess {
    val context = LocalContext.current
    val pending = remember { arrayOfNulls<(Boolean) -> Unit>(1) }
    fun finish(granted: Boolean) {
        pending[0]?.invoke(granted)
        pending[0] = null
    }

    val writeRequest = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
        finish(it.resultCode == Activity.RESULT_OK)
    }
    val legacyPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        finish(it)
    }
    return remember(context) {
        WriteAccess(
            context = context,
            launchWriteRequest = { writeRequest.launch(it) },
            launchLegacyPermission = { legacyPermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE) },
            setPending = { pending[0] = it }
        )
    }
}
