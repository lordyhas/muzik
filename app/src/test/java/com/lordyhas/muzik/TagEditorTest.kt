package com.lordyhas.muzik

import com.lordyhas.muzik.data.tags.FileNameGuess
import com.lordyhas.muzik.data.tags.TagField
import com.lordyhas.muzik.data.tags.applyTagEdits
import com.lordyhas.muzik.data.tags.changedTags
import com.lordyhas.muzik.data.tags.guessFromFileName
import com.lordyhas.muzik.data.tags.tagsFromPropertyMap
import com.lordyhas.muzik.tageditor.ui.sharedValues
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TagEditorTest {

    @Test
    fun `reads fields from a property map`() {
        val tags = tagsFromPropertyMap(
            mapOf(
                "TITLE" to arrayOf("Intro"),
                "ARTIST" to arrayOf("A", "B"),
                "TRACKNUMBER" to arrayOf("3/12"),
                "DATE" to arrayOf(" 2021 ")
            )
        )
        assertEquals("Intro", tags[TagField.TITLE])
        assertEquals("A; B", tags[TagField.ARTIST])
        assertEquals("3", tags[TagField.TRACK])
        assertEquals("2021", tags[TagField.YEAR])
        assertEquals("", tags[TagField.ALBUM])
    }

    @Test
    fun `edits replace, blank removes, other tags are kept`() {
        val map = mutableMapOf(
            "TITLE" to arrayOf("Old"),
            "GENRE" to arrayOf("Rock"),
            "LYRICS" to arrayOf("la la")
        )
        applyTagEdits(map, mapOf(TagField.TITLE to " New ", TagField.GENRE to "", TagField.ALBUM to "Album"))
        assertArrayEquals(arrayOf("New"), map["TITLE"])
        assertFalse(map.containsKey("GENRE"))
        assertArrayEquals(arrayOf("Album"), map["ALBUM"])
        assertArrayEquals(arrayOf("la la"), map["LYRICS"])
    }

    @Test
    fun `only changed fields are written`() {
        val original = mapOf(TagField.TITLE to "Song", TagField.TRACK to "3")
        val edited = mapOf(TagField.TITLE to "Song ", TagField.TRACK to "4", TagField.ALBUM to "")
        assertEquals(mapOf(TagField.TRACK to "4"), changedTags(original, edited))
    }

    @Test
    fun `guesses track and title from file names`() {
        assertEquals(FileNameGuess(1, "Intro"), guessFromFileName("01 - Intro.mp3"))
        assertEquals(FileNameGuess(2, "Song Name"), guessFromFileName("02. Song Name.flac"))
        assertEquals(FileNameGuess(3, "My Song"), guessFromFileName("3_My_Song.ogg"))
        assertEquals(FileNameGuess(4, "Song"), guessFromFileName("04 Song.m4a"))
        assertEquals(FileNameGuess(null, "2Pac - Changes"), guessFromFileName("2Pac - Changes.mp3"))
        assertEquals(FileNameGuess(null, "1999 - Prince"), guessFromFileName("1999 - Prince.mp3"))
        assertEquals(FileNameGuess(null, "Untitled"), guessFromFileName("Untitled"))
    }

    @Test
    fun `shared values mark fields where songs disagree`() {
        val songs = listOf(
            mapOf(TagField.ALBUM to "X", TagField.YEAR to "2020"),
            mapOf(TagField.ALBUM to "X", TagField.YEAR to "2021")
        )
        val (common, mixed) = sharedValues(songs, listOf(TagField.ALBUM, TagField.YEAR, TagField.GENRE))
        assertEquals("X", common[TagField.ALBUM])
        assertEquals("", common[TagField.YEAR])
        assertEquals("", common[TagField.GENRE])
        assertTrue(TagField.YEAR in mixed)
        assertFalse(TagField.GENRE in mixed)
    }
}
