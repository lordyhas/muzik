package com.lordyhas.muzik.data.model

/**
 * Songs to play for a voice request such as "play <query> on Muzik" (Android Auto, Assistant).
 *
 * A blank query plays the whole library. An artist or album named exactly like the query plays
 * in full (albums in track order); otherwise songs whose title, artist or album contains the
 * query play, title matches first.
 */
fun List<Song>.forVoiceSearch(query: String): List<Song> {
    val q = query.trim()
    if (q.isEmpty()) return this

    filter { it.artist.equals(q, ignoreCase = true) }.takeIf { it.isNotEmpty() }?.let { return it }
    filter { it.album.equals(q, ignoreCase = true) }.takeIf { it.isNotEmpty() }?.let { album ->
        return album.sortedWith(compareBy({ it.discNumber ?: 0 }, { it.trackNumber ?: Int.MAX_VALUE }))
    }

    val byTitle = filter { it.title.contains(q, ignoreCase = true) }
        .sortedByDescending { it.title.equals(q, ignoreCase = true) }
    val byArtist = filter { it.artist.contains(q, ignoreCase = true) }
    val byAlbum = filter { it.album.contains(q, ignoreCase = true) }
    return (byTitle + byArtist + byAlbum).distinctBy { it.id }
}
