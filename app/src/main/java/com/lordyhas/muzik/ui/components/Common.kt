package com.lordyhas.muzik.ui.components

import android.net.Uri
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lordyhas.muzik.ui.theme.AccentPrimary
import com.lordyhas.muzik.ui.theme.BackgroundDark
import com.lordyhas.muzik.ui.theme.StrokeSubtle
import com.lordyhas.muzik.ui.theme.SurfaceCard
import com.lordyhas.muzik.ui.theme.SurfaceElevated
import com.lordyhas.muzik.ui.theme.TextMuted
import com.lordyhas.muzik.ui.theme.TextPrimary
import com.lordyhas.muzik.ui.theme.TextSecondary
import com.lordyhas.muzik.ui.theme.rememberArtworkColor
import kotlinx.coroutines.launch

/** "1 song", "3 songs" */
fun pluralize(count: Int, singular: String, plural: String = singular + "s"): String =
    "$count ${if (count == 1) singular else plural}"

// ─── Artwork ────────────────────────────────────────────────────────────────

/** Album art with a tinted gradient placeholder when the image is missing. */
@Composable
fun Artwork(
    uri: Uri?,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    placeholderIcon: ImageVector = Icons.Default.MusicNote,
    iconSize: Dp = 24.dp,
    placeholderTint: Color = AccentPrimary,
    contentDescription: String? = null
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(SurfaceElevated, placeholderTint.copy(alpha = 0.22f))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = placeholderIcon,
            contentDescription = null,
            tint = placeholderTint.copy(alpha = 0.85f),
            modifier = Modifier.size(iconSize)
        )
        if (uri != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(uri)
                    .crossfade(true)
                    .build(),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }
    }
}

// ─── Buttons ────────────────────────────────────────────────────────────────

@Composable
fun CircleIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    iconSize: Dp = 20.dp,
    tint: Color = TextPrimary,
    background: Color = SurfaceCard
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(background)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

/** Primary "Play" + secondary "Shuffle" buttons used on every collection screen. */
@Composable
fun PlayShuffleButtons(
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = AccentPrimary,
    onAddToQueue: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onPlay,
            modifier = Modifier
                .weight(1f)
                .height(50.dp)
                .shadow(14.dp, CircleShape, spotColor = accent),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.White)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Play", style = MaterialTheme.typography.labelLarge)
        }

        OutlinedButton(
            onClick = onShuffle,
            modifier = Modifier
                .weight(1f)
                .height(50.dp),
            shape = CircleShape,
            border = BorderStroke(1.dp, TextPrimary.copy(alpha = 0.18f)),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White.copy(alpha = 0.06f),
                contentColor = TextPrimary
            )
        ) {
            Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Shuffle", style = MaterialTheme.typography.labelLarge)
        }

        if (onAddToQueue != null) {
            CircleIconButton(
                icon = Icons.AutoMirrored.Filled.QueueMusic,
                contentDescription = "Add all to queue",
                onClick = onAddToQueue,
                size = 50.dp,
                iconSize = 22.dp,
                background = Color.White.copy(alpha = 0.06f),
                modifier = Modifier.border(1.dp, TextPrimary.copy(alpha = 0.18f), CircleShape)
            )
        }
    }
}

// ─── Headers ────────────────────────────────────────────────────────────────

/** Large title used at the top of every tab. */
@Composable
fun ScreenHeader(
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = actions
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        if (actionLabel != null && onAction != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = AccentPrimary,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onAction)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

// ─── Search ─────────────────────────────────────────────────────────────────

@Composable
fun MuzikSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        placeholder = {
            Text(text = placeholder, color = TextMuted, style = MaterialTheme.typography.bodyMedium)
        },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted)
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = TextSecondary)
                }
            }
        },
        singleLine = true,
        shape = CircleShape,
        textStyle = MaterialTheme.typography.bodyMedium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = SurfaceCard,
            unfocusedContainerColor = SurfaceCard,
            disabledContainerColor = SurfaceCard,
            focusedBorderColor = AccentPrimary.copy(alpha = 0.7f),
            unfocusedBorderColor = StrokeSubtle,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = AccentPrimary
        )
    )
}

// ─── States ─────────────────────────────────────────────────────────────────

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = AccentPrimary, strokeWidth = 3.dp)
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    action: (@Composable () -> Unit)? = null
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(AccentPrimary.copy(alpha = 0.22f), Color.Transparent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = AccentPrimary, modifier = Modifier.size(40.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            if (message != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
            if (action != null) {
                Spacer(modifier = Modifier.height(16.dp))
                action()
            }
        }
    }
}

// ─── Detail screens ─────────────────────────────────────────────────────────

/**
 * Shared layout for album / artist / playlist pages: a scrolling list with a hero header
 * and a top bar that fades in (with the title) once the hero scrolls away.
 */
@Composable
fun DetailScaffold(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    content: LazyListScope.() -> Unit
) {
    val showTopBar by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 600 }
    }

    Box(modifier = modifier.fillMaxSize().background(BackgroundDark)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            content = content
        )

        val scope = rememberCoroutineScope()
        AnimatedVisibility(visible = showTopBar, enter = fadeIn(), exit = fadeOut()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    // The bar sits over the list: it must take the taps that land on it (a near
                    // miss on Back used to play the song scrolled underneath). Tapping it
                    // scrolls back to the top instead.
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { scope.launch { listState.animateScrollToItem(0) } }
                    .background(BackgroundDark.copy(alpha = 0.96f))
                    .statusBarsPadding()
                    .height(60.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(52.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // A generous touch area around the round Back button, so a slightly off tap still
        // goes back instead of reaching the list
        Box(
            modifier = Modifier
                .statusBarsPadding()
                .size(72.dp, 60.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBack
                ),
            contentAlignment = Alignment.Center
        ) {
            CircleIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                onClick = onBack,
                background = Color.Black.copy(alpha = 0.35f)
            )
        }
    }
}

/**
 * Hero header: blurred artwork backdrop, large cover, title block and play/shuffle actions.
 */
@Composable
fun DetailHero(
    artworkUri: Uri?,
    eyebrow: String,
    title: String,
    meta: String,
    onPlay: (() -> Unit)?,
    onShuffle: (() -> Unit)?,
    modifier: Modifier = Modifier,
    onAddToQueue: (() -> Unit)? = null,
    subtitle: String? = null,
    onSubtitleClick: (() -> Unit)? = null,
    circular: Boolean = false,
    placeholderIcon: ImageVector = Icons.Default.MusicNote,
    cover: (@Composable (Modifier) -> Unit)? = null,
    extra: (@Composable () -> Unit)? = null
) {
    val accent = rememberArtworkColor(artworkUri)
    val coverShape = if (circular) CircleShape else RoundedCornerShape(20.dp)

    Box(modifier = modifier.fillMaxWidth()) {
        // Backdrop: blurred artwork (API 31+) + accent glow, fading into the background
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(accent.copy(alpha = 0.45f), accent.copy(alpha = 0.12f), BackgroundDark)
                    )
                )
        )
        if (artworkUri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AsyncImage(
                model = artworkUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .blur(60.dp)
                    .alpha(0.35f)
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, BackgroundDark)))
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 56.dp, bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val coverModifier = Modifier
                .size(if (circular) 190.dp else 220.dp)
                .shadow(28.dp, coverShape, spotColor = accent, ambientColor = accent)
                .border(1.dp, StrokeSubtle, coverShape)
            if (cover != null) {
                cover(coverModifier.clip(coverShape))
            } else {
                Artwork(
                    uri = artworkUri,
                    shape = coverShape,
                    placeholderIcon = placeholderIcon,
                    iconSize = 80.dp,
                    placeholderTint = accent,
                    contentDescription = title,
                    modifier = coverModifier
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = eyebrow.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing * 3),
                color = accent
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = if (onSubtitleClick != null) {
                        Modifier
                            .clip(CircleShape)
                            .clickable(onClick = onSubtitleClick)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    } else Modifier
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = meta,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            if (extra != null) {
                Spacer(modifier = Modifier.height(12.dp))
                extra()
            }

            if (onPlay != null && onShuffle != null) {
                Spacer(modifier = Modifier.height(20.dp))
                PlayShuffleButtons(onPlay = onPlay, onShuffle = onShuffle, accent = accent, onAddToQueue = onAddToQueue)
            }
        }
    }
}

@Composable
fun DetailNotFound(
    message: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize().background(BackgroundDark)) {
        EmptyState(
            icon = Icons.Default.MusicNote,
            title = message,
            message = "It may have been removed from this device."
        )
        CircleIconButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            onClick = onBack,
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}
