package com.lordyhas.muzik.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorGroup
import androidx.compose.ui.graphics.vector.VectorPath
import com.composables.icons.lucide.Heart
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pause
import com.composables.icons.lucide.Play
import com.composables.icons.lucide.SkipBack
import com.composables.icons.lucide.SkipForward

/*
 * Lucide icons are outline only. A few places read better filled: the liked heart and the
 * playback controls. These variants keep the Lucide outline and fill its closed shapes.
 */

private val heartFilled by lazy { Lucide.Heart.filled() }
private val playFilled by lazy { Lucide.Play.filled() }
private val pauseFilled by lazy { Lucide.Pause.filled() }
private val skipForwardFilled by lazy { Lucide.SkipForward.filled() }
private val skipBackFilled by lazy { Lucide.SkipBack.filled() }

val Lucide.HeartFilled: ImageVector get() = heartFilled
val Lucide.PlayFilled: ImageVector get() = playFilled
val Lucide.PauseFilled: ImageVector get() = pauseFilled
val Lucide.SkipForwardFilled: ImageVector get() = skipForwardFilled
val Lucide.SkipBackFilled: ImageVector get() = skipBackFilled

/** Copies an outline icon, filling every path with the stroke's color. */
private fun ImageVector.filled(): ImageVector =
    ImageVector.Builder(
        name = "$name-filled",
        defaultWidth = defaultWidth,
        defaultHeight = defaultHeight,
        viewportWidth = viewportWidth,
        viewportHeight = viewportHeight,
    ).apply { addFilled(root) }.build()

private fun ImageVector.Builder.addFilled(group: VectorGroup) {
    group.forEach { node ->
        when (node) {
            is VectorPath -> addPath(
                pathData = node.pathData,
                pathFillType = node.pathFillType,
                fill = node.stroke ?: SolidColor(Color.Black),
                stroke = node.stroke,
                strokeLineWidth = node.strokeLineWidth,
                strokeLineCap = node.strokeLineCap,
                strokeLineJoin = node.strokeLineJoin,
                strokeLineMiter = node.strokeLineMiter,
            )
            is VectorGroup -> {
                addGroup(
                    name = node.name,
                    rotate = node.rotation,
                    pivotX = node.pivotX,
                    pivotY = node.pivotY,
                    scaleX = node.scaleX,
                    scaleY = node.scaleY,
                    translationX = node.translationX,
                    translationY = node.translationY,
                    clipPathData = node.clipPathData,
                )
                addFilled(node)
                clearGroup()
            }
        }
    }
}
