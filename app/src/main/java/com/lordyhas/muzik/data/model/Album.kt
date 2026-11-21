package com.lordyhas.muzik.data.model

import android.net.Uri

data class Album(
    val id: Long,
    val title: String,
    val artist: String,
    val albumArtUri: Uri?,
    val songCount: Int,
    val songs: List<Song> = emptyList(),
    /** Most common release year among the tracks, when the tags have one. */
    val year: Int? = null
)

/**
 * Groups songs into albums by MediaStore album id rather than by title, so two different albums
 * that share a name (e.g. two artists' "Greatest Hits") stay separate.
 */
fun List<Song>.groupIntoAlbums(): List<Album> =
    groupBy { it.albumId }
        .map { (albumId, albumSongs) ->
            val sorted = albumSongs.sortedForAlbum()
            Album(
                id = albumId,
                title = sorted.first().album,
                // Most frequent track artist, so a single "feat." track doesn't name the album.
                artist = albumSongs.groupingBy { it.artist }.eachCount().maxByOrNull { it.value }!!.key,
                albumArtUri = sorted.firstOrNull { it.albumArtUri != null }?.albumArtUri,
                songCount = sorted.size,
                songs = sorted,
                year = albumSongs.mapNotNull { it.year }.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key
            )
        }
        .sortedWith(compareBy<Album> { it.title.lowercase() }.thenBy { it.artist.lowercase() })

fun List<Song>.sortedForAlbum(): List<Song> {
    return sortedWith(
        compareBy<Song> { it.discNumber ?: 1 }
            .thenBy { if (it.trackNumber != null && it.trackNumber > 0) 0 else 1 }
            .thenBy { it.trackNumber ?: Int.MAX_VALUE }
            .thenBy { it.title.lowercase() }
    )
}

