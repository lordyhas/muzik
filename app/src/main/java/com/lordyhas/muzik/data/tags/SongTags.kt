package com.lordyhas.muzik.data.tags

/**
 * The tags the Tag editor shows, with their TagLib property-map key. TagLib maps these keys to
 * the right frame or field for each format (ID3v2, Vorbis comments, MP4 atoms...).
 */
enum class TagField(val key: String, val label: String, val numeric: Boolean = false) {
    TITLE("TITLE", "Title"),
    ARTIST("ARTIST", "Artist"),
    ALBUM("ALBUM", "Album"),
    ALBUM_ARTIST("ALBUMARTIST", "Album artist"),
    YEAR("DATE", "Year", numeric = true),
    GENRE("GENRE", "Genre"),
    TRACK("TRACKNUMBER", "Track", numeric = true),
    DISC("DISCNUMBER", "Disc", numeric = true),
    COMPOSER("COMPOSER", "Composer");
}

/** Tag values as shown in the editor: one string per field, "" when the tag is missing. */
typealias SongTags = Map<TagField, String>

/**
 * Reads the editor's fields from a TagLib property map. Several values (two artists) are joined
 * with "; ". A track or disc written as "3/12" shows as "3".
 */
fun tagsFromPropertyMap(map: Map<String, Array<String>>): SongTags =
    TagField.entries.associateWith { field ->
        val values = map[field.key].orEmpty().map { it.trim() }.filter { it.isNotEmpty() }
        val joined = values.joinToString("; ")
        if (field == TagField.TRACK || field == TagField.DISC) joined.substringBefore('/').trim() else joined
    }

/**
 * Writes [edits] into a TagLib property map. A blank value removes the tag. Only the fields in
 * [edits] are touched, so tags the editor doesn't show (lyrics, ReplayGain...) are kept.
 */
fun applyTagEdits(map: MutableMap<String, Array<String>>, edits: SongTags) {
    edits.forEach { (field, value) ->
        val trimmed = value.trim()
        if (trimmed.isEmpty()) map.remove(field.key) else map[field.key] = arrayOf(trimmed)
    }
}

/** The fields of [edited] whose value differs from [original] (missing counts as ""). */
fun changedTags(original: SongTags, edited: SongTags): SongTags =
    edited.filter { (field, value) -> value.trim() != original[field].orEmpty().trim() }

/** Track number and title guessed from a file name such as "01 - Intro.mp3". */
data class FileNameGuess(val track: Int?, val title: String)

private val TRACK_PREFIX = Regex("""^(\d{1,3})(?:\s*[-._)]\s*|\s+)(.+)$""")

/**
 * Guesses a track number and title from [fileName]: "01 - Intro.mp3", "02. Song", "3_Song.flac"
 * and "04 Song" all work. A name that only starts with digits ("2Pac - Changes") stays a title.
 * Underscores become spaces.
 */
fun guessFromFileName(fileName: String): FileNameGuess {
    val base = fileName.substringBeforeLast('.', fileName).replace('_', ' ').trim()
    val match = TRACK_PREFIX.matchEntire(base)
    return if (match != null) {
        FileNameGuess(match.groupValues[1].toInt(), match.groupValues[2].trim())
    } else {
        FileNameGuess(null, base)
    }
}
