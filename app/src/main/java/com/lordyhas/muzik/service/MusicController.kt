package com.lordyhas.muzik.service

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.lordyhas.muzik.data.model.Song
import com.lordyhas.muzik.service.QueuePlayer.addToQueue
import com.lordyhas.muzik.service.QueuePlayer.isAddedToQueue
import com.lordyhas.muzik.service.QueuePlayer.queueEntries
import com.lordyhas.muzik.service.QueuePlayer.queueItems
import com.lordyhas.muzik.service.QueuePlayer.reorderUpcoming
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * The app's side of playback. It sends user actions to the playback service through a
 * MediaController and mirrors the player into StateFlows for the UI.
 *
 * The queue is the player's own list: each item says whether it was added with "Add to
 * queue" (see [QueueRules] and [QueuePlayer]), so the UI state is always read back from the
 * player instead of being tracked on the side.
 */
@UnstableApi
class MusicController(private val context: Context) {

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null

    /** Library songs by id, to show queue items with their full details. */
    private var songsById: Map<Long, Song> = emptyMap()

    private val _currentMediaItem = MutableStateFlow<MediaItem?>(null)
    val currentMediaItem: StateFlow<MediaItem?> = _currentMediaItem.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    val isShuffle: StateFlow<Boolean> = PlaybackSettings.shuffle

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _isControllerReady = MutableStateFlow(false)
    val isControllerReady: StateFlow<Boolean> = _isControllerReady.asStateFlow()

    /** The whole list, played songs included (1, 2, 3, A, B, 4). */
    private val _currentQueue = MutableStateFlow<List<Song>>(emptyList())
    val currentQueue: StateFlow<List<Song>> = _currentQueue.asStateFlow()

    /** For each song of [currentQueue], whether it was added with "Add to queue". */
    private val _queueAdded = MutableStateFlow<List<Boolean>>(emptyList())
    val queueAdded: StateFlow<List<Boolean>> = _queueAdded.asStateFlow()

    private val _currentMediaItemIndex = MutableStateFlow(0)
    val currentMediaItemIndex: StateFlow<Int> = _currentMediaItemIndex.asStateFlow()

    /** How many added songs come right after the current one ("Next in queue"). */
    private val _userQueueCount = MutableStateFlow(0)
    val userQueueCount: StateFlow<Int> = _userQueueCount.asStateFlow()

    private val _userQueueList = MutableStateFlow<List<Song>>(emptyList())
    val userQueueList: StateFlow<List<Song>> = _userQueueList.asStateFlow()

    private val _contextQueueList = MutableStateFlow<List<Song>>(emptyList())
    val contextQueueList: StateFlow<List<Song>> = _contextQueueList.asStateFlow()

    private val playerListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            _currentMediaItem.value = mediaItem
            syncQueue()
            updateDuration()
            updatePosition()
            persist()
        }

        override fun onTimelineChanged(timeline: androidx.media3.common.Timeline, reason: Int) {
            syncQueue()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
            if (isPlaying) {
                startProgressTracker()
            } else {
                stopProgressTracker()
                persist()
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_READY) {
                updateDuration()
            }
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            _repeatMode.value = repeatMode
        }
    }

    init {
        PlaybackSettings.init(context)
        initController()
    }

    private fun initController() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, MusicPlaybackService::class.java)
        )

        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            try {
                mediaController = controllerFuture?.get()
                mediaController?.addListener(playerListener)

                // Sync initial states
                mediaController?.let { controller ->
                    _currentMediaItem.value = controller.currentMediaItem
                    _isPlaying.value = controller.isPlaying
                    _repeatMode.value = controller.repeatMode
                    syncQueue()
                    updateDuration()
                    updatePosition()
                    if (controller.isPlaying) {
                        startProgressTracker()
                    }
                }
                _isControllerReady.value = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, MoreExecutors.directExecutor())
    }

    /** Reads the queue back from the player into the UI flows. */
    private fun syncQueue() {
        val controller = mediaController ?: return
        val items = controller.queueItems()
        val index = controller.currentMediaItemIndex
        val songs = items.map { songFor(it) }
        val upcomingAdded = QueueRules.upcomingAddedCount(controller.queueEntries(), index)

        _currentQueue.value = songs
        _queueAdded.value = items.map { it.isAddedToQueue }
        _currentMediaItemIndex.value = index
        _userQueueCount.value = upcomingAdded
        if (index in songs.indices) {
            val addedEnd = index + 1 + upcomingAdded
            _userQueueList.value = songs.subList(index + 1, addedEnd)
            _contextQueueList.value = songs.subList(addedEnd, songs.size)
        } else {
            _userQueueList.value = emptyList()
            _contextQueueList.value = emptyList()
        }
    }

    /** The library song behind [item], or one rebuilt from the item when it isn't known. */
    private fun songFor(item: MediaItem): Song {
        val id = item.mediaId.toLongOrNull() ?: -1L
        songsById[id]?.let { return it }
        val meta = item.mediaMetadata
        return Song(
            id = id,
            title = meta.title?.toString().orEmpty(),
            artist = meta.artist?.toString().orEmpty(),
            album = meta.albumTitle?.toString().orEmpty(),
            albumId = -1L,
            duration = meta.durationMs ?: 0L,
            contentUri = item.localConfiguration?.uri ?: Uri.EMPTY,
            albumArtUri = meta.artworkUri,
            trackNumber = meta.trackNumber,
            discNumber = meta.discNumber
        )
    }

    private fun remember(songs: List<Song>) {
        if (songs.any { songsById[it.id] != it }) songsById = songsById + songs.associateBy { it.id }
    }

    /**
     * Plays [songs] (an album, a playlist, a list) from [startIndex]. It replaces the whole
     * queue, added songs included. With shuffle on, the songs after the first one are shuffled.
     */
    fun playSongList(songs: List<Song>, startIndex: Int) {
        val controller = mediaController ?: return
        if (songs.isEmpty()) return
        remember(songs)
        val start = startIndex.coerceIn(0, songs.lastIndex)
        val items = songs.mapIndexed { i, song -> song.toMediaItem(contextOrder = i) }
        val playItems = if (PlaybackSettings.shuffle.value) {
            items.take(start + 1) + items.drop(start + 1).shuffled()
        } else {
            items
        }

        controller.shuffleModeEnabled = false
        controller.setMediaItems(playItems, start, 0L)
        controller.prepare()
        controller.play()
        persist()
    }

    fun addToQueue(song: Song) = addAllToQueue(listOf(song))

    /**
     * "Add to queue": right after the current song and the songs already added, in order.
     * With nothing playing, the songs simply start playing as a list.
     */
    fun addAllToQueue(songs: List<Song>) {
        if (songs.isEmpty()) return
        val controller = mediaController ?: return

        if (controller.mediaItemCount == 0) {
            playSongList(songs, 0)
            return
        }

        remember(songs)
        controller.addToQueue(songs.map { it.toMediaItem() })
        persist()
    }

    /** Removes the song at [index] and returns its item, to put it back with [insertQueueItem]. */
    fun removeQueueItem(index: Int): MediaItem? {
        val controller = mediaController ?: return null
        if (index !in 0 until controller.mediaItemCount) return null
        val item = controller.getMediaItemAt(index)
        controller.removeMediaItem(index)
        persist()
        return item
    }

    /** Puts back an item removed with [removeQueueItem], with its queue marks. */
    fun insertQueueItem(index: Int, item: MediaItem) {
        val controller = mediaController ?: return
        controller.addMediaItem(index.coerceIn(0, controller.mediaItemCount), item)
        persist()
    }

    fun playQueueIndex(index: Int) {
        val controller = mediaController ?: return
        if (index in 0 until controller.mediaItemCount) {
            controller.seekTo(index, 0L)
            if (!controller.isPlaying) {
                controller.play()
            }
        }
    }

    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        val controller = mediaController ?: return
        if (fromIndex in 0 until controller.mediaItemCount && toIndex in 0 until controller.mediaItemCount) {
            controller.moveMediaItem(fromIndex, toIndex)
            persist()
        }
    }

    fun togglePlayPause() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else {
            if (controller.playbackState == Player.STATE_IDLE) {
                controller.prepare()
            }
            controller.play()
        }
    }

    fun seekTo(positionMs: Long) {
        val controller = mediaController ?: return
        controller.seekTo(positionMs)
        _currentPosition.value = positionMs
    }

    fun skipToNext() {
        val controller = mediaController ?: return
        if (controller.hasNextMediaItem()) {
            controller.seekToNextMediaItem()
        }
    }

    fun skipToPrevious() {
        val controller = mediaController ?: return
        if (controller.currentPosition > 3000) {
            controller.seekTo(0)
        } else if (controller.hasPreviousMediaItem()) {
            controller.seekToPreviousMediaItem()
        } else {
            controller.seekTo(0)
        }
    }

    /** Shuffles, or puts back in order, the album/playlist songs still to come. */
    fun toggleShuffle() {
        val controller = mediaController ?: return
        val enabled = !PlaybackSettings.shuffle.value
        PlaybackSettings.setShuffle(enabled)
        controller.shuffleModeEnabled = false
        controller.reorderUpcoming(enabled)
        persist()
    }

    fun toggleRepeat() {
        val controller = mediaController ?: return
        val nextMode = when (controller.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        controller.repeatMode = nextMode
        _repeatMode.value = nextMode
        persist()
    }

    // ─── Session persistence ────────────────────────────────────────────────

    private fun persist() {
        val controller = mediaController ?: return
        PlaybackStateStore.save(context, controller)
    }

    /**
     * Called with the library once it is loaded (and again when it changes). If the player is
     * idle (fresh start), reload the last session paused at its saved position; otherwise just
     * refresh the queue shown, now that every song can be resolved.
     */
    fun restoreOrSync(library: List<Song>) {
        val controller = mediaController ?: return
        if (library.isEmpty()) return
        songsById = library.associateBy { it.id }

        if (controller.mediaItemCount > 0) {
            syncQueue()
            return
        }

        val saved = PlaybackStateStore.load(context) ?: return
        val restored = saved.toMediaItems(songsById) ?: return
        val position = restored.startPositionMs.takeIf { it != C.TIME_UNSET } ?: 0L

        controller.setMediaItems(restored.mediaItems, restored.startIndex, position)
        controller.repeatMode = saved.repeatMode
        _repeatMode.value = saved.repeatMode
        controller.prepare()

        syncQueue()
        updateDuration()
        _currentPosition.value = position
    }

    private fun updateDuration() {
        val dur = mediaController?.duration ?: 0L
        _duration.value = if (dur < 0) 0L else dur
    }

    private fun updatePosition() {
        val pos = mediaController?.currentPosition ?: 0L
        _currentPosition.value = if (pos < 0) 0L else pos
    }

    private fun startProgressTracker() {
        stopProgressTracker()
        progressJob = scope.launch {
            var ticks = 0
            while (isActive) {
                updatePosition()
                if (++ticks % 10 == 0) persist() // every ~5 s
                delay(500)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    fun release() {
        persist()
        stopProgressTracker()
        mediaController?.removeListener(playerListener)
        controllerFuture?.let { MediaController.releaseFuture(it) }
        mediaController = null
        controllerFuture = null
    }
}
