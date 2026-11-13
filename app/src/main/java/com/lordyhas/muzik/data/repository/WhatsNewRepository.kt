package com.lordyhas.muzik.data.repository

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import com.lordyhas.muzik.data.AppVersion
import com.lordyhas.muzik.data.model.ReleaseNotes
import com.lordyhas.muzik.data.model.parseReleaseNotes
import com.lordyhas.muzik.data.model.releaseNotesToShow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Release notes bundled in `assets/whatsnew.json`, and which of them the user has already seen. */
class WhatsNewRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("muzik_prefs", Context.MODE_PRIVATE)

    /** Every release's notes, newest first; empty if the file is missing or broken. */
    suspend fun allReleaseNotes(): List<ReleaseNotes> = withContext(Dispatchers.IO) {
        runCatching {
            context.assets.open(ASSET).bufferedReader().use { parseReleaseNotes(it.readText()) }
        }.onFailure { Log.w(TAG, "Couldn't read $ASSET", it) }.getOrDefault(emptyList())
    }

    /** What to show after an update. Records the current version as seen when there's nothing to show. */
    fun unseen(all: List<ReleaseNotes>, version: AppVersion): List<ReleaseNotes> {
        val lastSeen = if (prefs.contains(KEY_SEEN)) prefs.getLong(KEY_SEEN, 0L) else null
        val toShow = releaseNotesToShow(all, version.code, lastSeen, version.isFreshInstall)
        if (toShow.isEmpty()) markSeen(version)
        return toShow
    }

    fun markSeen(version: AppVersion) {
        prefs.edit { putLong(KEY_SEEN, version.code) }
    }

    private companion object {
        const val ASSET = "whatsnew.json"
        const val KEY_SEEN = "whats_new_seen_version_code"
        const val TAG = "WhatsNewRepository"
    }
}
