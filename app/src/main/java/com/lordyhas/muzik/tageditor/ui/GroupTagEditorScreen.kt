package com.lordyhas.muzik.tageditor.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lordyhas.muzik.data.model.Song
import com.lordyhas.muzik.data.tags.AudioTagStore
import com.lordyhas.muzik.data.tags.CoverArt
import com.lordyhas.muzik.data.tags.CoverEdit
import com.lordyhas.muzik.data.tags.SongEdit
import com.lordyhas.muzik.data.tags.SongTags
import com.lordyhas.muzik.data.tags.TagField
import com.lordyhas.muzik.data.tags.changedTags
import com.lordyhas.muzik.data.tags.guessFromFileName
import com.lordyhas.muzik.tageditor.TagEditorViewModel
import com.lordyhas.muzik.tageditor.rememberWriteAccess
import com.lordyhas.muzik.ui.components.EmptyState
import com.lordyhas.muzik.ui.components.LoadingState
import com.lordyhas.muzik.ui.components.pluralize
import com.lordyhas.muzik.ui.theme.AccentPrimary
import com.lordyhas.muzik.ui.theme.BackgroundDark
import com.lordyhas.muzik.ui.theme.SurfaceCard
import com.lordyhas.muzik.ui.theme.TextMuted
import com.lordyhas.muzik.ui.theme.TextPrimary
import com.lordyhas.muzik.ui.theme.TextSecondary
import kotlinx.coroutines.launch

/** What a group page edits: the fields shared by all its songs, and how songs are listed. */
enum class TagGroupKind(
    val sharedFields: List<TagField>,
    val editsTrackNumbers: Boolean,
    val editsCover: Boolean
) {
    ALBUM(
        listOf(TagField.ALBUM, TagField.ALBUM_ARTIST, TagField.ARTIST, TagField.YEAR, TagField.GENRE),
        editsTrackNumbers = true,
        editsCover = true
    ),
    ARTIST(listOf(TagField.ARTIST, TagField.GENRE), editsTrackNumbers = false, editsCover = false)
}

/**
 * Edits a whole album (or an artist's songs) at once: the shared fields apply to every song,
 * and each song's title (and track number for an album) can be typed right in the list. That's
 * the quick way to tag an album that has no tags at all.
 */
@Composable
fun GroupTagEditorScreen(
    viewModel: TagEditorViewModel,
    kind: TagGroupKind,
    title: String,
    songs: List<Song>,
    onOpenSong: (Song) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSaving by viewModel.isSaving.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val writeAccess = rememberWriteAccess()

    var originals by remember { mutableStateOf<Map<Long, SongTags>?>(null) }
    var sharedOriginal by remember { mutableStateOf<Map<TagField, String>>(emptyMap()) }
    var mixedFields by remember { mutableStateOf<Set<TagField>>(emptySet()) }
    val shared = remember { mutableStateMapOf<TagField, String>() }
    val titles = remember { mutableStateMapOf<Long, String>() }
    val tracks = remember { mutableStateMapOf<Long, String>() }
    var fileCover by remember { mutableStateOf<CoverArt?>(null) }
    var coverEdit by remember { mutableStateOf<CoverEdit?>(null) }
    var askDiscard by remember { mutableStateOf(false) }

    LaunchedEffect(songs.map { it.id }) {
        val loaded = songs.associate { it.id to (viewModel.readTags(it) ?: emptyMap()) }
        val (common, mixed) = sharedValues(loaded.values, kind.sharedFields)
        sharedOriginal = common
        mixedFields = mixed
        shared.clear(); shared.putAll(common)
        titles.clear(); tracks.clear()
        loaded.forEach { (id, tags) ->
            titles[id] = tags[TagField.TITLE].orEmpty()
            tracks[id] = tags[TagField.TRACK].orEmpty()
        }
        if (kind.editsCover) fileCover = songs.firstOrNull()?.let { viewModel.readCover(it) }
        originals = loaded
    }

    val edits: List<SongEdit> = originals?.let { loaded ->
        songs.mapNotNull { song ->
            val base = loaded[song.id] ?: return@mapNotNull null
            val edited = base.toMutableMap()
            kind.sharedFields.forEach { field ->
                val value = shared[field].orEmpty()
                // A field with several values is only applied once something is typed in it
                if (value != sharedOriginal[field].orEmpty()) edited[field] = value
            }
            edited[TagField.TITLE] = titles[song.id].orEmpty()
            if (kind.editsTrackNumbers) edited[TagField.TRACK] = tracks[song.id].orEmpty()
            SongEdit(song, changedTags(base, edited), coverEdit)
        }.filterNot { it.isEmpty }
    }.orEmpty()
    val isDirty = edits.isNotEmpty()

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) scope.launch {
            val cover = AudioTagStore.loadCoverFromImage(context, uri)
            if (cover != null) coverEdit = CoverEdit.Set(cover) else viewModel.showMessage("Couldn't read that image")
        }
    }

    BackHandler(enabled = isDirty && !isSaving) { askDiscard = true }

    Column(modifier = modifier.fillMaxSize().background(BackgroundDark)) {
        EditorTopBar(
            title = title,
            onBack = { if (isDirty) askDiscard = true else onBack() },
            canSave = isDirty,
            isSaving = isSaving,
            onSave = {
                writeAccess.request(edits.map { it.song.contentUri }) { granted ->
                    if (granted) {
                        viewModel.save(edits) { ok -> if (ok) onBack() }
                    } else {
                        viewModel.showMessage("Muzik needs your OK to change these files")
                    }
                }
            }
        )

        when {
            songs.isEmpty() -> EmptyState(icon = Icons.Default.ErrorOutline, title = "Nothing to edit here")
            originals == null -> LoadingState()
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().imePadding(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (kind.editsCover) {
                    item(key = "cover") {
                        val shownCover = when (val edit = coverEdit) {
                            is CoverEdit.Set -> edit.cover
                            CoverEdit.Remove -> null
                            null -> fileCover
                        }
                        CoverEditor(
                            cover = rememberCoverBitmap(shownCover),
                            fallbackUri = if (coverEdit == null && fileCover == null) songs.first().albumArtUri else null,
                            onChange = { pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            onRemove = if (shownCover != null) ({ coverEdit = CoverEdit.Remove }) else null,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                item(key = "shared_title") {
                    EditorSectionTitle(
                        title = "For all ${pluralize(songs.size, "song")}",
                        subtitle = if (mixedFields.isNotEmpty()) "Fields with several values are left alone unless you type in them" else null
                    )
                }
                items(kind.sharedFields, key = { "shared_${it.name}" }) { field ->
                    TagTextField(
                        label = field.label,
                        value = shared[field].orEmpty(),
                        onValueChange = { shared[field] = it },
                        placeholder = if (field in mixedFields) "Several values" else null,
                        numeric = field.numeric,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item(key = "songs_title") {
                    Column {
                        EditorSectionTitle(title = "Songs")
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()).padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ToolChip("Titles from file names", Icons.Default.AutoFixHigh) {
                                songs.forEach { song ->
                                    val guess = guessFromFileName(song.fileName)
                                    titles[song.id] = guess.title
                                    if (kind.editsTrackNumbers) guess.track?.let { tracks[song.id] = it.toString() }
                                }
                            }
                            if (kind.editsTrackNumbers) {
                                ToolChip("Number 1 to ${songs.size}", Icons.Default.FormatListNumbered) {
                                    songs.forEachIndexed { index, song -> tracks[song.id] = (index + 1).toString() }
                                }
                            }
                        }
                    }
                }

                items(songs, key = { it.id }) { song ->
                    SongRowEditor(
                        song = song,
                        title = titles[song.id].orEmpty(),
                        onTitleChange = { titles[song.id] = it },
                        track = if (kind.editsTrackNumbers) tracks[song.id].orEmpty() else null,
                        onTrackChange = { tracks[song.id] = it },
                        showAlbum = !kind.editsTrackNumbers,
                        // Opening another page would drop the edits made here
                        onOpenSong = if (isDirty) null else ({ onOpenSong(song) })
                    )
                }

                item(key = "bottom") { Spacer(modifier = Modifier.navigationBarsPadding()) }
            }
        }
    }

    if (askDiscard) {
        DiscardChangesDialog(
            onDiscard = { askDiscard = false; onBack() },
            onKeepEditing = { askDiscard = false }
        )
    }
}

/**
 * For each of [fields]: the value every song shares, or "" plus a "mixed" mark when the songs
 * disagree. A tag missing from some songs only counts as a value when no other song has one.
 */
internal fun sharedValues(all: Collection<SongTags>, fields: List<TagField>): Pair<Map<TagField, String>, Set<TagField>> {
    val common = mutableMapOf<TagField, String>()
    val mixed = mutableSetOf<TagField>()
    fields.forEach { field ->
        val distinct = all.map { it[field].orEmpty().trim() }.distinct()
        if (distinct.size <= 1) common[field] = distinct.firstOrNull().orEmpty() else {
            common[field] = ""
            mixed += field
        }
    }
    return common to mixed
}

@Composable
private fun ToolChip(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = AccentPrimary, modifier = Modifier.size(18.dp)) },
        colors = AssistChipDefaults.assistChipColors(containerColor = SurfaceCard, labelColor = TextPrimary),
        border = null
    )
}

@Composable
private fun SongRowEditor(
    song: Song,
    title: String,
    onTitleChange: (String) -> Unit,
    track: String?,
    onTrackChange: (String) -> Unit,
    showAlbum: Boolean,
    onOpenSong: (() -> Unit)?
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (track != null) {
                TagTextField(label = "#", value = track, onValueChange = onTrackChange, numeric = true, modifier = Modifier.width(68.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            TagTextField(label = "Title", value = title, onValueChange = onTitleChange, modifier = Modifier.weight(1f))
            if (onOpenSong != null) {
                IconButton(onClick = onOpenSong) {
                    Icon(Icons.Default.Edit, contentDescription = "All tags of this song", tint = TextSecondary)
                }
            }
        }
        Text(
            text = if (showAlbum) "${song.album} · ${song.fileName}" else song.fileName,
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
        )
    }
}
