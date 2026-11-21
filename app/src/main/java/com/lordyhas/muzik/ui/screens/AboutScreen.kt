package com.lordyhas.muzik.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.Bug
import com.composables.icons.lucide.CircleFadingArrowUp
import com.composables.icons.lucide.CodeXml
import com.composables.icons.lucide.Lightbulb
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Scale
import com.composables.icons.lucide.Sparkles
import com.lordyhas.muzik.data.AppLinks
import com.lordyhas.muzik.data.AppVersion
import com.lordyhas.muzik.ui.components.CircleIconButton
import com.lordyhas.muzik.ui.components.SettingsGroup
import com.lordyhas.muzik.ui.components.SettingsRow
import com.lordyhas.muzik.ui.components.SettingsRowTrailing
import com.lordyhas.muzik.ui.theme.AccentSecondary
import com.lordyhas.muzik.ui.theme.BackgroundDark
import com.lordyhas.muzik.ui.theme.TextMuted
import com.lordyhas.muzik.ui.theme.TextPrimary
import com.lordyhas.muzik.ui.theme.TextSecondary

@Composable
fun AboutScreen(
    appVersion: AppVersion,
    onBackClick: () -> Unit,
    onLicensesClick: () -> Unit,
    onWhatsNewClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .navigationBarsPadding()
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
            CircleIconButton(
                icon = Lucide.ArrowLeft,
                contentDescription = "Back",
                onClick = onBackClick
            )
        }

        // App identity
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppIcon(modifier = Modifier.size(96.dp))
            Text(
                text = "Muzik",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                modifier = Modifier.padding(top = 14.dp)
            )
            Text(
                text = "Version ${appVersion.label}",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary
            )
            Text(
                text = "Your local music, beautifully played.\nNo ads, no account, no internet needed.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        SettingsGroup(title = null) {
            SettingsRow(
                icon = Lucide.Sparkles,
                title = "What's new",
                subtitle = "See what changed in each version",
                onClick = onWhatsNewClick,
                showDivider = false
            )
        }

        SettingsGroup(title = "Open source") {
            SettingsRow(
                icon = Lucide.CodeXml,
                title = "Source code",
                subtitle = "Muzik is open source on GitHub",
                trailing = SettingsRowTrailing.External,
                onClick = { uriHandler.openUri(AppLinks.SOURCE_CODE) }
            )
            SettingsRow(
                icon = Lucide.Scale,
                title = "License",
                subtitle = "GNU Affero General Public License v3.0",
                trailing = SettingsRowTrailing.External,
                onClick = { uriHandler.openUri(AppLinks.LICENSE) }
            )
            SettingsRow(
                icon = Lucide.BookOpen,
                title = "Open-source libraries",
                subtitle = "The projects Muzik is built with",
                onClick = onLicensesClick,
                showDivider = false
            )
        }

        SettingsGroup(title = "Feedback") {
            SettingsRow(
                icon = Lucide.Bug,
                title = "Report a bug",
                subtitle = "Something not working? Let us know",
                iconTint = AccentSecondary,
                trailing = SettingsRowTrailing.External,
                onClick = { uriHandler.openUri(AppLinks.REPORT_BUG) }
            )
            SettingsRow(
                icon = Lucide.Lightbulb,
                title = "Suggest a feature",
                iconTint = AccentSecondary,
                trailing = SettingsRowTrailing.External,
                onClick = { uriHandler.openUri(AppLinks.SUGGEST_FEATURE) },
                showDivider = false
            )
        }

        SettingsGroup(title = "Updates") {
            SettingsRow(
                icon = Lucide.CircleFadingArrowUp,
                title = "Get the latest version",
                subtitle = "Opens the download page in your browser",
                iconTint = AccentSecondary,
                trailing = SettingsRowTrailing.External,
                onClick = { uriHandler.openUri(AppLinks.RELEASES) },
                showDivider = false
            )
        }
    }
}

/** The real launcher icon (an adaptive icon, which painterResource can't load). */
@Composable
private fun AppIcon(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap = remember(context) {
        context.packageManager.getApplicationIcon(context.packageName).toBitmap(width = 288, height = 288).asImageBitmap()
    }
    Image(bitmap = bitmap, contentDescription = null, modifier = modifier)
}
