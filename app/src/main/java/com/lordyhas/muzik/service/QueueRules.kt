package com.lordyhas.muzik.service

import kotlin.random.Random

/**
 * One song of the play queue. [added] is true for songs put there with "Add to queue";
 * [order] is the song's place in the album/playlist being played, used to undo shuffle.
 * [tag] travels with the entry untouched (the player uses it for the item's original index).
 */
data class QueueEntry(val songId: Long, val added: Boolean, val order: Int, val tag: Int = 0)

/**
 * The queue is one ordinary list (1, 2, 3, A, B, 4): Previous and Next move through it
 * normally, and added songs stay in it after they played. These rules are pure so they can
 * be unit-tested; [QueuePlayer] applies them to a Media3 player.
 *
 * - "Add to queue" puts songs right after the current one and the added songs that follow it.
 * - Shuffle only reorders the album/playlist songs still to come; added songs keep their slots.
 * - When the album/playlist loops back to its start (repeat all), added songs are dropped and,
 *   with shuffle on, the album is shuffled again.
 * - Playing another album/playlist replaces the whole list, added songs included.
 */
object QueueRules {

    /** Where "Add to queue" inserts. */
    fun addIndex(entries: List<QueueEntry>, current: Int): Int {
        if (entries.isEmpty()) return 0
        var index = (current + 1).coerceIn(0, entries.size)
        while (index < entries.size && entries[index].added) index++
        return index
    }

    /** How many added songs come right after [current] (the "Next in queue" section). */
    fun upcomingAddedCount(entries: List<QueueEntry>, current: Int): Int =
        addIndex(entries, current) - (current + 1).coerceIn(0, entries.size)

    /** The queue with the album/playlist songs after [current] shuffled. */
    fun shuffled(entries: List<QueueEntry>, current: Int, random: Random = Random.Default): List<QueueEntry> =
        reorderAlbumSongs(entries, current) { it.shuffled(random) }

    /** The queue with the album/playlist songs after [current] back in their original order. */
    fun unshuffled(entries: List<QueueEntry>, current: Int): List<QueueEntry> =
        reorderAlbumSongs(entries, current) { songs -> songs.sortedBy { it.order } }

    /**
     * The queue once the album/playlist looped back to [current] (its first song): every added
     * song except the one playing is dropped, and the rest is reshuffled when [shuffle] is on.
     * Returns the new list and the new index of the playing song.
     */
    fun afterLoop(
        entries: List<QueueEntry>,
        current: Int,
        shuffle: Boolean,
        random: Random = Random.Default
    ): Pair<List<QueueEntry>, Int> {
        val kept = entries.filterIndexed { i, entry -> i == current || !entry.added }
        val newCurrent = entries.take(current).count { !it.added }
        val result = if (shuffle) shuffled(kept, newCurrent, random) else kept
        return result to newCurrent
    }

    private fun reorderAlbumSongs(
        entries: List<QueueEntry>,
        current: Int,
        reorder: (List<QueueEntry>) -> List<QueueEntry>
    ): List<QueueEntry> {
        val slots = ((current + 1).coerceAtLeast(0) until entries.size).filter { !entries[it].added }
        if (slots.size < 2) return entries
        val reordered = reorder(slots.map { entries[it] })
        return entries.toMutableList().also { list ->
            slots.forEachIndexed { k, slot -> list[slot] = reordered[k] }
        }
    }
}
