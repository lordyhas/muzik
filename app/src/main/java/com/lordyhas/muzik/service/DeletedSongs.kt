package com.lordyhas.muzik.service

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Songs whose files were just deleted (by the Tag editor), announced in-process so the running
 * [MusicPlaybackService] drops them from the queue. A saved session needs no signal: restoring
 * it already skips songs that are gone (PlaybackStateStore).
 */
object DeletedSongs {
    private val _ids = MutableSharedFlow<Set<Long>>(extraBufferCapacity = 8)
    val ids: SharedFlow<Set<Long>> = _ids.asSharedFlow()

    fun announce(ids: Set<Long>) {
        if (ids.isNotEmpty()) _ids.tryEmit(ids)
    }
}
