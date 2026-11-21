package com.lordyhas.muzik.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.BadgePlus
import com.composables.icons.lucide.Flame
import com.composables.icons.lucide.Heart
import com.composables.icons.lucide.History
import com.composables.icons.lucide.Lucide
import com.lordyhas.muzik.data.model.SmartPlaylist
import com.lordyhas.muzik.data.model.SmartPlaylistType
import com.lordyhas.muzik.ui.theme.AppThemeStore
import com.lordyhas.muzik.ui.theme.SurfaceCard
import com.lordyhas.muzik.ui.theme.contentColorOn

/** Visual identity (icon + gradient) of each smart playlist. */
val SmartPlaylistType.icon: ImageVector
    get() = when (this) {
        SmartPlaylistType.LIKED -> Lucide.HeartFilled
        SmartPlaylistType.MOST_PLAYED -> Lucide.Flame
        SmartPlaylistType.RECENTLY_ADDED -> Lucide.BadgePlus
        SmartPlaylistType.REDISCOVER -> Lucide.History
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

/** Outline icon for the tile's badge and watermark (the liked heart is filled elsewhere). */
private val SmartPlaylistType.outlineIcon: ImageVector
    get() = if (this == SmartPlaylistType.LIKED) Lucide.Heart else icon

/**
 * Smart playlist card: a muted tint of the playlist's hue, a round badge with its icon, the title
 * at the bottom, and a large faded copy of the icon spilling off the bottom-right corner.
 */
@Composable
fun SmartPlaylistTile(
    playlist: SmartPlaylist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hue = playlist.type.gradient.first()
    val background = lerp(SurfaceCard, hue, 0.42f)
    val light = lerp(hue, Color.White, 0.72f)
    Box(
        modifier = modifier
            .height(120.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(background)
            .clickable(onClick = onClick)
    ) {
        Icon(
            imageVector = playlist.type.outlineIcon,
            contentDescription = null,
            tint = light.copy(alpha = 0.18f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 22.dp, y = 26.dp)
                .size(110.dp)
        )
        Box(
            modifier = Modifier
                .padding(14.dp)
                .size(38.dp)
                .clip(CircleShape)
                .background(light),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = playlist.type.outlineIcon,
                contentDescription = null,
                tint = lerp(hue, Color.Black, 0.45f),
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = playlist.type.title,
            style = MaterialTheme.typography.titleMedium,
            color = light,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
        )
    }
}
