package com.lordyhas.muzik.service

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession.MediaItemsWithStartPosition
import com.lordyhas.muzik.data.model.Song
import com.lordyhas.muzik.service.QueuePlayer.queueEntries

/** Snapshot of the playback session, restored on the next launch. */
data class SavedSession(
    val songIds: List<Long>,
    val index: Int,
    val positionMs: Long,
    /** Indexes of the songs put in the queue with "Add to queue" (see QueueRules). */
    val addedIndexes: Set<Int>,
    /** Each song's place in its album/playlist, to undo shuffle; same size as [songIds]. */
    val orders: List<Int>,
    val repeatMode: Int
) {
    /** The player items to restore, skipping songs that are no longer on the device. */
    @UnstableApi
    fun toMediaItems(songsById: Map<Long, Song>): MediaItemsWithStartPosition? {
        val kept = songIds.indices.filter { songsById[songIds[it]] != null }
        if (kept.isEmpty()) return null
        val items = kept.map { i ->
            songsById.getValue(songIds[i]).toMediaItem(contextOrder = orders.getOrElse(i) { i }, addedToQueue = i in addedIndexes)
        }
        // If the saved song was deleted, start from the next one still there
        val exact = kept.indexOf(index)
        val start = if (exact >= 0) exact else kept.indexOfFirst { it > index }.takeIf { it >= 0 } ?: 0
        return MediaItemsWithStartPosition(items, start, if (exact >= 0) positionMs else C.TIME_UNSET)
    }
}

/**
 * Persists the current queue and position in SharedPreferences. The app and the playback
 * service both save straight from their player, since every item carries its queue marks.
 */
object PlaybackStateStore {

    private const val PREFS = "muzik_session"
    private const val KEY_IDS = "ids"
    private const val KEY_INDEX = "index"
    private const val KEY_POSITION = "position"
    private const val KEY_ADDED = "added"
    private const val KEY_ORDERS = "orders"
    private const val KEY_REPEAT = "repeat"

    // Before the queue marks: a count of queued songs after the current one, and the album order
    private const val LEGACY_KEY_USER_QUEUE = "user_queue"
    private const val LEGACY_KEY_CONTEXT = "context_ids"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun save(context: Context, player: Player) {
        if (player.mediaItemCount == 0) return
        val entries = player.queueEntries()
        prefs(context).edit()
            .putString(KEY_IDS, entries.joinToString(",") { it.songId.toString() })
            .putInt(KEY_INDEX, player.currentMediaItemIndex)
            .putLong(KEY_POSITION, player.currentPosition.coerceAtLeast(0L))
            .putString(KEY_ADDED, entries.indices.filter { entries[it].added }.joinToString(","))
            .putString(KEY_ORDERS, entries.joinToString(",") { it.order.toString() })
            .putInt(KEY_REPEAT, player.repeatMode)
            .remove(LEGACY_KEY_USER_QUEUE)
            .remove(LEGACY_KEY_CONTEXT)
            .apply()
    }

    fun load(context: Context): SavedSession? {
        val p = prefs(context)
        val ids = p.getString(KEY_IDS, null).toLongList()
        if (ids.isEmpty()) return null
        val index = p.getInt(KEY_INDEX, 0).coerceIn(0, ids.lastIndex)

        val added = if (p.contains(KEY_ADDED)) {
            p.getString(KEY_ADDED, null).toLongList().map { it.toInt() }.toSet()
        } else {
            val count = p.getInt(LEGACY_KEY_USER_QUEUE, 0).coerceAtLeast(0)
            (index + 1..index + count).filter { it <= ids.lastIndex }.toSet()
        }
        val orders = p.getString(KEY_ORDERS, null).toLongList().map { it.toInt() }
            .takeIf { it.size == ids.size }
            ?: legacyOrders(ids, p.getString(LEGACY_KEY_CONTEXT, null).toLongList())

        return SavedSession(
            songIds = ids,
            index = index,
            positionMs = p.getLong(KEY_POSITION, 0L).coerceAtLeast(0L),
            addedIndexes = added,
            orders = orders,
            repeatMode = p.getInt(KEY_REPEAT, Player.REPEAT_MODE_OFF)
        )
    }

    private fun legacyOrders(ids: List<Long>, contextIds: List<Long>): List<Int> =
        ids.mapIndexed { i, id -> contextIds.indexOf(id).takeIf { it >= 0 } ?: i }

    private fun String?.toLongList(): List<Long> =
        this?.split(",")?.mapNotNull { it.trim().toLongOrNull() }.orEmpty()
}
