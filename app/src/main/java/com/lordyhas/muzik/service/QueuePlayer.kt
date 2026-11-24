package com.lordyhas.muzik.service

import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player

/**
 * Applies [QueueRules] to a Media3 [Player]. The same functions run on the playback service's
 * ExoPlayer (loops, Android Auto) and on the app's MediaController (user actions), so the
 * queue behaves the same whether the app is open or not.
 *
 * Each item carries its queue marks in its metadata extras: whether it was added with
 * "Add to queue", and its place in the album/playlist. The media id stays the song id.
 */
object QueuePlayer {

    private const val KEY_ADDED = "com.lordyhas.muzik.queue.ADDED"
    private const val KEY_ORDER = "com.lordyhas.muzik.queue.ORDER"

    val MediaItem.isAddedToQueue: Boolean
        get() = mediaMetadata.extras?.getBoolean(KEY_ADDED, false) == true

    /** The item's place in its album/playlist, or [fallback] when it has none. */
    fun MediaItem.contextOrder(fallback: Int): Int =
        mediaMetadata.extras?.getInt(KEY_ORDER, fallback) ?: fallback

    fun MediaItem.withQueueMarks(added: Boolean, order: Int): MediaItem {
        val extras = Bundle(mediaMetadata.extras ?: Bundle()).apply {
            putBoolean(KEY_ADDED, added)
            putInt(KEY_ORDER, order)
        }
        return buildUpon()
            .setMediaMetadata(mediaMetadata.buildUpon().setExtras(extras).build())
            .build()
    }

    /** [this] item with the queue marks of [source], when [source] has any. */
    fun MediaItem.withQueueMarksOf(source: MediaItem): MediaItem {
        val extras = source.mediaMetadata.extras ?: return this
        if (!extras.containsKey(KEY_ADDED) && !extras.containsKey(KEY_ORDER)) return this
        return withQueueMarks(source.isAddedToQueue, source.contextOrder(-1))
    }

    fun Player.queueItems(): List<MediaItem> = (0 until mediaItemCount).map { getMediaItemAt(it) }

    fun Player.queueEntries(): List<QueueEntry> = queueItems().mapIndexed { index, item ->
        QueueEntry(
            songId = item.mediaId.toLongOrNull() ?: -1L,
            added = item.isAddedToQueue,
            order = item.contextOrder(index),
            tag = index
        )
    }

    /** "Add to queue": [items] go right after the current song and the added songs after it. */
    /** Removes every queue item playing one of [songIds]; the player moves on if it was current. */
    fun Player.removeSongs(songIds: Set<Long>) {
        for (index in mediaItemCount - 1 downTo 0) {
            if (getMediaItemAt(index).mediaId.toLongOrNull() in songIds) removeMediaItem(index)
        }
    }

    fun Player.addToQueue(items: List<MediaItem>) {
        if (items.isEmpty()) return
        val index = QueueRules.addIndex(queueEntries(), currentMediaItemIndex)
        addMediaItems(index, items.map { it.withQueueMarks(added = true, order = -1) })
    }

    /** Shuffles, or puts back in order, the album/playlist songs still to come. */
    fun Player.reorderUpcoming(shuffle: Boolean) {
        val entries = queueEntries()
        val current = currentMediaItemIndex
        val target = if (shuffle) QueueRules.shuffled(entries, current) else QueueRules.unshuffled(entries, current)
        replaceUpcoming(target, current)
    }

    /**
     * True when this transition is the album/playlist looping back to its first song, i.e. it
     * moved from the last item to the first with repeat all.
     */
    fun Player.isLoopTransition(previousIndex: Int, previousCount: Int, reason: Int): Boolean =
        repeatMode == Player.REPEAT_MODE_ALL &&
            (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO || reason == Player.MEDIA_ITEM_TRANSITION_REASON_SEEK) &&
            previousCount > 1 && previousIndex == previousCount - 1 && currentMediaItemIndex == 0

    /** After a loop: drops the added songs and, with [shuffle], reshuffles the album. */
    fun Player.cleanUpAfterLoop(shuffle: Boolean) {
        val items = queueItems()
        val (target, newCurrent) = QueueRules.afterLoop(queueEntries(), currentMediaItemIndex, shuffle)
        val keptTags = target.map { it.tag }.toSet()
        items.indices.filter { it !in keptTags }.sortedDescending().forEach { removeMediaItem(it) }
        if (shuffle) replaceUpcoming(target, newCurrent, itemsByTag = items.withIndex().associate { it.index to it.value })
    }

    /** Puts the items after [current] in the order of [target] (entries tagged with their index). */
    private fun Player.replaceUpcoming(
        target: List<QueueEntry>,
        current: Int,
        itemsByTag: Map<Int, MediaItem?> = queueItems().withIndex().associate { it.index to it.value }
    ) {
        val start = current + 1
        if (start >= mediaItemCount) return
        val upcoming = target.drop(start).mapNotNull { itemsByTag[it.tag] }
        if (upcoming.size != mediaItemCount - start) return
        replaceMediaItems(start, mediaItemCount, upcoming)
    }
}
