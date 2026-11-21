package com.lordyhas.muzik

import com.lordyhas.muzik.data.model.Album
import com.lordyhas.muzik.data.model.Pin
import com.lordyhas.muzik.data.model.PinnedItem
import com.lordyhas.muzik.data.model.Playlist
import com.lordyhas.muzik.data.model.decodePins
import com.lordyhas.muzik.data.model.encodePins
import com.lordyhas.muzik.data.model.resolvePins
import com.lordyhas.muzik.data.model.toggled
import org.junit.Assert.assertEquals
import org.junit.Test

class PinTest {

    private val albumA = Album(id = 1L, title = "A", artist = "X", albumArtUri = null, songCount = 1)
    private val albumB = Album(id = 2L, title = "B", artist = "X", albumArtUri = null, songCount = 1)
    private val playlist = Playlist(id = "p-1", name = "Road", songIds = emptyList(), createdAt = 0L)

    @Test
    fun toggle_pinsNewestFirst_andUnpins() {
        val pins = emptyList<Pin>()
            .toggled(Pin.album(1))
            .toggled(Pin.playlist("p-1"))
        assertEquals(listOf(Pin.playlist("p-1"), Pin.album(1)), pins)
        assertEquals(listOf(Pin.album(1)), pins.toggled(Pin.playlist("p-1")))
    }

    @Test
    fun encodeDecode_roundTrips_andSkipsBadLines() {
        val pins = listOf(Pin.playlist("p-1"), Pin.album(42))
        assertEquals(pins, decodePins(pins.encodePins()))
        assertEquals(listOf(Pin.album(7)), decodePins("album:7\nsong:3\nplaylist:\n\nalbum:7"))
        assertEquals(emptyList<Pin>(), decodePins(null))
    }

    @Test
    fun resolve_keepsOrder_andDropsMissing() {
        val pins = listOf(Pin.album(2), Pin.playlist("gone"), Pin.playlist("p-1"), Pin.album(9), Pin.album(1))
        val resolved = resolvePins(pins, listOf(albumA, albumB), listOf(playlist))
        assertEquals(
            listOf(PinnedItem.PinnedAlbum(albumB), PinnedItem.PinnedPlaylist(playlist), PinnedItem.PinnedAlbum(albumA)),
            resolved
        )
    }
}
