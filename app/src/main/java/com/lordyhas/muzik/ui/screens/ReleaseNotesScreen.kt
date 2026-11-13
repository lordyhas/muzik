package com.lordyhas.muzik.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.lordyhas.muzik.data.model.ReleaseNotes
import com.lordyhas.muzik.ui.components.CircleIconButton
import com.lordyhas.muzik.ui.components.EmptyState
import com.lordyhas.muzik.ui.components.HighlightRow
import com.lordyhas.muzik.ui.components.ScreenHeader
import com.lordyhas.muzik.ui.theme.AccentPrimary
import com.lordyhas.muzik.ui.theme.BackgroundDark
import com.lordyhas.muzik.ui.theme.SurfaceCard
import com.lordyhas.muzik.ui.theme.TextMuted
import com.lordyhas.muzik.ui.theme.TextPrimary

/** Every version's highlights, newest first (About > What's new). */
@Composable
fun ReleaseNotesScreen(
    releases: List<ReleaseNotes>,
    currentVersionCode: Long,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
            CircleIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                onClick = onBackClick
            )
        }
        ScreenHeader(title = "What's new", subtitle = null)

        if (releases.isEmpty()) {
            EmptyState(icon = Icons.Default.AutoAwesome, title = "No release notes yet")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
            ) {
                items(releases, key = { it.versionCode }) { release ->
                    ReleaseCard(release, isCurrent = release.versionCode == currentVersionCode)
                }
            }
        }
    }
}

@Composable
private fun ReleaseCard(release: ReleaseNotes, isCurrent: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceCard)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Version ${release.versionName}",
                style = MaterialTheme.typography.titleMedium,
                color = if (isCurrent) AccentPrimary else TextPrimary,
                modifier = Modifier.weight(1f)
            )
            if (isCurrent) {
                Text("Installed", style = MaterialTheme.typography.labelMedium, color = TextMuted)
            }
        }
        release.highlights.forEach { HighlightRow(it) }
    }
}
