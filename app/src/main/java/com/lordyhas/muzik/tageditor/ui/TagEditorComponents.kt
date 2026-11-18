package com.lordyhas.muzik.tageditor.ui

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lordyhas.muzik.data.tags.CoverArt
import com.lordyhas.muzik.ui.components.Artwork
import com.lordyhas.muzik.ui.components.CircleIconButton
import com.lordyhas.muzik.ui.theme.OnAccentFill
import com.lordyhas.muzik.ui.theme.AccentFill
import com.lordyhas.muzik.ui.theme.AccentPrimary
import com.lordyhas.muzik.ui.theme.AccentSecondary
import com.lordyhas.muzik.ui.theme.BackgroundDark
import com.lordyhas.muzik.ui.theme.DangerRed
import com.lordyhas.muzik.ui.theme.SurfaceCard
import com.lordyhas.muzik.ui.theme.SurfaceDark
import com.lordyhas.muzik.ui.theme.TextMuted
import com.lordyhas.muzik.ui.theme.TextPrimary
import com.lordyhas.muzik.ui.theme.TextSecondary

/** Back button, title and Save, at the top of every editor page. */
@Composable
fun EditorTopBar(
    title: String,
    onBack: () -> Unit,
    canSave: Boolean,
    isSaving: Boolean,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(BackgroundDark)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircleIconButton(icon = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", onClick = onBack)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (isSaving) {
            CircularProgressIndicator(color = AccentPrimary, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
        } else {
            Button(
                onClick = onSave,
                enabled = canSave,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentFill,
                    contentColor = OnAccentFill,
                    disabledContainerColor = SurfaceCard,
                    disabledContentColor = TextMuted
                )
            ) { Text("Save", style = MaterialTheme.typography.labelLarge) }
        }
    }
}

/** A text field styled for the dark editor. */
@Composable
fun TagTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    numeric: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it, maxLines = 1, overflow = TextOverflow.Ellipsis) } },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            capitalization = if (numeric) KeyboardCapitalization.None else KeyboardCapitalization.Words,
            keyboardType = if (numeric) KeyboardType.Number else KeyboardType.Text,
            imeAction = ImeAction.Next
        ),
        shape = RoundedCornerShape(12.dp),
        colors = tagFieldColors(),
        modifier = modifier
    )
}

@Composable
fun tagFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedContainerColor = SurfaceCard,
    unfocusedContainerColor = SurfaceCard,
    focusedBorderColor = AccentPrimary,
    unfocusedBorderColor = TextPrimary.copy(alpha = 0.10f),
    focusedLabelColor = AccentPrimary,
    unfocusedLabelColor = TextSecondary,
    cursorColor = AccentPrimary,
    focusedPlaceholderColor = TextMuted,
    unfocusedPlaceholderColor = TextMuted
)

/** Decodes embedded cover bytes once per picture. */
@Composable
fun rememberCoverBitmap(cover: CoverArt?): ImageBitmap? = remember(cover) {
    cover?.let { runCatching { BitmapFactory.decodeByteArray(it.data, 0, it.data.size)?.asImageBitmap() }.getOrNull() }
}

/**
 * The cover being edited: the picture inside the file (or the new one), else what the library
 * shows for it ([fallbackUri]), with Change / Remove buttons.
 */
@Composable
fun CoverEditor(
    cover: ImageBitmap?,
    fallbackUri: Uri?,
    onChange: () -> Unit,
    onRemove: (() -> Unit)?,
    modifier: Modifier = Modifier,
    size: Dp = 168.dp
) {
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        val shape = RoundedCornerShape(18.dp)
        Box(
            modifier = Modifier
                .size(size)
                .clip(shape)
                .border(1.dp, TextPrimary.copy(alpha = 0.08f), shape),
            contentAlignment = Alignment.Center
        ) {
            if (cover != null) {
                Image(bitmap = cover, contentDescription = "Cover", contentScale = ContentScale.Crop, modifier = Modifier.size(size))
            } else {
                Artwork(
                    uri = fallbackUri,
                    shape = shape,
                    placeholderIcon = Icons.Default.Album,
                    iconSize = 64.dp,
                    placeholderTint = AccentSecondary,
                    modifier = Modifier.size(size)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TextButton(onClick = onChange) {
                Icon(Icons.Default.Image, contentDescription = null, tint = AccentPrimary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Change cover", color = AccentPrimary)
            }
            if (onRemove != null) {
                TextButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Remove", color = TextSecondary)
                }
            }
        }
    }
}

/** Small section title inside an editor page. */
@Composable
fun EditorSectionTitle(title: String, modifier: Modifier = Modifier, subtitle: String? = null) {
    Column(modifier = modifier.padding(top = 8.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
        if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
    }
}

/** Asked when leaving an editor with unsaved changes. */
@Composable
fun DiscardChangesDialog(onDiscard: () -> Unit, onKeepEditing: () -> Unit) {
    AlertDialog(
        onDismissRequest = onKeepEditing,
        containerColor = SurfaceDark,
        title = { Text("Discard changes?", color = TextPrimary) },
        text = { Text("Your edits haven't been saved to the files yet.", color = TextSecondary) },
        confirmButton = { TextButton(onClick = onDiscard) { Text("Discard", color = DangerRed) } },
        dismissButton = { TextButton(onClick = onKeepEditing) { Text("Keep editing", color = TextPrimary) } }
    )
}
