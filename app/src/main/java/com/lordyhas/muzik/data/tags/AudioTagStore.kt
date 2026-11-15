package com.lordyhas.muzik.data.tags

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import androidx.core.graphics.scale
import com.kyant.taglib.Picture
import com.kyant.taglib.TagLib
import com.lordyhas.muzik.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume
import kotlin.math.max

/** An embedded cover: the image bytes and their MIME type. */
class CoverArt(val data: ByteArray, val mimeType: String)

/** A change of embedded cover. */
sealed interface CoverEdit {
    data class Set(val cover: CoverArt) : CoverEdit
    data object Remove : CoverEdit
}

/** Everything to write into one song's file. */
data class SongEdit(val song: Song, val tags: SongTags, val cover: CoverEdit? = null) {
    val isEmpty: Boolean get() = tags.isEmpty() && cover == null
}

/**
 * Reads and writes tags inside the audio files themselves, through TagLib on a file descriptor
 * (so it works with MediaStore content URIs). Writing needs write access to each URI first:
 * see [com.lordyhas.muzik.tageditor.rememberWriteAccess].
 */
class AudioTagStore(private val context: Context) {

    suspend fun readTags(song: Song): SongTags? = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openFileDescriptor(song.contentUri, "r")?.use { pfd ->
                // TagLib takes ownership of (and closes) the detached descriptor
                TagLib.getMetadata(pfd.dup().detachFd(), false)?.let { tagsFromPropertyMap(it.propertyMap) }
            }
        }.getOrNull()
    }

    suspend fun readCover(song: Song): CoverArt? = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openFileDescriptor(song.contentUri, "r")?.use { pfd ->
                TagLib.getFrontCover(pfd.dup().detachFd())?.let { CoverArt(it.data, it.mimeType) }
            }
        }.getOrNull()
    }

    /**
     * Writes [edits] and asks MediaStore to re-read the files. Returns the songs that could not be
     * written (missing file, read-only storage, unsupported format...).
     */
    suspend fun write(edits: List<SongEdit>): List<Song> = withContext(Dispatchers.IO) {
        val failed = edits.filterNot { it.isEmpty || writeOne(it) }.map { it.song }
        rescan(edits.filterNot { it.isEmpty }.map { it.song } - failed.toSet())
        failed
    }

    private fun writeOne(edit: SongEdit): Boolean = runCatching {
        context.contentResolver.openFileDescriptor(edit.song.contentUri, "rw")?.use { pfd ->
            var ok = true
            if (edit.tags.isNotEmpty()) {
                val metadata = TagLib.getMetadata(pfd.dup().detachFd(), false) ?: return@use false
                val map = HashMap(metadata.propertyMap)
                applyTagEdits(map, edit.tags)
                ok = TagLib.savePropertyMap(pfd.dup().detachFd(), map)
            }
            when (val cover = edit.cover) {
                null -> Unit
                CoverEdit.Remove -> ok = TagLib.savePictures(pfd.dup().detachFd(), emptyArray()) && ok
                is CoverEdit.Set -> {
                    val picture = Picture(cover.cover.data, "", "Front Cover", cover.cover.mimeType)
                    ok = TagLib.savePictures(pfd.dup().detachFd(), arrayOf(picture)) && ok
                }
            }
            ok
        } ?: false
    }.getOrDefault(false)

    /** Waits until MediaStore has re-read [songs], so the new tags show up in the library. */
    private suspend fun rescan(songs: List<Song>) {
        val paths = songs.map { File(it.folderPath, it.fileName).path }.filter { it.isNotBlank() }
        if (paths.isEmpty()) return
        suspendCancellableCoroutine { cont ->
            val remaining = AtomicInteger(paths.size)
            MediaScannerConnection.scanFile(context, paths.toTypedArray(), null) { _, _ ->
                if (remaining.decrementAndGet() == 0 && cont.isActive) cont.resume(Unit)
            }
        }
    }

    companion object {
        private const val MAX_COVER_SIZE = 1000

        /**
         * Loads an image picked by the user as a cover: scaled down to at most 1000 px and
         * re-encoded as JPEG, so a 12 MP photo doesn't add megabytes to every song of the album.
         */
        suspend fun loadCoverFromImage(context: Context, uri: Uri): CoverArt? = withContext(Dispatchers.IO) {
            runCatching {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
                var sample = 1
                while (max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_COVER_SIZE) sample *= 2
                val decoded = context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
                } ?: return@runCatching null
                val scale = MAX_COVER_SIZE.toFloat() / max(decoded.width, decoded.height)
                val bitmap = if (scale < 1f) {
                    decoded.scale((decoded.width * scale).toInt(), (decoded.height * scale).toInt())
                } else decoded
                val out = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                CoverArt(out.toByteArray(), "image/jpeg")
            }.getOrNull()
        }
    }
}
