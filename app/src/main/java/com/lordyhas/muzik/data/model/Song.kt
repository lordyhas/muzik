package com.lordyhas.muzik.data.model

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.lordyhas.muzik.service.QueuePlayer.withQueueMarks
import java.util.Locale
import java.util.concurrent.TimeUnit

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val duration: Long,
    val contentUri: Uri,
    val albumArtUri: Uri? = null,
    val trackNumber: Int? = null,
    val discNumber: Int? = null,
    /** Seconds since epoch when the file was added to the device (MediaStore DATE_ADDED). */
    val dateAdded: Long = 0L,
    /** Directory containing the file, e.g. "/storage/emulated/0/Music/Rock". */
    val folderPath: String = "",
    /** File name with extension, e.g. "01 - Intro.mp3". */
    val fileName: String = "",
    /** Voice/call recording according to MediaStore (Android 12+ only, always false before). */
    val isRecording: Boolean = false
) {
    fun formattedDuration(): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(minutes)
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    /**
     * The player item for this song. [contextOrder] is its place in the album/playlist being
     * played and [addedToQueue] marks songs put in the queue by hand (see QueueRules).
     */
    fun toMediaItem(contextOrder: Int? = null, addedToQueue: Boolean = false): MediaItem {
        val item = toPlainMediaItem()
        return if (contextOrder != null || addedToQueue) {
            item.withQueueMarks(addedToQueue, contextOrder ?: -1)
        } else {
            item
        }
    }

    private fun toPlainMediaItem(): MediaItem {
        val metadataBuilder = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setAlbumTitle(album)
            .setDisplayTitle(title)

        trackNumber?.let { metadataBuilder.setTrackNumber(it) }
        discNumber?.let { metadataBuilder.setDiscNumber(it) }

        albumArtUri?.let {
            metadataBuilder.setArtworkUri(it)
        }

        return MediaItem.Builder()
            .setMediaId(id.toString())
            .setUri(contentUri)
            .setMediaMetadata(metadataBuilder.build())
            .build()
    }
}
