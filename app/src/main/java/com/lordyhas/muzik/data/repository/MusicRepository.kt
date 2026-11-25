package com.lordyhas.muzik.data.repository

import android.content.ContentUris
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.lordyhas.muzik.data.SongArtworkProvider
import com.lordyhas.muzik.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MusicRepository(private val context: Context) {

    private val supportedExtensions = setOf("mp3", "m4a", "wav", "flac", "ogg", "aac", "opus")

    /** @param scanStorage ask the media scanner to index new files first (skipped by the playback service) */
    suspend fun getLocalSongs(scanStorage: Boolean = true): List<Song> = withContext(Dispatchers.IO) {
        // Trigger media scan on directories so Android indexes any newly added files
        if (scanStorage) scanAndIndexStorage()

        val songsList = mutableListOf<Song>()

        // Query Android MediaStore (single source of truth for all audio on the device)
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = listOfNotNull(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media.YEAR,
            @Suppress("DEPRECATION") MediaStore.Audio.Media.DATA,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaStore.Audio.Media.IS_RECORDING else null
        ).toTypedArray()

        val selection = "(${MediaStore.Audio.Media.IS_MUSIC} != 0 OR ${MediaStore.Audio.Media.MIME_TYPE} LIKE 'audio/%' OR ${MediaStore.Audio.Media.DISPLAY_NAME} LIKE '%.mp3' OR ${MediaStore.Audio.Media.DISPLAY_NAME} LIKE '%.m4a' OR ${MediaStore.Audio.Media.DISPLAY_NAME} LIKE '%.wav' OR ${MediaStore.Audio.Media.DISPLAY_NAME} LIKE '%.flac' OR ${MediaStore.Audio.Media.DISPLAY_NAME} LIKE '%.ogg' OR ${MediaStore.Audio.Media.DISPLAY_NAME} LIKE '%.aac' OR ${MediaStore.Audio.Media.DISPLAY_NAME} LIKE '%.opus') AND (${MediaStore.Audio.Media.IS_RINGTONE} == 0 OR ${MediaStore.Audio.Media.IS_RINGTONE} IS NULL) AND (${MediaStore.Audio.Media.IS_NOTIFICATION} == 0 OR ${MediaStore.Audio.Media.IS_NOTIFICATION} IS NULL) AND (${MediaStore.Audio.Media.IS_ALARM} == 0 OR ${MediaStore.Audio.Media.IS_ALARM} IS NULL)"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            val cursor = context.contentResolver.query(
                uri,
                projection,
                selection,
                null,
                sortOrder
            )

            cursor?.use {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val displayNameColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val trackColumn = it.getColumnIndex(MediaStore.Audio.Media.TRACK)
                val dateAddedColumn = it.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
                val dateModifiedColumn = it.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED)
                val yearColumn = it.getColumnIndex(MediaStore.Audio.Media.YEAR)
                @Suppress("DEPRECATION")
                val dataColumn = it.getColumnIndex(MediaStore.Audio.Media.DATA)
                val recordingColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    it.getColumnIndex(MediaStore.Audio.Media.IS_RECORDING)
                } else -1

                val albumArtUriBase = Uri.parse("content://media/external/audio/albumart")

                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val rawTitle = it.getString(titleColumn)
                    val rawArtist = it.getString(artistColumn)
                    val rawAlbum = it.getString(albumColumn)
                    val albumId = it.getLong(albumIdColumn)
                    val duration = it.getLong(durationColumn)
                    val displayName = it.getString(displayNameColumn) ?: "audio_${id}"
                    val rawTrack = if (trackColumn != -1 && !it.isNull(trackColumn)) it.getInt(trackColumn) else 0
                    val dateAdded = if (dateAddedColumn != -1 && !it.isNull(dateAddedColumn)) it.getLong(dateAddedColumn) else 0L
                    val dateModified = if (dateModifiedColumn != -1 && !it.isNull(dateModifiedColumn)) it.getLong(dateModifiedColumn) else 0L
                    val year = if (yearColumn != -1 && !it.isNull(yearColumn)) it.getInt(yearColumn).takeIf { y -> y > 0 } else null
                    val filePath = if (dataColumn != -1) it.getString(dataColumn) else null
                    val folderPath = filePath?.substringBeforeLast('/', missingDelimiterValue = "").orEmpty()
                    val isRecording = recordingColumn != -1 && !it.isNull(recordingColumn) && it.getInt(recordingColumn) != 0

                    val cleanTitle = when {
                        !rawTitle.isNullOrBlank() && rawTitle != "<unknown>" -> rawTitle
                        else -> displayName.substringBeforeLast(".")
                    }

                    val artist = if (rawArtist == "<unknown>" || rawArtist.isNullOrBlank()) "Unknown artist" else rawArtist
                    val album = if (rawAlbum == "<unknown>" || rawAlbum.isNullOrBlank()) "Unknown album" else rawAlbum

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    val albumArtUri = if (albumId > 0) {
                        ContentUris.withAppendedId(albumArtUriBase, albumId)
                    } else null

                    val discNumber: Int?
                    val trackNumber: Int?
                    when {
                        rawTrack >= 1000 -> {
                            discNumber = rawTrack / 1000
                            val t = rawTrack % 1000
                            trackNumber = if (t > 0) t else null
                        }
                        rawTrack > 0 -> {
                            discNumber = 1
                            trackNumber = rawTrack
                        }
                        else -> {
                            discNumber = null
                            trackNumber = null
                        }
                    }

                    val song = Song(
                        id = id,
                        title = cleanTitle,
                        artist = artist,
                        album = album,
                        albumId = albumId,
                        duration = if (duration < 0) 0L else duration,
                        contentUri = contentUri,
                        albumArtUri = albumArtUri,
                        artworkUri = SongArtworkProvider.uriFor(context, id, albumId, dateModified),
                        trackNumber = trackNumber,
                        discNumber = discNumber,
                        dateAdded = dateAdded,
                        year = year,
                        folderPath = folderPath,
                        fileName = displayName,
                        isRecording = isRecording,
                        dateModified = dateModified
                    )
                    songsList.add(song)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        songsList.sortedBy { it.title.lowercase() }
    }

    private fun scanAndIndexStorage() {
        try {
            val directories = listOf(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                Environment.getExternalStorageDirectory()
            )

            val filesFound = mutableListOf<File>()
            for (dir in directories) {
                if (dir != null && dir.exists() && dir.isDirectory) {
                    collectAudioFiles(dir, filesFound, maxDepth = 4)
                }
            }

            if (filesFound.isNotEmpty()) {
                val paths = filesFound.map { it.absolutePath }.toTypedArray()
                MediaScannerConnection.scanFile(context, paths, null, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun collectAudioFiles(dir: File, result: MutableList<File>, maxDepth: Int) {
        if (maxDepth <= 0) return
        val files = dir.listFiles() ?: return
        for (file in files) {
            if (file.isDirectory && !file.name.startsWith(".") && file.name != "Android") {
                collectAudioFiles(file, result, maxDepth - 1)
            } else if (file.isFile && file.extension.lowercase() in supportedExtensions) {
                result.add(file)
            }
        }
    }
}

