package com.lordyhas.muzik

import com.lordyhas.muzik.data.model.ReleaseNotes
import com.lordyhas.muzik.data.model.parseReleaseNotes
import com.lordyhas.muzik.data.model.releaseNotesToShow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class WhatsNewTest {

    private val json = """
        {
          "releases": [
            { "versionCode": 7, "versionName": "1.6.0",
              "highlights": [ { "type": "new", "title": "A", "text": "a" },
                              { "type": "whatever", "title": "B", "text": "b" } ] },
            { "versionCode": 9, "versionName": "1.8.0",
              "highlights": [ { "type": "FIXED", "title": "C", "text": "c" } ] },
            { "versionCode": 8, "versionName": "1.7.0", "highlights": [] }
          ]
        }
    """.trimIndent()

    private val all = parseReleaseNotes(json)

    @Test
    fun parsesNewestFirst() {
        assertEquals(listOf(9L, 8L, 7L), all.map { it.versionCode })
        assertEquals("1.8.0", all.first().versionName)
    }

    @Test
    fun parsesHighlightTypesWithFallback() {
        val highlights = all.last().highlights
        assertEquals(ReleaseNotes.Type.NEW, highlights[0].type)
        assertEquals(ReleaseNotes.Type.IMPROVED, highlights[1].type)
        assertEquals(ReleaseNotes.Type.FIXED, all.first().highlights.single().type)
    }

    @Test
    fun freshInstallShowsNothing() {
        assertTrue(releaseNotesToShow(all, currentCode = 9, lastSeenCode = null, isFreshInstall = true).isEmpty())
    }

    @Test
    fun updateFromBeforeTheFeatureShowsOnlyTheCurrentVersion() {
        val shown = releaseNotesToShow(all, currentCode = 9, lastSeenCode = null, isFreshInstall = false)
        assertEquals(listOf(9L), shown.map { it.versionCode })
    }

    @Test
    fun skippedVersionsAreAllShown() {
        val shown = releaseNotesToShow(all, currentCode = 9, lastSeenCode = 7, isFreshInstall = false)
        assertEquals(listOf(9L, 8L), shown.map { it.versionCode })
    }

    @Test
    fun alreadySeenShowsNothing() {
        assertTrue(releaseNotesToShow(all, currentCode = 9, lastSeenCode = 9, isFreshInstall = false).isEmpty())
    }

    @Test
    fun shippedReleaseNotesAreValid() {
        // Unit tests run from the module directory
        val shipped = parseReleaseNotes(File("src/main/assets/whatsnew.json").readText())
        assertTrue("whatsnew.json has no releases", shipped.isNotEmpty())
        shipped.forEach { release ->
            assertTrue("${release.versionName} has no highlights", release.highlights.isNotEmpty())
            release.highlights.forEach {
                assertTrue(it.title.isNotBlank() && it.text.isNotBlank())
            }
        }
        assertEquals("Duplicate versionCode", shipped.size, shipped.map { it.versionCode }.toSet().size)
    }
}
