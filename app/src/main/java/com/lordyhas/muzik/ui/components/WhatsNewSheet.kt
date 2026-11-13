package com.lordyhas.muzik.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.lordyhas.muzik.data.model.ReleaseNotes
import com.lordyhas.muzik.ui.theme.AccentCyan
import com.lordyhas.muzik.ui.theme.AccentPrimary
import com.lordyhas.muzik.ui.theme.AccentSecondary
import com.lordyhas.muzik.ui.theme.SurfaceDark
import com.lordyhas.muzik.ui.theme.TextPrimary
import com.lordyhas.muzik.ui.theme.TextSecondary

/** Shown once after an update: the highlights of every version the user hasn't seen yet. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsNewSheet(
    releases: List<ReleaseNotes>,
    onSeeAll: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val latest = releases.firstOrNull() ?: return

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceDark,
        dragHandle = { SheetDragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = "What's new in Muzik",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
            Text(
                text = "Version ${latest.versionName}",
                style = MaterialTheme.typography.bodyMedium,
                color = AccentPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                releases.flatMap { it.highlights }.forEach { HighlightRow(it) }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary, contentColor = Color.White)
            ) {
                Text("Got it", style = MaterialTheme.typography.labelLarge)
            }
            TextButton(
                onClick = onSeeAll,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("See all updates", color = TextSecondary)
            }
        }
    }
}

/** One highlight: a tinted icon for its type, a title and a sentence. */
@Composable
fun HighlightRow(highlight: ReleaseNotes.Highlight, modifier: Modifier = Modifier) {
    val (icon, tint) = highlight.type.visual()
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = highlight.type.label, tint = tint, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(highlight.title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
            Text(highlight.text, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
    }
}

private fun ReleaseNotes.Type.visual(): Pair<ImageVector, Color> = when (this) {
    ReleaseNotes.Type.NEW -> Icons.Default.AutoAwesome to AccentPrimary
    ReleaseNotes.Type.IMPROVED -> Icons.AutoMirrored.Filled.TrendingUp to AccentSecondary
    ReleaseNotes.Type.FIXED -> Icons.Default.Build to AccentCyan
}
