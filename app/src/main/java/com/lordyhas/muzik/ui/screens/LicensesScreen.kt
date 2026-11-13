package com.lordyhas.muzik.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lordyhas.muzik.R
import com.lordyhas.muzik.ui.components.CircleIconButton
import com.lordyhas.muzik.ui.components.ScreenHeader
import com.lordyhas.muzik.ui.theme.AccentPrimary
import com.lordyhas.muzik.ui.theme.BackgroundDark
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer

/** Licenses of the libraries Muzik ships with, generated at build time by the aboutlibraries plugin. */
@Composable
fun LicensesScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val libraries by produceLibraries(R.raw.aboutlibraries)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                CircleIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    onClick = onBackClick
                )
            }
            ScreenHeader(title = "Open-source libraries", subtitle = null)
        }
        // The library list draws with the theme's surface colors: match the app's background
        MaterialTheme(
            colorScheme = MaterialTheme.colorScheme.copy(
                background = BackgroundDark,
                surface = BackgroundDark,
                primary = AccentPrimary
            )
        ) {
            LibrariesContainer(
                libraries = libraries,
                modifier = Modifier.fillMaxSize().navigationBarsPadding()
            )
        }
    }
}
