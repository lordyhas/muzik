package com.lordyhas.muzik.data.model

/** What can be pinned to Home. */
enum class PinType { ALBUM, PLAYLIST }

/**
 * An album or playlist pinned to Home, by id (MediaStore album id, or playlist id). Pins are
 * kept newest first and saved as one `type:id` line each.
 */
data class Pin(val type: PinType, val id: String) {

    fun encode(): String = "${type.name.lowercase()}:$id"

    companion object {
        fun album(albumId: Long) = Pin(PinType.ALBUM, albumId.toString())
        fun playlist(playlistId: String) = Pin(PinType.PLAYLIST, playlistId)

        fun decode(line: String): Pin? {
            val type = line.substringBefore(':', missingDelimiterValue = "")
            val id = line.substringAfter(':', missingDelimiterValue = "")
            if (id.isBlank()) return null
            return when (type) {
                "album" -> Pin(PinType.ALBUM, id)
                "playlist" -> Pin(PinType.PLAYLIST, id)
                else -> null
            }
        }
    }
}

/** Pins it if absent (at the front, newest first), unpins it otherwise. */
fun List<Pin>.toggled(pin: Pin): List<Pin> = if (pin in this) this - pin else listOf(pin) + this

fun List<Pin>.encodePins(): String = joinToString("\n") { it.encode() }

fun decodePins(saved: String?): List<Pin> =
    saved.orEmpty().lines().mapNotNull { Pin.decode(it.trim()) }.distinct()

/** A pin resolved against the library, ready to show on Home. */
sealed interface PinnedItem {
    val pin: Pin

    data class PinnedAlbum(val album: Album) : PinnedItem {
        override val pin get() = Pin.album(album.id)
    }

    data class PinnedPlaylist(val playlist: Playlist) : PinnedItem {
        override val pin get() = Pin.playlist(playlist.id)
    }
}

/**
 * Resolves [pins] in order against the library. Pins whose album or playlist no longer exists
 * (deleted, or hidden by the library filter) are skipped, so Home never shows an empty card.
 */
fun resolvePins(pins: List<Pin>, albums: List<Album>, playlists: List<Playlist>): List<PinnedItem> {
    val albumsById = albums.associateBy { it.id.toString() }
    val playlistsById = playlists.associateBy { it.id }
    return pins.mapNotNull { pin ->
        when (pin.type) {
            PinType.ALBUM -> albumsById[pin.id]?.let { PinnedItem.PinnedAlbum(it) }
            PinType.PLAYLIST -> playlistsById[pin.id]?.let { PinnedItem.PinnedPlaylist(it) }
        }
    }
}
