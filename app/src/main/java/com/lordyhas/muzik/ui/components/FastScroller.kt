package com.lordyhas.muzik.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lordyhas.muzik.ui.theme.OnAccentFill
import com.lordyhas.muzik.ui.theme.AccentFill
import com.lordyhas.muzik.ui.theme.AccentPrimary
import com.lordyhas.muzik.ui.theme.TextMuted
import com.lordyhas.muzik.ui.theme.TextPrimary
import kotlinx.coroutines.launch

/**
 * Vertical A–Z strip shown on the edge of long alphabetical lists. Tap or drag over a letter to
 * jump to the first item starting with it; a bubble previews the current letter.
 *
 * @param letters section letter of every list item, in list order (see `indexLetter`)
 */
@Composable
fun AlphabetFastScroller(
    letters: List<String>,
    onScrollToIndex: suspend (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val sections = remember(letters) {
        val firstIndex = LinkedHashMap<String, Int>()
        letters.forEachIndexed { index, letter -> firstIndex.putIfAbsent(letter, index) }
        firstIndex.entries.map { it.key to it.value }
    }
    if (sections.size < 2) return

    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    var heightPx by remember { mutableIntStateOf(1) }
    var activeLetter by remember { mutableStateOf<String?>(null) }

    fun select(y: Float) {
        val slot = ((y / heightPx) * sections.size).toInt().coerceIn(0, sections.lastIndex)
        val (letter, index) = sections[slot]
        if (letter != activeLetter) {
            activeLetter = letter
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            scope.launch { onScrollToIndex(index) }
        }
    }

    Row(modifier = modifier.fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {
        AnimatedVisibility(visible = activeLetter != null, enter = fadeIn(), exit = fadeOut()) {
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(56.dp)
                    .clip(RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp, topEnd = 28.dp))
                    .background(AccentFill),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = activeLetter.orEmpty(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = OnAccentFill
                )
            }
        }

        Column(
            modifier = Modifier
                .width(22.dp)
                .fillMaxHeight()
                .padding(vertical = 8.dp)
                .clip(CircleShape)
                .background(if (activeLetter != null) TextPrimary.copy(alpha = 0.08f) else androidx.compose.ui.graphics.Color.Transparent)
                .onSizeChanged { heightPx = it.height.coerceAtLeast(1) }
                .pointerInput(sections) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        down.consume()
                        select(down.position.y)
                        while (true) {
                            val change = awaitPointerEvent().changes.firstOrNull() ?: break
                            if (!change.pressed) break
                            change.consume()
                            select(change.position.y)
                        }
                        activeLetter = null
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            sections.forEach { (letter, _) ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = letter,
                        fontSize = 10.sp,
                        fontWeight = if (letter == activeLetter) FontWeight.Black else FontWeight.SemiBold,
                        color = if (letter == activeLetter) AccentPrimary else TextMuted
                    )
                }
            }
        }
    }
}
