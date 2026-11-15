package com.lordyhas.muzik.tageditor.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.lordyhas.muzik.ui.theme.AccentPrimary
import com.lordyhas.muzik.ui.theme.BackgroundDark
import com.lordyhas.muzik.ui.theme.TextMuted
import kotlinx.coroutines.launch

/** Every tag of one song, plus its embedded cover. */
@Composable
fun SongTagEditorScreen(
    viewModel: TagEditorViewModel,
    song: Song?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSaving by viewModel.isSaving.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val writeAccess = rememberWriteAccess()

    var original by remember { mutableStateOf<SongTags?>(null) }
    var loadFailed by remember { mutableStateOf(false) }
    val values = remember { mutableStateMapOf<TagField, String>() }
    var fileCover by remember { mutableStateOf<CoverArt?>(null) }
    var coverEdit by remember { mutableStateOf<CoverEdit?>(null) }
    var askDiscard by remember { mutableStateOf(false) }

    LaunchedEffect(song?.id) {
        if (song == null) return@LaunchedEffect
        val tags = viewModel.readTags(song)
        if (tags == null) {
            loadFailed = true
        } else {
            original = tags
            values.putAll(tags)
            fileCover = viewModel.readCover(song)
        }
    }

    val changes = original?.let { changedTags(it, values.toMap()) }.orEmpty()
    val isDirty = changes.isNotEmpty() || coverEdit != null

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) scope.launch {
            val cover = AudioTagStore.loadCoverFromImage(context, uri)
            if (cover != null) coverEdit = CoverEdit.Set(cover) else viewModel.showMessage("Couldn't read that image")
        }
    }

    BackHandler(enabled = isDirty && !isSaving) { askDiscard = true }

    Column(modifier = modifier.fillMaxSize().background(BackgroundDark)) {
        EditorTopBar(
            title = song?.title ?: "Edit tags",
            onBack = { if (isDirty) askDiscard = true else onBack() },
            canSave = isDirty,
            isSaving = isSaving,
            onSave = {
                if (song == null) return@EditorTopBar
                writeAccess.request(listOf(song.contentUri)) { granted ->
                    if (granted) {
                        viewModel.save(listOf(SongEdit(song, changes, coverEdit))) { ok -> if (ok) onBack() }
                    } else {
                        viewModel.showMessage("Muzik needs your OK to change this file")
                    }
                }
            }
        )

        when {
            song == null -> EmptyState(icon = Icons.Default.ErrorOutline, title = "Song not found")
            loadFailed -> EmptyState(
                icon = Icons.Default.ErrorOutline,
                title = "Can't read this file",
                message = "Its format may not support tags, or the file is no longer there."
            )
            original == null -> LoadingState()
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 20.dp)
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val shownCover = when (val edit = coverEdit) {
                    is CoverEdit.Set -> edit.cover
                    CoverEdit.Remove -> null
                    null -> fileCover
                }
                CoverEditor(
                    cover = rememberCoverBitmap(shownCover),
                    fallbackUri = if (coverEdit == null && fileCover == null) song.albumArtUri else null,
                    onChange = { pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    onRemove = if (shownCover != null) ({ coverEdit = CoverEdit.Remove }) else null,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Text(
                    text = song.fileName,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    modifier = Modifier.fillMaxWidth()
                )

                TagFieldInput(TagField.TITLE, values)
                TagFieldInput(TagField.ARTIST, values)
                TagFieldInput(TagField.ALBUM, values)
                TagFieldInput(TagField.ALBUM_ARTIST, values)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TagFieldInput(TagField.YEAR, values, Modifier.weight(1f))
                    TagFieldInput(TagField.GENRE, values, Modifier.weight(1.6f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TagFieldInput(TagField.TRACK, values, Modifier.weight(1f))
                    TagFieldInput(TagField.DISC, values, Modifier.weight(1f))
                }
                TagFieldInput(TagField.COMPOSER, values)

                TextButton(
                    onClick = {
                        val guess = guessFromFileName(song.fileName)
                        values[TagField.TITLE] = guess.title
                        guess.track?.let { values[TagField.TRACK] = it.toString() }
                    }
                ) {
                    Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = AccentPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Fill title and track from the file name", color = AccentPrimary)
                }
                Spacer(modifier = Modifier.height(8.dp))
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

@Composable
private fun TagFieldInput(field: TagField, values: MutableMap<TagField, String>, modifier: Modifier = Modifier) {
    TagTextField(
        label = field.label,
        value = values[field].orEmpty(),
        onValueChange = { values[field] = it },
        numeric = field.numeric,
        modifier = modifier.fillMaxWidth()
    )
}
