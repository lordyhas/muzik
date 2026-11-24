package com.lordyhas.muzik.data

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import androidx.core.net.toUri
import java.io.File
import java.io.FileNotFoundException

/**
 * Serves each song's own cover: the picture embedded in the file, or the album's cover from
 * MediaStore when the file has none. MediaStore only keeps one cover per album, so tracks of a
 * compilation would otherwise all show the same picture.
 *
 * A plain content URI works everywhere a cover is loaded (Coil, the Media3 notification and
 * session, the widget), all inside this process, so the provider isn't exported.
 *
 * URI: `content://<package>.artwork/song/<songId>?album=<albumId>&v=<dateModified>`. The file's
 * modification date is part of the URI, so a retagged file gets a fresh URI and skips stale caches.
 * Extracted pictures are cached in `cache/artwork` (`<id>_<v>.img`, or `.none` when there is none).
 */
class SongArtworkProvider : ContentProvider() {

    override fun onCreate(): Boolean = true

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        if (mode != "r") throw SecurityException("Artwork is read-only")
        val context = context ?: throw FileNotFoundException(uri.toString())
        val songId = uri.pathSegments.takeIf { it.size == 2 && it[0] == SONG_PATH }
            ?.get(1)?.toLongOrNull() ?: throw FileNotFoundException(uri.toString())
        val albumId = uri.getQueryParameter(PARAM_ALBUM)?.toLongOrNull() ?: -1L
        val version = uri.getQueryParameter(PARAM_VERSION)?.toLongOrNull() ?: 0L

        embeddedPicture(context, songId, version)?.let {
            return ParcelFileDescriptor.open(it, ParcelFileDescriptor.MODE_READ_ONLY)
        }
        if (albumId > 0) {
            val albumArt = ContentUris.withAppendedId(ALBUM_ART_BASE, albumId)
            try {
                context.contentResolver.openFileDescriptor(albumArt, "r")?.let { return it }
            } catch (_: Exception) {
                // No album cover either.
            }
        }
        throw FileNotFoundException("No artwork for song $songId")
    }

    /** The cached picture embedded in the song's file, extracting it on first use. */
    private fun embeddedPicture(context: Context, songId: Long, version: Long): File? {
        val dir = cacheDir(context)
        val image = File(dir, "${songId}_$version.img")
        val none = File(dir, "${songId}_$version.none")
        if (image.exists()) return image
        if (none.exists()) return null

        val bytes = try {
            MediaMetadataRetriever().run {
                try {
                    val audio = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId)
                    setDataSource(context, audio)
                    embeddedPicture
                } finally {
                    release()
                }
            }
        } catch (_: Exception) {
            null
        }

        // Older versions of this song's cover are stale now.
        dir.listFiles { f ->
            f.name.startsWith("${songId}_") && f.name != image.name && f.name != none.name
        }?.forEach { it.delete() }
        if (bytes == null || bytes.isEmpty()) {
            none.createNewFile()
            return null
        }
        // Write then rename, so a concurrent reader never sees half a picture.
        val tmp = File.createTempFile("tmp_", ".part", dir)
        tmp.writeBytes(bytes)
        if (!tmp.renameTo(image)) tmp.delete()
        return image.takeIf { it.exists() }
    }

    override fun getType(uri: Uri): String = "image/*"

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?,
                       selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(uri: Uri, values: ContentValues?, selection: String?,
                        selectionArgs: Array<out String>?): Int = 0

    companion object {
        private const val SONG_PATH = "song"
        private const val PARAM_ALBUM = "album"
        private const val PARAM_VERSION = "v"
        private val ALBUM_ART_BASE: Uri = "content://media/external/audio/albumart".toUri()

        /** The cover URI for one song; [albumId] is the fallback when the file has no picture. */
        fun uriFor(context: Context, songId: Long, albumId: Long, dateModified: Long): Uri =
            Uri.Builder()
                .scheme("content")
                .authority("${context.packageName}.artwork")
                .appendPath(SONG_PATH)
                .appendPath(songId.toString())
                .appendQueryParameter(PARAM_ALBUM, albumId.toString())
                .appendQueryParameter(PARAM_VERSION, dateModified.toString())
                .build()

        /** Forgets every extracted picture, e.g. after the tag editor changed a cover. */
        fun clearCache(context: Context) {
            cacheDir(context).listFiles()?.forEach { it.delete() }
        }

        private fun cacheDir(context: Context): File =
            File(context.cacheDir, "artwork").apply { mkdirs() }
    }
}
