package com.lordyhas.muzik.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.lordyhas.muzik.ui.theme.AccentPrimary
import com.lordyhas.muzik.ui.theme.SurfaceElevated
import com.lordyhas.muzik.ui.theme.TextPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

/** A short confirmation at the bottom of the screen, optionally with one action such as Undo. */
data class UiMessage(
    val text: String,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null
)

/** How long a plain confirmation ("Added to queue") stays on screen. */
private const val TOAST_DURATION_MS = 1_200L

/**
 * Shows [messages] one at a time. A new message replaces the one on screen right away, so quick
 * repeated actions (swiping several songs) never queue up stale confirmations.
 *
 * Plain confirmations are a small pill that lets touches through and fades out after about a
 * second, so it never covers the next song you want to swipe. Messages with an action (Undo)
 * stay a snackbar, which has to be tappable and last long enough to reach.
 */
@Composable
fun MuzikSnackbarHost(messages: Flow<UiMessage>, modifier: Modifier = Modifier) {
    val state = remember { SnackbarHostState() }
    var toast by remember { mutableStateOf<String?>(null) }
    // Keeps the last text while the pill fades out
    var lastToast by remember { mutableStateOf("") }

    LaunchedEffect(messages) {
        messages.collectLatest { message ->
            if (message.actionLabel == null) {
                state.currentSnackbarData?.dismiss()
                lastToast = message.text
                toast = message.text
                // A newer message cancels this wait and replaces the text, restarting the timer
                delay(TOAST_DURATION_MS)
                toast = null
            } else {
                toast = null
                val result = state.showSnackbar(
                    message = message.text,
                    actionLabel = message.actionLabel,
                    duration = SnackbarDuration.Long
                )
                if (result == SnackbarResult.ActionPerformed) message.onAction?.invoke()
            }
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.BottomCenter) {
        SnackbarHost(hostState = state) { data ->
            Snackbar(
                snackbarData = data,
                shape = RoundedCornerShape(14.dp),
                containerColor = SurfaceElevated,
                contentColor = TextPrimary,
                actionColor = AccentPrimary
            )
        }

        AnimatedVisibility(
            visible = toast != null,
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut(),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            // No click or pointer modifier: taps go straight through to the list underneath
            Row(
                modifier = Modifier
                    .background(SurfaceElevated.copy(alpha = 0.94f), CircleShape)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .semantics { liveRegion = LiveRegionMode.Polite },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = AccentPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = toast ?: lastToast,
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary
                )
            }
        }
    }
}
