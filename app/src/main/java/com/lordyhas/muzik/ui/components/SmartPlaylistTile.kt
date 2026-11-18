package com.lordyhas.muzik.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.lordyhas.muzik.data.model.SmartPlaylist
import com.lordyhas.muzik.data.model.SmartPlaylistType
import com.lordyhas.muzik.ui.theme.AppThemeStore
import com.lordyhas.muzik.ui.theme.contentColorOn

/** Visual identity (icon + gradient) of each smart playlist. */
val SmartPlaylistType.icon: ImageVector
    get() = when (this) {
        SmartPlaylistType.LIKED -> Icons.Default.Favorite
        SmartPlaylistType.MOST_PLAYED -> Icons.Default.Whatshot
        SmartPlaylistType.RECENTLY_ADDED -> Icons.Default.NewReleases
        SmartPlaylistType.REDISCOVER -> Icons.Default.History
    }

val SmartPlaylistType.gradient: List<Color>
    get() = when (this) {
        SmartPlaylistType.LIKED -> AppThemeStore.current.colors.liked
        SmartPlaylistType.MOST_PLAYED -> AppThemeStore.current.colors.mostPlayed
        SmartPlaylistType.RECENTLY_ADDED -> AppThemeStore.current.colors.recentlyAdded
        SmartPlaylistType.REDISCOVER -> AppThemeStore.current.colors.rediscover
    }

/** Gradient square cover with the smart playlist's icon. */
@Composable
fun SmartPlaylistCover(type: SmartPlaylistType, modifier: Modifier = Modifier, iconSize: androidx.compose.ui.unit.Dp = 40.dp) {
    Box(
        modifier = modifier.background(Brush.linearGradient(type.gradient)),
        contentAlignment = Alignment.Center
    ) {
        Icon(type.icon, contentDescription = null, tint = contentColorOn(type.gradient), modifier = Modifier.size(iconSize))
    }
}

@Composable
fun SmartPlaylistTile(
    playlist: SmartPlaylist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(width = 150.dp, height = 110.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    ) {
        SmartPlaylistCover(type = playlist.type, modifier = Modifier.fillMaxSize(), iconSize = 30.dp)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f))))
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {
            Text(playlist.type.title, style = MaterialTheme.typography.titleSmall, color = Color.White)
            Text(
                pluralize(playlist.songs.size, "song"),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}
