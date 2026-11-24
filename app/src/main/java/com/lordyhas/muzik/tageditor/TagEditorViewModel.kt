package com.lordyhas.muzik.tageditor

import android.app.Application
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import coil.imageLoader
import com.lordyhas.muzik.data.model.Album
import com.lordyhas.muzik.data.model.Song
import com.lordyhas.muzik.data.model.filteredBy
import com.lordyhas.muzik.data.model.groupIntoAlbums
import com.lordyhas.muzik.data.model.sortedForAlbum
import com.lordyhas.muzik.data.repository.LibraryFilterStore
import com.lordyhas.muzik.data.repository.MusicRepository
import com.lordyhas.muzik.data.tags.AudioTagStore
import com.lordyhas.muzik.data.tags.CoverArt
import com.lordyhas.muzik.data.tags.SongEdit
import com.lordyhas.muzik.data.tags.SongTags
import com.lordyhas.muzik.service.DeletedSongs
import com.lordyhas.muzik.ui.components.UiMessage
import com.lordyhas.muzik.ui.components.pluralize
import com.lordyhas.muzik.ui.theme.ArtworkColorExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/** An artist in the Tag editor: every song whose artist tag has this name. */
data class TagArtist(val name: String, val songs: List<Song>) {
    val albumCount: Int get() = songs.map { it.albumId }.distinct().size
}

/** What the library screen lists for the current search. */
data class TagLibrary(
    val albums: List<Album> = emptyList(),
    val artists: List<TagArtist> = emptyList(),
    val songs: List<Song> = emptyList()
)

/**
 * State for the Tag editor activity. It reads the same library as the player (same filter), and
 * writes tags straight into the audio files through [AudioTagStore].
 */
class TagEditorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MusicRepository(application)
    private val tagStore = AudioTagStore(application)

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _onlyMissing = MutableStateFlow(false)
    val onlyMissing: StateFlow<Boolean> = _onlyMissing.asStateFlow()

    private val _messages = MutableSharedFlow<UiMessage>(extraBufferCapacity = 4)
    val messages: SharedFlow<UiMessage> = _messages.asSharedFlow()

    val albums: StateFlow<List<Album>> = _songs.map { it.groupIntoAlbums() }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val missingCount: StateFlow<Int> = _songs.map { list -> list.count { it.needsTags() } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val library: StateFlow<TagLibrary> = combine(_songs, albums, _query, _onlyMissing) { songs, albums, query, onlyMissing ->
        val q = query.trim().lowercase()
        val visibleSongs = songs
            .filter { !onlyMissing || it.needsTags() }
            .filter { q.isEmpty() || it.matches(q) }
        val visibleIds = visibleSongs.mapTo(HashSet()) { it.id }
        TagLibrary(
            albums = albums.filter { album -> album.songs.any { it.id in visibleIds } || (q.isNotEmpty() && !onlyMissing && album.title.lowercase().contains(q)) },
            artists = visibleSongs.groupBy { it.artist }
                .map { (name, list) -> TagArtist(name, list.sortedBy { it.title.lowercase() }) }
                .sortedBy { it.name.lowercase() },
            songs = visibleSongs.sortedBy { it.title.lowercase() }
        )
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.Eagerly, TagLibrary())

    init {
        LibraryFilterStore.init(application)
        load()
    }

    fun load() {
        viewModelScope.launch {
            _isLoading.value = _songs.value.isEmpty()
            reload()
            _isLoading.value = false
        }
    }

    private suspend fun reload() {
        // The player already scans storage; reading MediaStore is enough here and much faster
        _songs.value = repository.getLocalSongs(scanStorage = false).filteredBy(LibraryFilterStore.filter.value)
    }

    fun setQuery(query: String) { _query.value = query }
    fun setOnlyMissing(only: Boolean) { _onlyMissing.value = only }

    fun song(id: Long): Song? = _songs.value.firstOrNull { it.id == id }
    fun album(id: Long): Album? = albums.value.firstOrNull { it.id == id }
    /** The artist's songs, album by album, each album in track order. */
    fun artistSongs(name: String): List<Song> =
        _songs.value.filter { it.artist == name }
            .groupBy { it.albumId }.values
            .sortedBy { it.first().album.lowercase() }
            .flatMap { it.sortedForAlbum() }

    suspend fun readTags(song: Song): SongTags? = tagStore.readTags(song)
    suspend fun readCover(song: Song): CoverArt? = tagStore.readCover(song)

    /** Writes [edits] (write access must already be granted), then reloads the library. */
    fun save(edits: List<SongEdit>, onDone: (success: Boolean) -> Unit) {
        val toWrite = edits.filterNot { it.isEmpty }
        if (toWrite.isEmpty()) return onDone(true)
        viewModelScope.launch {
            _isSaving.value = true
            val failed = tagStore.write(toWrite)
            if (toWrite.any { it.cover != null }) clearArtworkCaches()
            reload()
            _isSaving.value = false
            val saved = toWrite.size - failed.size
            _messages.tryEmit(
                UiMessage(
                    when {
                        failed.isEmpty() -> if (saved == 1) "Tags saved" else "Tags saved for " + pluralize(saved, "song")
                        saved == 0 -> "Couldn't save the tags"
                        else -> "Saved ${pluralize(saved, "song")}, ${failed.size} couldn't be changed"
                    }
                )
            )
            onDone(failed.isEmpty())
        }
    }

    /**
     * Removes [song] from the device once deletion was approved (see WriteAccess.requestDelete).
     * Android 11+ has already deleted the file by then; older versions delete it here.
     */
    fun delete(song: Song, onDone: (success: Boolean) -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                withContext(Dispatchers.IO) {
                    val resolver = getApplication<Application>().contentResolver
                    runCatching { resolver.delete(song.contentUri, null, null) }
                    if (song.folderPath.isNotEmpty() && song.fileName.isNotEmpty()) {
                        runCatching { File(song.folderPath, song.fileName).takeIf { it.exists() }?.delete() }
                    }
                }
            }
            reload()
            _isSaving.value = false
            val deleted = song(song.id) == null
            if (deleted) DeletedSongs.announce(setOf(song.id))
            _messages.tryEmit(UiMessage(if (deleted) "Song deleted" else "Couldn't delete the song"))
            onDone(deleted)
        }
    }

    fun showMessage(text: String) { _messages.tryEmit(UiMessage(text)) }

    /** Covers are cached by URI, which doesn't change when the picture inside the file does. */
    private fun clearArtworkCaches() {
        val loader = getApplication<Application>().imageLoader
        loader.memoryCache?.clear()
        loader.diskCache?.clear()
        ArtworkColorExtractor.clear()
    }
}

private fun Song.matches(q: String): Boolean =
    title.lowercase().contains(q) || artist.lowercase().contains(q) || album.lowercase().contains(q) ||
        fileName.lowercase().contains(q)

/**
 * Whether the song looks untagged: MediaStore falls back to "<unknown>" for a missing artist
 * (shown as "Unknown artist" by [MusicRepository]), the file name for a missing title, and the
 * folder name for a missing album.
 */
fun Song.needsTags(): Boolean {
    val folderName = folderPath.substringAfterLast('/')
    return artist.isBlank() || artist == MediaStore.UNKNOWN_STRING || artist == "Unknown artist" ||
        title == fileName.substringBeforeLast('.') ||
        album.isBlank() || album == MediaStore.UNKNOWN_STRING || (folderName.isNotEmpty() && album == folderName)
}
