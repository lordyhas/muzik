package com.lordyhas.muzik.data.model

/** Totals shown in the Listening stats card at the bottom of Home. */
data class ListeningStats(
    /** Every play counted so far. */
    val plays: Int,
    /** Plays × track length: an estimate, since a play counts once the song starts. */
    val listenedMs: Long,
    /** Songs played at least once. */
    val explored: Int,
    /** Songs in the library, for "22 of 76". */
    val librarySize: Int,
    val topSong: Song?,
    val topSongPlays: Int
)

/**
 * Builds the stats from play counts by song id, counting only songs still in [library] (so
 * deleted or hidden songs don't inflate them). Null when nothing has been played yet.
 */
fun listeningStats(playCounts: Map<Long, Int>, library: List<Song>): ListeningStats? {
    val played = library.mapNotNull { song -> playCounts[song.id]?.takeIf { it > 0 }?.let { song to it } }
    if (played.isEmpty()) return null
    val top = played.maxWith(compareBy<Pair<Song, Int>> { it.second }.thenBy { -it.first.id })
    return ListeningStats(
        plays = played.sumOf { it.second },
        listenedMs = played.sumOf { (song, count) -> song.duration * count },
        explored = played.size,
        librarySize = library.size,
        topSong = top.first,
        topSongPlays = top.second
    )
}

/** "42 min", "3 h 05 min" */
fun formatListenedTime(ms: Long): String {
    val totalMinutes = ms / 60_000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "$hours h ${minutes.toString().padStart(2, '0')} min" else "$minutes min"
}
