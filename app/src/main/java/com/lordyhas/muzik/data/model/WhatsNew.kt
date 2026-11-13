package com.lordyhas.muzik.data.model

import org.json.JSONObject

/** One version's user-facing release notes, from `assets/whatsnew.json`. */
data class ReleaseNotes(
    val versionCode: Long,
    val versionName: String,
    val highlights: List<Highlight>
) {
    data class Highlight(val type: Type, val title: String, val text: String)

    enum class Type(val label: String) { NEW("New"), IMPROVED("Improved"), FIXED("Fixed") }
}

/** Parses `whatsnew.json`, newest release first. Unknown highlight types read as [ReleaseNotes.Type.IMPROVED]. */
fun parseReleaseNotes(json: String): List<ReleaseNotes> {
    val releases = JSONObject(json).getJSONArray("releases")
    return (0 until releases.length()).map { i ->
        val release = releases.getJSONObject(i)
        val highlights = release.getJSONArray("highlights")
        ReleaseNotes(
            versionCode = release.getLong("versionCode"),
            versionName = release.getString("versionName"),
            highlights = (0 until highlights.length()).map { j ->
                val item = highlights.getJSONObject(j)
                ReleaseNotes.Highlight(
                    type = ReleaseNotes.Type.entries.firstOrNull {
                        it.name.equals(item.optString("type"), ignoreCase = true)
                    } ?: ReleaseNotes.Type.IMPROVED,
                    title = item.getString("title"),
                    text = item.getString("text")
                )
            }
        )
    }.sortedByDescending { it.versionCode }
}

/**
 * The release notes to show once after an update: every version newer than [lastSeenCode] up to
 * [currentCode]. Nothing on a fresh install (there's nothing "new" yet). When [lastSeenCode] is
 * unknown on an update (coming from a version without this feature), only [currentCode]'s notes.
 */
fun releaseNotesToShow(
    all: List<ReleaseNotes>,
    currentCode: Long,
    lastSeenCode: Long?,
    isFreshInstall: Boolean
): List<ReleaseNotes> {
    if (lastSeenCode == null && isFreshInstall) return emptyList()
    val since = lastSeenCode ?: (currentCode - 1)
    return all.filter { it.versionCode in (since + 1)..currentCode }
}
