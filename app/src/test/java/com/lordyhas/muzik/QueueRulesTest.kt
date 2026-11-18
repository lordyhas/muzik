package com.lordyhas.muzik

import com.lordyhas.muzik.service.QueueEntry
import com.lordyhas.muzik.service.QueueRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import kotlin.random.Random

/** The queue scenarios agreed for Muzik: album 1..5, added songs A, B, C. */
class QueueRulesTest {

    private fun album(vararg n: Int) = n.map { QueueEntry(it.toLong(), added = false, order = it - 1) }
    private fun added(name: Char) = QueueEntry(name.code.toLong(), added = true, order = -1)
    private val a = added('A')
    private val b = added('B')
    private val c = added('C')

    /** Inserts [songs] the way "Add to queue" does, one call per song. */
    private fun List<QueueEntry>.add(current: Int, vararg songs: QueueEntry): List<QueueEntry> =
        songs.fold(this) { list, song -> list.toMutableList().apply { add(QueueRules.addIndex(list, current), song) } }

    private fun List<QueueEntry>.names() = joinToString(",") {
        if (it.added) it.songId.toInt().toChar().toString() else it.songId.toString()
    }

    @Test
    fun addGoesRightAfterTheCurrentSong() {
        assertEquals("1,2,A,3,4", album(1, 2, 3, 4).add(1, a).names())
    }

    @Test
    fun addedSongsKeepTheirOrder() {
        assertEquals("1,2,A,B,3", album(1, 2, 3).add(1, a, b).names())
    }

    @Test
    fun addWhileAnAddedSongPlaysGoesAfterTheOtherAddedSongs() {
        val queue = album(1, 2, 3).add(1, a, b) // 1, 2, A, B, 3
        assertEquals("1,2,A,B,C,3", queue.add(2, c).names())
    }

    @Test
    fun addAfterGoingBackGoesRightAfterTheCurrentSong() {
        val queue = album(1, 2, 3).add(2, a, b) + album(4) // 1, 2, 3, A, B, 4
        assertEquals("1,2,C,3,A,B,4", queue.add(1, c).names())
    }

    @Test
    fun playedAddedSongsStayForPreviousAndNext() {
        val queue = album(1, 2, 3).add(2, a, b) + album(4)
        // Nothing removes A or B while the album plays: 1, 2, 3, A, B, 4 stays as is
        assertEquals("1,2,3,A,B,4", queue.names())
        assertEquals(0, QueueRules.upcomingAddedCount(queue, 5))
        assertEquals(2, QueueRules.upcomingAddedCount(queue, 2))
        assertEquals(1, QueueRules.upcomingAddedCount(queue, 3))
    }

    @Test
    fun loopingDropsAddedSongs() {
        val queue = album(1, 2).add(1, a, b) + album(3, 4, 5) // 1, 2, A, B, 3, 4, 5 -> loops to 1
        val (after, current) = QueueRules.afterLoop(queue, 0, shuffle = false)
        assertEquals("1,2,3,4,5", after.names())
        assertEquals(0, current)
    }

    @Test
    fun loopingDropsAnAddedSongThatWasLast() {
        val queue = album(1, 2, 3, 4, 5).add(4, a)
        val (after, current) = QueueRules.afterLoop(queue, 0, shuffle = false)
        assertEquals("1,2,3,4,5", after.names())
        assertEquals(0, current)
    }

    @Test
    fun loopingWithShuffleReshufflesTheAlbumOnly() {
        val queue = album(1, 2, 3, 4, 5, 6, 7, 8).add(2, a, b)
        val (after, current) = QueueRules.afterLoop(queue, 0, shuffle = true, random = Random(7))
        assertEquals(0, current)
        assertEquals(1L, after[0].songId)
        assertEquals((1L..8L).toSet(), after.map { it.songId }.toSet())
        assertNotEquals("1,2,3,4,5,6,7,8", after.names())
    }

    @Test
    fun shuffleOnlyMovesUpcomingAlbumSongs() {
        val queue = album(1, 2).add(1, a, b) + album(3, 4, 5, 6, 7) // 1, 2, A, B, 3..7
        val shuffled = QueueRules.shuffled(queue, 1, Random(3))
        assertEquals("1,2,A,B", shuffled.take(4).names())
        assertEquals(setOf(3L, 4L, 5L, 6L, 7L), shuffled.drop(4).map { it.songId }.toSet())
        assertNotEquals("3,4,5,6,7", shuffled.drop(4).names())
    }

    @Test
    fun shuffleKeepsAddedSongsInTheirSlotsFurtherDown() {
        val queue = album(1, 2).add(1, c) + album(3).add(3, a, b) + album(4, 5, 6) // 1,2,C,3,A,B,4,5,6
        val shuffled = QueueRules.shuffled(queue, 1, Random(5))
        assertEquals(listOf(2, 4, 5), shuffled.indices.filter { shuffled[it].added })
    }

    @Test
    fun unshuffleRestoresAlbumOrder() {
        val queue = album(1, 2).add(1, a, b) + album(3, 4, 5, 6, 7)
        val back = QueueRules.unshuffled(QueueRules.shuffled(queue, 1, Random(11)), 1)
        assertEquals(queue.names(), back.names())
    }

    @Test
    fun addToAnEmptyQueueStartsAtZero() {
        assertEquals(0, QueueRules.addIndex(emptyList(), 0))
    }
}
