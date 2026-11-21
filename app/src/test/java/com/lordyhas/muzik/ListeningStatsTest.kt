package com.lordyhas.muzik

import android.net.FakeUri
import com.lordyhas.muzik.data.model.Song
import com.lordyhas.muzik.data.model.formatListenedTime
import com.lordyhas.muzik.data.model.listeningStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ListeningStatsTest {

    private fun song(id: Long, duration: Long = 180_000L) = Song(
        id = id,
        title = "Song $id",
        artist = "Artist",
        album = "Album",
        albumId = 1L,
        duration = duration,
        contentUri = FakeUri("content://media/$id")
    )

    @Test
    fun `nothing played gives no stats`() {
        assertNull(listeningStats(emptyMap(), listOf(song(1))))
        assertNull(listeningStats(mapOf(1L to 0), listOf(song(1))))
    }

    @Test
    fun `totals count only songs still in the library`() {
        val library = listOf(song(1, duration = 120_000), song(2, duration = 60_000), song(3))
        // Song 9 was played but is gone (deleted or hidden), so it doesn't count
        val stats = listeningStats(mapOf(1L to 3, 2L to 5, 9L to 40), library)!!
        assertEquals(8, stats.plays)
        assertEquals(3 * 120_000L + 5 * 60_000L, stats.listenedMs)
        assertEquals(2, stats.explored)
        assertEquals(3, stats.librarySize)
        assertEquals(2L, stats.topSong?.id)
        assertEquals(5, stats.topSongPlays)
    }

    @Test
    fun `listened time reads in minutes then hours`() {
        assertEquals("0 min", formatListenedTime(59_000))
        assertEquals("42 min", formatListenedTime(42 * 60_000L))
        assertEquals("3 h 05 min", formatListenedTime((3 * 60 + 5) * 60_000L))
    }
}
