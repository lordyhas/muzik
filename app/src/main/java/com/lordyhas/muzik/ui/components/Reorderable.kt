package com.lordyhas.muzik.ui.components

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.composables.icons.lucide.GripVertical
import com.composables.icons.lucide.Lucide
import com.lordyhas.muzik.ui.theme.TextMuted
import kotlin.math.roundToInt

/** Tracks which row of a list is being dragged and by how much. */
class ReorderState {
    var draggingKey by mutableStateOf<Any?>(null)
        internal set
    var dragOffset by mutableFloatStateOf(0f)
        internal set
    /** Row just dropped: it is already where it belongs, so lists skip its placement animation. */
    var droppedKey by mutableStateOf<Any?>(null)
        internal set
}

/** Placement animation for reorderable rows, off for the row that was just dropped. */
fun ReorderState.placementSpec(key: Any): FiniteAnimationSpec<IntOffset>? =
    if (droppedKey == key) null else spring(stiffness = Spring.StiffnessMediumLow, visibilityThreshold = IntOffset.VisibilityThreshold)

@Composable
fun rememberReorderState(): ReorderState = remember { ReorderState() }

/**
 * A list row that can be dragged vertically by its handle. The row follows the finger while
 * dragging and [onMove] is called once on release with the number of rows it moved (+down / -up).
 *
 * [content] receives the modifier to put on the drag handle.
 */
@Composable
fun ReorderableRow(
    state: ReorderState,
    key: Any,
    onMove: (steps: Int) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (handleModifier: Modifier, isDragging: Boolean) -> Unit
) {
    var rowHeightPx by remember { mutableIntStateOf(1) }
    val currentOnMove by rememberUpdatedState(onMove)
    val haptics = LocalHapticFeedback.current
    val isDragging = state.draggingKey == key

    val handleModifier = Modifier.pointerInput(key) {
        detectDragGestures(
            onDragStart = {
                state.droppedKey = null
                state.draggingKey = key
                state.dragOffset = 0f
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            },
            onDrag = { change, amount ->
                change.consume()
                state.dragOffset += amount.y
            },
            onDragEnd = {
                val steps = (state.dragOffset / rowHeightPx).roundToInt()
                state.droppedKey = key
                state.draggingKey = null
                state.dragOffset = 0f
                if (steps != 0) currentOnMove(steps)
            },
            onDragCancel = {
                state.draggingKey = null
                state.dragOffset = 0f
            }
        )
    }

    Box(
        modifier = modifier
            .onSizeChanged { rowHeightPx = it.height.coerceAtLeast(1) }
            .zIndex(if (isDragging) 1f else 0f)
            .graphicsLayer {
                translationY = if (isDragging) state.dragOffset else 0f
                val scale = if (isDragging) 1.03f else 1f
                scaleX = scale
                scaleY = scale
                shadowElevation = if (isDragging) 24f else 0f
            }
    ) {
        content(handleModifier, isDragging)
    }
}

/** Standard grip icon used as a drag handle. */
@Composable
fun DragHandleIcon(modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(36.dp), contentAlignment = Alignment.Center) {
        Icon(
            imageVector = Lucide.GripVertical,
            contentDescription = "Drag to reorder",
            tint = TextMuted,
            modifier = Modifier.size(22.dp)
        )
    }
}
