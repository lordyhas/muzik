package com.lordyhas.muzik.service

import android.content.Context
import android.os.SystemClock
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession.MediaItemsWithStartPosition
import com.lordyhas.muzik.data.model.Album
import com.lordyhas.muzik.data.model.Song
import com.lordyhas.muzik.data.model.filteredBy
import com.lordyhas.muzik.data.model.forVoiceSearch
import com.lordyhas.muzik.data.model.groupIntoAlbums
import com.lordyhas.muzik.data.repository.LibraryFilterStore
import com.lordyhas.muzik.data.repository.LibraryRepository
import com.lordyhas.muzik.data.repository.MusicRepository
import com.lordyhas.muzik.data.repository.PlaylistRepository
import kotlinx.coroutines.flow.first

/**
 * Browse tree exposed to Android Auto and other media browsers:
 *
 * root
 * ├── Recently played
 * ├── Liked Songs
 * ├── Playlists → playlist → songs
 * ├── Albums → album → songs
 * ├── Artists → artist → songs
 * └── All songs
 *
 * Browsable song items carry their parent in the id ("song|<id>|<parentId>") so that picking one
 * plays the whole album/playlist from that song. Queue items always use the plain song id as
 * mediaId, which the rest of the app relies on.
 */
@UnstableApi
class MediaLibraryTree(private val context: Context) {

    private val musicRepository = MusicRepository(context)
    private val playlistRepository = PlaylistRepository(context)
    private val libraryRepository = LibraryRepository(context)

    private data class Snapshot(
        val songs: List<Song>,
        val songsById: Map<Long, Song>,
        val albums: List<Album>,
        val artists: Map<String, List<Song>>,
        val playlists: List<Pair<String, String>>, // id to name
        val playlistSongs: Map<String, List<Long>>,
        val liked: List<Long>,
        val recent: List<Long>,
        val loadedAt: Long
    )

    @Volatile
    private var cached: Snapshot? = null

    private suspend fun snapshot(): Snapshot {
        cached?.takeIf { SystemClock.elapsedRealtime() - it.loadedAt < CACHE_MS }?.let { return it }
        val songs = musicRepository.getLocalSongs(scanStorage = false).filteredBy(LibraryFilterStore.filter.value)
        val playlistRows = playlistRepository.getAllPlaylistSongs().first()
        val snapshot = Snapshot(
            songs = songs,
            songsById = songs.associateBy { it.id },
            albums = songs.groupIntoAlbums(),
            artists = songs.groupBy { it.artist }.toSortedMap(String.CASE_INSENSITIVE_ORDER),
            playlists = playlistRepository.getAllPlaylists().first().map { it.id to it.name },
            playlistSongs = playlistRows.groupBy { it.playlistId }.mapValues { (_, rows) -> rows.map { it.songId } },
            liked = libraryRepository.getLikedSongs().first().map { it.songId },
            recent = libraryRepository.getPlayStats().first()
                .filter { it.lastPlayedAt > 0 }
                .sortedByDescending { it.lastPlayedAt }
                .take(50)
                .map { it.songId },
            loadedAt = SystemClock.elapsedRealtime()
        )
        cached = snapshot
        return snapshot
    }

    fun root(): MediaItem = folder(ROOT, "Muzik", MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)

    suspend fun children(parentId: String): List<MediaItem> {
        val s = snapshot()
        return when {
            parentId == ROOT -> listOf(
                folder(RECENT, "Recently played", MediaMetadata.MEDIA_TYPE_PLAYLIST, browsable = false),
                folder(LIKED, "Liked Songs", MediaMetadata.MEDIA_TYPE_PLAYLIST, browsable = false),
                folder(PLAYLISTS, "Playlists", MediaMetadata.MEDIA_TYPE_FOLDER_PLAYLISTS),
                folder(ALBUMS, "Albums", MediaMetadata.MEDIA_TYPE_FOLDER_ALBUMS),
                folder(ARTISTS, "Artists", MediaMetadata.MEDIA_TYPE_FOLDER_ARTISTS),
                folder(SONGS, "All songs", MediaMetadata.MEDIA_TYPE_PLAYLIST, browsable = false)
            ).map { it.asBrowsable() }
            parentId == PLAYLISTS -> s.playlists.map { (id, name) ->
                folder("$PLAYLIST_PREFIX$id", name, MediaMetadata.MEDIA_TYPE_PLAYLIST)
            }
            parentId == ALBUMS -> s.albums.map { album ->
                folder("$ALBUM_PREFIX${album.id}", album.title, MediaMetadata.MEDIA_TYPE_ALBUM, subtitle = album.artist,
                    artwork = album.albumArtUri)
            }
            parentId == ARTISTS -> s.artists.map { (name, _) ->
                folder("$ARTIST_PREFIX$name", name, MediaMetadata.MEDIA_TYPE_ARTIST)
            }
            else -> songsOf(parentId, s).map { browsableSong(it, parentId) }
        }
    }

    suspend fun item(mediaId: String): MediaItem? {
        if (mediaId == ROOT) return root()
        parseSongId(mediaId)?.let { id -> return snapshot().songsById[id]?.let { browsableSong(it, parentOf(mediaId)) } }
        return children(ROOT).firstOrNull { it.mediaId == mediaId }
    }

    /** Turns items sent by external controllers (no URI) into playable queue items. */
    suspend fun resolve(items: List<MediaItem>): List<MediaItem> {
        if (items.all { it.localConfiguration != null }) return items
        val s = snapshot()
        return items.flatMap { item ->
            val query = item.requestMetadata.searchQuery
            when {
                item.localConfiguration != null -> listOf(item)
                query != null -> s.songs.forVoiceSearch(query).map { it.toMediaItem() }
                else -> listOfNotNull(
                    parseSongId(item.mediaId)?.let { s.songsById[it]?.toMediaItem() }
                        ?: songsOf(item.mediaId, s).firstOrNull()?.toMediaItem()
                )
            }
        }
    }

    /**
     * Picking one song from a browsed list plays the whole list from that song;
     * picking a playable folder (e.g. Liked Songs) plays it from the start;
     * a voice search ("play <query>") plays the matching songs from the first one.
     */
    suspend fun expand(items: List<MediaItem>, startIndex: Int, startPositionMs: Long): MediaItemsWithStartPosition {
        val s = snapshot()
        if (items.size == 1 && items[0].localConfiguration == null) {
            items[0].requestMetadata.searchQuery?.let { query ->
                val matches = s.songs.forVoiceSearch(query)
                return MediaItemsWithStartPosition(matches.map { it.toMediaItem() }, 0, C.TIME_UNSET)
            }
            val mediaId = items[0].mediaId
            val songId = parseSongId(mediaId)
            val parent = if (songId != null) parentOf(mediaId) else mediaId
            val context = songsOf(parent, s)
            if (context.isNotEmpty()) {
                val index = songId?.let { id -> context.indexOfFirst { it.id == id } }?.takeIf { it >= 0 } ?: 0
                return MediaItemsWithStartPosition(context.map { it.toMediaItem() }, index, startPositionMs)
            }
        }
        return MediaItemsWithStartPosition(resolve(items), startIndex, startPositionMs)
    }

    /** Last saved session, for the system's "resume playback" (e.g. Bluetooth play button). */
    suspend fun resumption(): MediaItemsWithStartPosition? {
        val saved = PlaybackStateStore.load(context) ?: return null
        val s = snapshot()
        val songs = saved.songIds.mapNotNull { s.songsById[it] }
        if (songs.isEmpty()) return null
        val currentId = saved.songIds.getOrNull(saved.index)
        val index = songs.indexOfFirst { it.id == currentId }
        return MediaItemsWithStartPosition(
            songs.map { it.toMediaItem() },
            index.coerceAtLeast(0),
            if (index >= 0) saved.positionMs else 0L
        )
    }

    fun invalidate() {
        cached = null
    }

    // ─── Helpers ────────────────────────────────────────────────────────────

    private fun songsOf(parentId: String, s: Snapshot): List<Song> = when {
        parentId == RECENT -> s.recent.mapNotNull { s.songsById[it] }
        parentId == LIKED -> s.liked.mapNotNull { s.songsById[it] }
        parentId == SONGS -> s.songs
        parentId.startsWith(PLAYLIST_PREFIX) ->
            s.playlistSongs[parentId.removePrefix(PLAYLIST_PREFIX)].orEmpty().mapNotNull { s.songsById[it] }
        parentId.startsWith(ALBUM_PREFIX) -> {
            val albumId = parentId.removePrefix(ALBUM_PREFIX).toLongOrNull()
            s.albums.firstOrNull { it.id == albumId }?.songs.orEmpty()
        }
        parentId.startsWith(ARTIST_PREFIX) -> s.artists[parentId.removePrefix(ARTIST_PREFIX)].orEmpty()
        else -> emptyList()
    }

    private fun folder(
        id: String,
        title: String,
        mediaType: Int,
        browsable: Boolean = true,
        subtitle: String? = null,
        artwork: android.net.Uri? = null
    ): MediaItem = MediaItem.Builder()
        .setMediaId(id)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setArtworkUri(artwork)
                .setIsBrowsable(browsable)
                .setIsPlayable(!browsable)
                .setMediaType(mediaType)
                .build()
        )
        .build()

    /** Root shortcuts like "Liked Songs" are both browsable (to list) and playable (to play all). */
    private fun MediaItem.asBrowsable(): MediaItem = buildUpon()
        .setMediaMetadata(mediaMetadata.buildUpon().setIsBrowsable(true).build())
        .build()

    private fun browsableSong(song: Song, parentId: String): MediaItem {
        val item = song.toMediaItem()
        return item.buildUpon()
            .setMediaId("$SONG_PREFIX${song.id}|$parentId")
            .setMediaMetadata(
                item.mediaMetadata.buildUpon()
                    .setIsBrowsable(false)
                    .setIsPlayable(true)
                    .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                    .build()
            )
            .build()
    }

    private fun parseSongId(mediaId: String): Long? =
        if (mediaId.startsWith(SONG_PREFIX)) mediaId.removePrefix(SONG_PREFIX).substringBefore('|').toLongOrNull()
        else mediaId.toLongOrNull()

    private fun parentOf(mediaId: String): String =
        if (mediaId.startsWith(SONG_PREFIX)) mediaId.removePrefix(SONG_PREFIX).substringAfter('|', SONGS) else SONGS

    companion object {
        const val ROOT = "root"
        private const val RECENT = "recent"
        private const val LIKED = "liked"
        private const val PLAYLISTS = "playlists"
        private const val ALBUMS = "albums"
        private const val ARTISTS = "artists"
        private const val SONGS = "songs"
        private const val PLAYLIST_PREFIX = "playlist|"
        private const val ALBUM_PREFIX = "album|"
        private const val ARTIST_PREFIX = "artist|"
        private const val SONG_PREFIX = "song|"
        private const val CACHE_MS = 30_000L
    }
}
