package com.lordyhas.muzik.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.lordyhas.muzik.data.AppVersion
import com.lordyhas.muzik.data.local.PlaylistEntity
import com.lordyhas.muzik.data.readAppVersion
import com.lordyhas.muzik.data.model.ReleaseNotes
import com.lordyhas.muzik.data.repository.WhatsNewRepository
import com.lordyhas.muzik.ui.components.pluralize
import com.lordyhas.muzik.data.model.Album
import com.lordyhas.muzik.data.model.AlbumSort
import com.lordyhas.muzik.data.model.ArtistSort
import com.lordyhas.muzik.data.model.Folder
import com.lordyhas.muzik.data.model.SearchResults
import com.lordyhas.muzik.data.model.SmartPlaylist
import com.lordyhas.muzik.data.model.SmartPlaylistType
import com.lordyhas.muzik.data.model.SongSort
import com.lordyhas.muzik.data.model.Artist
import com.lordyhas.muzik.data.model.Playlist
import com.lordyhas.muzik.data.model.Song
import com.lordyhas.muzik.data.model.LibraryFilter
import com.lordyhas.muzik.data.model.filteredBy
import com.lordyhas.muzik.data.model.forVoiceSearch
import com.lordyhas.muzik.data.model.groupIntoAlbums
import com.lordyhas.muzik.data.model.groupIntoFolders
import com.lordyhas.muzik.data.model.mergeEditedOrder
import com.lordyhas.muzik.data.repository.LibraryFilterStore
import com.lordyhas.muzik.data.repository.LibraryRepository
import com.lordyhas.muzik.data.repository.LyricsRepository
import com.lordyhas.muzik.data.repository.LyricsState
import com.lordyhas.muzik.data.repository.MusicRepository
import com.lordyhas.muzik.data.repository.PlaylistRepository
import com.lordyhas.muzik.service.EqualizerInfo
import com.lordyhas.muzik.service.EqualizerSettings
import com.lordyhas.muzik.service.MusicController
import com.lordyhas.muzik.service.PlaybackSettings
import com.lordyhas.muzik.service.SleepTimer
import com.lordyhas.muzik.ui.components.UiMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@UnstableApi
class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("muzik_prefs", Context.MODE_PRIVATE)
    private val repository = MusicRepository(application)
    private val playlistRepository = PlaylistRepository(application)
    val musicController = MusicController(application)

    init {
        PlaybackSettings.init(application)
        LibraryFilterStore.init(application)
    }

    // Sleep timer / speed / equalizer live in the playback service; the UI only reads & requests changes
    val sleepTimer: StateFlow<SleepTimer> = PlaybackSettings.sleepTimer
    val playbackSpeed: StateFlow<Float> = PlaybackSettings.speed
    val equalizer: StateFlow<EqualizerSettings> = PlaybackSettings.equalizer
    val equalizerInfo: StateFlow<EqualizerInfo?> = PlaybackSettings.equalizerInfo

    /** Everything MediaStore returned; [_songs] is this minus what [libraryFilter] excludes. */
    private val _allSongs = MutableStateFlow<List<Song>>(emptyList())
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    val libraryFilter: StateFlow<LibraryFilter> = LibraryFilterStore.filter

    init {
        viewModelScope.launch {
            LibraryFilterStore.filter.collect { filter -> _songs.value = _allSongs.value.filteredBy(filter) }
        }
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /** A pull-to-refresh or "Rescan library" in progress; unlike [isLoading] the list stays visible. */
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val appVersion: AppVersion = application.readAppVersion()

    private val whatsNewRepository = WhatsNewRepository(application)

    /** Every release's notes, newest first (About > What's new). */
    private val _releaseNotes = MutableStateFlow<List<ReleaseNotes>>(emptyList())
    val releaseNotes: StateFlow<List<ReleaseNotes>> = _releaseNotes.asStateFlow()

    /** Notes to show once after an update; empty once dismissed. */
    private val _unseenReleaseNotes = MutableStateFlow<List<ReleaseNotes>>(emptyList())
    val unseenReleaseNotes: StateFlow<List<ReleaseNotes>> = _unseenReleaseNotes.asStateFlow()

    init {
        viewModelScope.launch {
            val all = whatsNewRepository.allReleaseNotes()
            _releaseNotes.value = all
            _unseenReleaseNotes.value = whatsNewRepository.unseen(all, appVersion)
        }
    }

    fun dismissWhatsNew() {
        _unseenReleaseNotes.value = emptyList()
        whatsNewRepository.markSeen(appVersion)
    }

    private val _isPermissionGranted = MutableStateFlow(false)
    val isPermissionGranted: StateFlow<Boolean> = _isPermissionGranted.asStateFlow()

    private val _showNowPlaying = MutableStateFlow(false)
    val showNowPlaying: StateFlow<Boolean> = _showNowPlaying.asStateFlow()

    private val _showQueue = MutableStateFlow(false)
    val showQueue: StateFlow<Boolean> = _showQueue.asStateFlow()

    private val _selectedSongForDetails = MutableStateFlow<Song?>(null)
    val selectedSongForDetails: StateFlow<Song?> = _selectedSongForDetails.asStateFlow()

    val currentQueue: StateFlow<List<Song>> = musicController.currentQueue
    val currentQueueIndex: StateFlow<Int> = musicController.currentMediaItemIndex
    val userQueueCount: StateFlow<Int> = musicController.userQueueCount
    val userQueue: StateFlow<List<Song>> = musicController.userQueueList
    val contextQueue: StateFlow<List<Song>> = musicController.contextQueueList

    private val libraryRepository = LibraryRepository(application)

    // Listening history is recorded by the playback service into Room
    private val playStats = libraryRepository.getPlayStats()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val likedEntities = libraryRepository.getLikedSongs()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val likedSongIds: StateFlow<Set<Long>> = likedEntities
        .map { list -> list.map { it.songId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    // Detail sheets
    private val _selectedAlbum = MutableStateFlow<Album?>(null)
    val selectedAlbum: StateFlow<Album?> = _selectedAlbum.asStateFlow()

    private val _selectedArtist = MutableStateFlow<Artist?>(null)
    val selectedArtist: StateFlow<Artist?> = _selectedArtist.asStateFlow()

    // ─── Playlists (Room-backed) ──────────────────────────────────────────────

    /**
     * Live Flow from Room: all playlists enriched with resolved Songs.
     * Room automatically re-emits whenever the DB changes — no manual refresh needed.
     */
    val playlists: StateFlow<List<Playlist>> = combine(
        playlistRepository.getAllPlaylists(),    // Flow<List<PlaylistEntity>>
        playlistRepository.getAllPlaylistSongs(), // Flow<List<PlaylistSongRow>>
        _songs                                   // StateFlow<List<Song>>
    ) { entities, songRows, songList ->
        val songMap = songList.associateBy { it.id }
        // Build a map: playlistId → ordered list of songIds
        val songIdsByPlaylist: Map<String, List<Long>> = songRows
            .groupBy { it.playlistId }
            .mapValues { (_, rows) -> rows.map { it.songId } }

        entities.map { entity ->
            val songIds = songIdsByPlaylist[entity.id] ?: emptyList()
            Playlist(
                id = entity.id,
                name = entity.name,
                songIds = songIds,
                createdAt = entity.createdAt,
                songs = songIds.mapNotNull { songMap[it] }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /** Playlist currently opened in detail sheet (kept in sync with Room via playlists flow). */
    private val _selectedPlaylistId = MutableStateFlow<String?>(null)

    val selectedPlaylist: StateFlow<Playlist?> = combine(_selectedPlaylistId, playlists) { id, list ->
        id?.let { list.find { p -> p.id == it } }
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    /** Controls Create-playlist dialog visibility. */
    private val _showCreatePlaylistDialog = MutableStateFlow(false)
    val showCreatePlaylistDialog: StateFlow<Boolean> = _showCreatePlaylistDialog.asStateFlow()

    /** Song waiting to be added to a playlist (triggers AddToPlaylistSheet). */
    private val _songPendingAddToPlaylist = MutableStateFlow<Song?>(null)
    val songPendingAddToPlaylist: StateFlow<Song?> = _songPendingAddToPlaylist.asStateFlow()


    // ─────────────────────────────────────────────────────────────────────────

    // ─── Sorting (persisted) ──────────────────────────────────────────────────

    private val _songSort = MutableStateFlow(loadEnum("song_sort", SongSort.TITLE))
    val songSort: StateFlow<SongSort> = _songSort.asStateFlow()

    private val _albumSort = MutableStateFlow(loadEnum("album_sort", AlbumSort.TITLE))
    val albumSort: StateFlow<AlbumSort> = _albumSort.asStateFlow()

    private val _artistSort = MutableStateFlow(loadEnum("artist_sort", ArtistSort.NAME))
    val artistSort: StateFlow<ArtistSort> = _artistSort.asStateFlow()

    fun setSongSort(sort: SongSort) { _songSort.value = sort; saveEnum("song_sort", sort) }
    fun setAlbumSort(sort: AlbumSort) { _albumSort.value = sort; saveEnum("album_sort", sort) }
    fun setArtistSort(sort: ArtistSort) { _artistSort.value = sort; saveEnum("artist_sort", sort) }

    private inline fun <reified T : Enum<T>> loadEnum(key: String, default: T): T =
        prefs.getString(key, null)?.let { name -> enumValues<T>().firstOrNull { it.name == name } } ?: default

    private fun saveEnum(key: String, value: Enum<*>) {
        prefs.edit().putString(key, value.name).apply()
    }

    val filteredSongs: StateFlow<List<Song>> = combine(_songs, _searchQuery, _songSort) { songList, query, sort ->
        val matching = if (query.isBlank()) {
            songList
        } else {
            val lowerQuery = query.trim().lowercase()
            songList.filter {
                it.title.lowercase().contains(lowerQuery) ||
                it.artist.lowercase().contains(lowerQuery) ||
                it.album.lowercase().contains(lowerQuery)
            }
        }
        sort.apply(matching)
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val albums: StateFlow<List<Album>> = _songs.map { songList ->
        songList.groupIntoAlbums()
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val artists: StateFlow<List<Artist>> = _songs.map { songList ->
        songList.groupBy { it.artist }
            .map { (artistName, artistSongs) ->
                val albumCount = artistSongs.map { it.albumId }.distinct().size
                Artist(
                    name = artistName,
                    songCount = artistSongs.size,
                    albumCount = albumCount,
                    songs = artistSongs,
                    artworkUri = artistSongs.firstOrNull { it.albumArtUri != null }?.albumArtUri
                )
            }
            .sortedBy { it.name.lowercase() }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val sortedAlbums: StateFlow<List<Album>> = combine(albums, _albumSort) { list, sort -> sort.apply(list) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val sortedArtists: StateFlow<List<Artist>> = combine(artists, _artistSort) { list, sort -> sort.apply(list) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /** Folders that directly contain music, A–Z. */
    val folders: StateFlow<List<Folder>> = _songs.map { songList ->
        songList.groupIntoFolders()
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /** Every folder, hidden ones included (flagged), for the folder manager. */
    val allFolders: StateFlow<List<Folder>> = combine(_allSongs, LibraryFilterStore.filter) { all, filter ->
        // Count what would show if the folder were visible (short clips/recordings stay excluded)
        all.filteredBy(filter.copy(hiddenFolders = emptySet())).groupIntoFolders(filter::hidingFolderOf)
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /** How many device audio files the library filter currently keeps out. */
    val hiddenSongCount: StateFlow<Int> = combine(_allSongs, _songs) { all, visible -> all.size - visible.size }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    fun setFolderHidden(path: String, hidden: Boolean) = LibraryFilterStore.setFolderHidden(path, hidden)
    fun setHideRecordings(hide: Boolean) = LibraryFilterStore.setHideRecordings(hide)
    fun setMinDuration(ms: Long) = LibraryFilterStore.setMinDuration(ms)

    // ─── Global search ────────────────────────────────────────────────────────

    private val _globalQuery = MutableStateFlow("")
    val globalQuery: StateFlow<String> = _globalQuery.asStateFlow()

    fun updateGlobalQuery(query: String) { _globalQuery.value = query }

    @OptIn(FlowPreview::class)
    val searchResults: StateFlow<SearchResults> = combine(
        _globalQuery.debounce(150),
        _songs,
        albums,
        artists,
        playlists
    ) { query, songList, albumList, artistList, playlistList ->
        val q = query.trim().lowercase()
        if (q.isEmpty()) {
            SearchResults()
        } else {
            SearchResults(
                songs = songList.filter {
                    it.title.lowercase().contains(q) || it.artist.lowercase().contains(q) || it.album.lowercase().contains(q)
                },
                albums = albumList.filter { it.title.lowercase().contains(q) || it.artist.lowercase().contains(q) },
                artists = artistList.filter { it.name.lowercase().contains(q) },
                playlists = playlistList.filter { it.name.lowercase().contains(q) }
            )
        }
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.Lazily, SearchResults())

    val recentlyPlayedSongs: StateFlow<List<Song>> = combine(_songs, playStats) { songList, stats ->
        val songMap = songList.associateBy { it.id }
        stats.filter { it.lastPlayedAt > 0 }
            .sortedByDescending { it.lastPlayedAt }
            .mapNotNull { songMap[it.songId] }
            .take(30)
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /** Liked Songs, Most Played, Recently Added and Rediscover, derived live from Room + MediaStore. */
    val smartPlaylists: StateFlow<List<SmartPlaylist>> = combine(_songs, playStats, likedEntities) { songList, stats, liked ->
        val songMap = songList.associateBy { it.id }
        val monthAgo = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
        listOf(
            SmartPlaylist(SmartPlaylistType.LIKED, liked.mapNotNull { songMap[it.songId] }),
            SmartPlaylist(
                SmartPlaylistType.MOST_PLAYED,
                stats.filter { it.playCount > 0 }.sortedByDescending { it.playCount }.mapNotNull { songMap[it.songId] }.take(50)
            ),
            SmartPlaylist(
                SmartPlaylistType.RECENTLY_ADDED,
                songList.filter { it.dateAdded > 0 }.sortedByDescending { it.dateAdded }.take(50)
            ),
            SmartPlaylist(
                SmartPlaylistType.REDISCOVER,
                stats.filter { it.playCount > 0 && it.lastPlayedAt in 1 until monthAgo }
                    .sortedByDescending { it.playCount }
                    .mapNotNull { songMap[it.songId] }
                    .take(50)
            )
        )
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun toggleLike(song: Song) {
        val liked = song.id in likedSongIds.value
        viewModelScope.launch { libraryRepository.setLiked(song.id, !liked) }
    }

    init {
        importLegacyRecentHistory()

        // Once both the library and the player connection are ready, restore the last session
        viewModelScope.launch {
            combine(_songs, musicController.isControllerReady) { songs, ready -> songs.takeIf { ready } }
                .filterNotNull()
                .first { it.isNotEmpty() }
                .let { musicController.restoreOrSync(it) }
        }
    }

    val recentlyPlayedAlbums: StateFlow<List<Album>> = combine(albums, recentlyPlayedSongs) { allAlbums, recentSongs ->
        val recentAlbumIds = recentSongs.map { it.albumId }.distinct()
        val albumMap = allAlbums.associateBy { it.id }
        val recents = recentAlbumIds.mapNotNull { albumMap[it] }
        if (recents.isNotEmpty()) recents else allAlbums.take(6)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val recentlyPlayedArtists: StateFlow<List<Artist>> = combine(artists, recentlyPlayedSongs) { allArtists, recentSongs ->
        val recentArtistNames = recentSongs.map { it.artist }.distinct()
        val artistMap = allArtists.associateBy { it.name }
        val recents = recentArtistNames.mapNotNull { artistMap[it] }
        if (recents.isNotEmpty()) recents else allArtists.take(8)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val currentSong: StateFlow<Song?> = combine(_songs, musicController.currentMediaItem) { songList, mediaItem ->
        if (mediaItem == null) null
        else {
            val mediaId = mediaItem.mediaId
            songList.find { it.id.toString() == mediaId } ?: Song(
                id = mediaId.toLongOrNull() ?: 0L,
                title = mediaItem.mediaMetadata.title?.toString() ?: "Unknown title",
                artist = mediaItem.mediaMetadata.artist?.toString() ?: "Unknown artist",
                album = mediaItem.mediaMetadata.albumTitle?.toString() ?: "Unknown album",
                albumId = 0L,
                duration = 0L,
                contentUri = android.net.Uri.EMPTY,
                albumArtUri = mediaItem.mediaMetadata.artworkUri
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val _playingPlaylistId = MutableStateFlow<String?>(null)

    /** The playlist the queue was started from, as long as the current song still belongs to it. */
    val playingPlaylistId: StateFlow<String?> = combine(_playingPlaylistId, currentSong, playlists) { id, song, lists ->
        id?.takeIf { song != null && lists.firstOrNull { it.id == id }?.songIds?.contains(song.id) == true }
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    val isPlaying = musicController.isPlaying
    val currentPosition = musicController.currentPosition
    val duration = musicController.duration
    val isShuffle = musicController.isShuffle
    val repeatMode = musicController.repeatMode

    // ─── Lyrics (loaded only while the lyrics panel is open) ──────────────────

    private val lyricsRepository = LyricsRepository(application)
    private val _showLyrics = MutableStateFlow(false)
    val showLyrics: StateFlow<Boolean> = _showLyrics.asStateFlow()

    fun toggleLyrics() { _showLyrics.value = !_showLyrics.value }

    @OptIn(ExperimentalCoroutinesApi::class)
    val lyrics: StateFlow<LyricsState> = combine(currentSong, _showLyrics) { song, show -> song.takeIf { show } }
        .distinctUntilChanged { old, new -> old?.id == new?.id }
        .flatMapLatest { song ->
            if (song == null) {
                flowOf<LyricsState>(LyricsState.Hidden)
            } else {
                flow<LyricsState> {
                    emit(LyricsState.Loading)
                    emit(lyricsRepository.load(song)?.let { LyricsState.Loaded(it) } ?: LyricsState.NotFound)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LyricsState.Hidden)

    fun setPermissionGranted(granted: Boolean) {
        _isPermissionGranted.value = granted
        if (granted) loadSongs()
    }

    fun loadSongs() {
        viewModelScope.launch {
            _isLoading.value = true
            reloadSongs()
            _isLoading.value = false
        }
    }

    /**
     * Rescans the device while keeping the current list on screen (pull-to-refresh, Settings).
     * With [announce], confirms the result with a message.
     */
    fun refreshLibrary(announce: Boolean = false) {
        if (_isRefreshing.value || _isLoading.value) return
        viewModelScope.launch {
            _isRefreshing.value = true
            reloadSongs()
            _isRefreshing.value = false
            if (announce) showMessage("Library updated · " + pluralize(_songs.value.size, "song"))
        }
    }

    private suspend fun reloadSongs() {
        val loaded = repository.getLocalSongs()
        _allSongs.value = loaded
        _songs.value = loaded.filteredBy(LibraryFilterStore.filter.value)
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun playSong(song: Song, playlist: List<Song>? = null) {
        _playingPlaylistId.value = null
        val currentPlaylist = playlist ?: filteredSongs.value.ifEmpty { _songs.value }
        val index = currentPlaylist.indexOfFirst { it.id == song.id }
        if (index != -1) {
            musicController.playSongList(currentPlaylist, index)
        } else {
            musicController.playSongList(listOf(song), 0)
        }
    }

    fun playAlbum(album: Album, startSong: Song? = null) {
        if (album.songs.isNotEmpty()) {
            val targetSong = startSong ?: album.songs.first()
            playSong(targetSong, album.songs)
        }
    }

    fun playArtist(artist: Artist, startSong: Song? = null) {
        if (artist.songs.isNotEmpty()) {
            val targetSong = startSong ?: artist.songs.first()
            playSong(targetSong, artist.songs)
        }
    }

    fun playPlaylist(playlist: Playlist, startSong: Song? = null, shuffle: Boolean = false) {
        val songs = if (shuffle) playlist.songs.shuffled() else playlist.songs
        if (songs.isNotEmpty()) {
            val targetSong = startSong ?: songs.first()
            playSong(targetSong, songs)
            _playingPlaylistId.value = playlist.id
        }
    }

    /** Plays the songs matching a voice search ("play <query> on Muzik") once the library and player are ready. */
    fun playFromSearch(query: String) {
        viewModelScope.launch {
            val library = combine(_songs, musicController.isControllerReady) { songs, ready -> songs.takeIf { ready } }
                .filterNotNull()
                .first { it.isNotEmpty() }
            val matches = library.forVoiceSearch(query)
            if (matches.isNotEmpty()) {
                _playingPlaylistId.value = null
                musicController.playSongList(matches, 0)
            }
        }
    }

    fun playRandom() {
        val all = _songs.value
        if (all.isNotEmpty()) {
            _playingPlaylistId.value = null
            val shuffled = all.shuffled()
            musicController.playSongList(shuffled, 0)
        }
    }

    fun togglePlayPause() {
        if (currentSong.value == null && _songs.value.isNotEmpty()) {
            playSong(_songs.value.first())
        } else {
            musicController.togglePlayPause()
        }
    }

    fun seekTo(positionMs: Long) { musicController.seekTo(positionMs) }
    fun skipNext() { musicController.skipToNext() }
    fun skipPrevious() { musicController.skipToPrevious() }
    fun toggleShuffle() { musicController.toggleShuffle() }
    fun toggleRepeat() { musicController.toggleRepeat() }

    fun setShowNowPlaying(show: Boolean) { _showNowPlaying.value = show }
    fun setShowQueue(show: Boolean) { _showQueue.value = show }
    fun selectSongForDetails(song: Song?) { _selectedSongForDetails.value = song }

    // ─── Confirmations (snackbar) ─────────────────────────────────────────────

    private val _messages = MutableSharedFlow<UiMessage>(extraBufferCapacity = 8)
    val messages: SharedFlow<UiMessage> = _messages.asSharedFlow()

    private fun showMessage(text: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
        _messages.tryEmit(UiMessage(text, actionLabel, onAction))
    }

    fun addToQueue(song: Song) {
        musicController.addToQueue(song)
        showMessage("Added to queue")
    }

    /** Queues a whole album, playlist, artist or folder after what's already queued. */
    fun addAllToQueue(songs: List<Song>) {
        if (songs.isEmpty()) return
        musicController.addAllToQueue(songs)
        showMessage(if (songs.size == 1) "Added to queue" else pluralize(songs.size, "song") + " added to queue")
    }

    fun removeQueueItem(index: Int) {
        val song = currentQueue.value.getOrNull(index) ?: return
        val current = currentQueueIndex.value
        val inUserQueue = index > current && index <= current + userQueueCount.value
        musicController.removeQueueItem(index)
        showMessage("Removed from queue", "Undo") { musicController.insertQueueItem(index, song, inUserQueue) }
    }
    fun playQueueIndex(index: Int) { musicController.playQueueIndex(index) }
    fun moveQueueItem(from: Int, to: Int) { musicController.moveQueueItem(from, to) }

    fun setSleepTimer(minutes: Int) = PlaybackSettings.setSleepTimer(minutes)
    fun setSleepAtEndOfTrack() = PlaybackSettings.setSleepAtEndOfTrack()
    fun cancelSleepTimer() = PlaybackSettings.cancelSleepTimer()
    fun setPlaybackSpeed(speed: Float) = PlaybackSettings.setSpeed(speed)
    fun setEqualizerEnabled(enabled: Boolean) = PlaybackSettings.setEqualizerEnabled(enabled)
    fun selectEqualizerPreset(preset: Int) = PlaybackSettings.selectEqualizerPreset(preset)
    fun setEqualizerBandLevel(band: Int, level: Int) = PlaybackSettings.setEqualizerBandLevel(band, level)
    fun resetEqualizer() = PlaybackSettings.resetEqualizer()

    fun navigateToArtist(artistName: String) {
        val target = artists.value.firstOrNull { it.name.equals(artistName, ignoreCase = true) }
            ?: artists.value.firstOrNull { it.name.contains(artistName, ignoreCase = true) }
            ?: Artist(
                name = artistName,
                songCount = _songs.value.count { it.artist.equals(artistName, ignoreCase = true) },
                albumCount = _songs.value.filter { it.artist.equals(artistName, ignoreCase = true) }.map { it.albumId }.distinct().size,
                songs = _songs.value.filter { it.artist.equals(artistName, ignoreCase = true) }
            )
        _selectedArtist.value = target
    }

    fun navigateToAlbum(albumId: Long) {
        _selectedAlbum.value = albums.value.firstOrNull { it.id == albumId }
    }

    fun selectAlbum(album: Album?) { _selectedAlbum.value = album }
    fun selectArtist(artist: Artist?) { _selectedArtist.value = artist }

    // ─── Playlist Actions ─────────────────────────────────────────────────────

    fun selectPlaylist(playlist: Playlist?) {
        _selectedPlaylistId.value = playlist?.id
    }

    fun setShowCreatePlaylistDialog(show: Boolean) { _showCreatePlaylistDialog.value = show }
    fun setSongPendingAddToPlaylist(song: Song?) { _songPendingAddToPlaylist.value = song }

    /** Creates a playlist and optionally adds [initialSongId] to it once it exists. */
    fun createPlaylist(name: String, initialSongId: Long? = null) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val created = playlistRepository.createPlaylist(name)
            initialSongId?.let { playlistRepository.addSongToPlaylist(created.id, it) }
        }
        // Room Flow auto-updates playlists StateFlow — no manual refresh
    }

    fun deletePlaylist(id: String) {
        viewModelScope.launch {
            playlistRepository.deletePlaylist(id)
            if (_selectedPlaylistId.value == id) _selectedPlaylistId.value = null
        }
    }

    fun addSongToPlaylist(playlistId: String, songId: Long) {
        viewModelScope.launch { playlistRepository.addSongToPlaylist(playlistId, songId) }
    }

    fun removeSongFromPlaylist(playlistId: String, songId: Long) {
        val playlist = playlists.value.firstOrNull { it.id == playlistId }
        viewModelScope.launch { playlistRepository.removeSongFromPlaylist(playlistId, songId) }
        if (playlist != null) {
            showMessage("Removed from ${playlist.name}", "Undo") {
                viewModelScope.launch {
                    playlistRepository.addSongToPlaylist(playlistId, songId)
                    playlistRepository.reorder(playlistId, playlist.songIds)
                }
            }
        }
    }

    /**
     * Saves an Edit playlist session: [name], and [editedSongs] as the remaining songs in their new
     * order. Songs the edit screen didn't show (missing from the device, or in hidden folders) keep
     * their positions.
     */
    fun savePlaylistEdits(playlist: Playlist, name: String, editedSongs: List<Song>) {
        val shownIds = playlist.songs.map { it.id }.toSet()
        val keptIds = editedSongs.map { it.id }
        val removedIds = shownIds - keptIds.toSet()
        val order = mergeEditedOrder(playlist.songIds, shownIds, keptIds)
        viewModelScope.launch {
            playlistRepository.applyEdits(playlist.id, name.ifBlank { playlist.name }, removedIds, order)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    /** Moves the pre-Room "recently played" list from SharedPreferences into play stats, once. */
    private fun importLegacyRecentHistory() {
        val raw = prefs.getString("recent_song_ids", null) ?: return
        val ids = raw.split(",").mapNotNull { it.trim().toLongOrNull() }
        viewModelScope.launch {
            libraryRepository.importRecentHistory(ids)
            prefs.edit().remove("recent_song_ids").apply()
        }
    }

    override fun onCleared() {
        super.onCleared()
        musicController.release()
    }
}
