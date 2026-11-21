package com.lordyhas.muzik.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.lordyhas.muzik.ui.theme.TextPrimary

/** Where the visible part of a horizontal row sits, as fractions of the whole row. */
private data class ScrollWindow(val start: Float, val size: Float)

private fun scrollWindow(first: Int, firstOffset: Int, firstSize: Int, visible: Int, total: Int): ScrollWindow? {
    if (total == 0 || visible >= total) return null
    val firstFraction = if (firstSize > 0) firstOffset.toFloat() / firstSize else 0f
    val size = visible.toFloat() / total
    val start = ((first + firstFraction) / total).coerceIn(0f, 1f - size)
    return ScrollWindow(start, size)
}

/**
 * Short track under a horizontal row with a thumb showing which part is on screen, so it is
 * clear the row scrolls sideways. Hidden when everything already fits.
 */
@Composable
fun HorizontalScrollIndicator(state: LazyListState, modifier: Modifier = Modifier) {
    val window by remember(state) {
        derivedStateOf {
            val info = state.layoutInfo
            // Only items fully on screen count as visible, so a peeking card leaves room to scroll
            val fully = info.visibleItemsInfo.count { it.offset >= info.viewportStartOffset && it.offset + it.size <= info.viewportEndOffset }
            scrollWindow(
                first = state.firstVisibleItemIndex,
                firstOffset = state.firstVisibleItemScrollOffset,
                firstSize = info.visibleItemsInfo.firstOrNull()?.size ?: 0,
                visible = fully.coerceAtLeast(1),
                total = info.totalItemsCount
            )
        }
    }
    window?.let { ScrollTrack(it, modifier) }
}

/** Same as the [LazyListState] version, for a horizontal grid of [rows] rows. */
@Composable
fun HorizontalScrollIndicator(state: LazyGridState, rows: Int, modifier: Modifier = Modifier) {
    val window by remember(state, rows) {
        derivedStateOf {
            val info = state.layoutInfo
            val columns = (info.totalItemsCount + rows - 1) / rows
            val fullyVisibleColumns = info.visibleItemsInfo
                .filter { it.offset.x >= info.viewportStartOffset && it.offset.x + it.size.width <= info.viewportEndOffset }
                .map { it.column }
                .distinct()
                .size
            scrollWindow(
                first = state.firstVisibleItemIndex / rows,
                firstOffset = state.firstVisibleItemScrollOffset,
                firstSize = info.visibleItemsInfo.firstOrNull()?.size?.width ?: 0,
                visible = fullyVisibleColumns.coerceAtLeast(1),
                total = columns
            )
        }
    }
    window?.let { ScrollTrack(it, modifier) }
}

@Composable
private fun ScrollTrack(window: ScrollWindow, modifier: Modifier) {
    val trackWidth = 56.dp
    Box(modifier = modifier.fillMaxWidth().padding(top = 6.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .width(trackWidth)
                .height(4.dp)
                .clip(CircleShape)
                .background(TextPrimary.copy(alpha = 0.14f))
        ) {
            Box(
                modifier = Modifier
                    .offset(x = trackWidth * window.start)
                    .width(trackWidth * window.size)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(TextPrimary.copy(alpha = 0.6f))
            )
        }
    }
}
