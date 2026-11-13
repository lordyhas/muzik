package com.lordyhas.muzik

import android.net.FakeUri
import com.lordyhas.muzik.data.model.Song
import com.lordyhas.muzik.data.model.forVoiceSearch
import org.junit.Assert.assertEquals
import org.junit.Test

class VoiceSearchTest {

    private val library = listOf(
        song(1, "Intro", "Band", "First Album", track = 1),
        song(2, "Closing Song", "Band", "First Album", track = 3),
        song(3, "Middle", "Band", "First Album", track = 2),
        song(4, "Song for You", "Singer", "Solo"),
        song(5, "Other", "Songwriters", "Mixed")
    )

    @Test
    fun `blank query plays the whole library`() {
        assertEquals(library, library.forVoiceSearch("  "))
    }

    @Test
    fun `exact artist name plays that artist`() {
        assertEquals(listOf(1L, 2L, 3L), library.forVoiceSearch("band").map { it.id })
    }

    @Test
    fun `exact album name plays the album in track order`() {
        assertEquals(listOf(1L, 3L, 2L), library.forVoiceSearch("First Album").map { it.id })
    }

    @Test
    fun `partial query matches titles first then artists and albums`() {
        assertEquals(listOf(2L, 4L, 5L), library.forVoiceSearch("song").map { it.id })
    }

    @Test
    fun `exact title ranks ahead of partial title matches`() {
        val songs = listOf(song(6, "Intro Reprise", "Band", "Live")) + library
        assertEquals(listOf(1L, 6L), songs.forVoiceSearch("intro").map { it.id })
    }

    @Test
    fun `no match plays nothing`() {
        assertEquals(emptyList<Song>(), library.forVoiceSearch("nothing like this"))
    }

    private fun song(id: Long, title: String, artist: String, album: String, track: Int? = null) = Song(
        id = id,
        title = title,
        artist = artist,
        album = album,
        albumId = album.hashCode().toLong(),
        duration = 180_000L,
        contentUri = FakeUri("content://media/$id"),
        trackNumber = track
    )
}
