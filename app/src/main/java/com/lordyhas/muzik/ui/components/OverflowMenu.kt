package com.lordyhas.muzik.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.EllipsisVertical
import com.composables.icons.lucide.Lucide
import com.lordyhas.muzik.ui.theme.SurfaceElevated
import com.lordyhas.muzik.ui.theme.TextPrimary
import com.lordyhas.muzik.ui.theme.TextSecondary

/** One entry of an [OverflowMenu]. */
class MenuAction(
    val label: String,
    val icon: ImageVector,
    val tint: Color = TextSecondary,
    val onClick: () -> Unit
)

/** Round ⋮ button that opens a menu of [actions], for the top-right corner of detail pages. */
@Composable
fun OverflowMenu(
    actions: List<MenuAction>,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        CircleIconButton(
            icon = Lucide.EllipsisVertical,
            contentDescription = contentDescription,
            onClick = { expanded = true },
            background = Color.Black.copy(alpha = 0.35f)
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(SurfaceElevated)
        ) {
            actions.forEach { action ->
                DropdownMenuItem(
                    text = { Text(action.label, color = TextPrimary, style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = { Icon(action.icon, contentDescription = null, tint = action.tint, modifier = Modifier.size(20.dp)) },
                    onClick = {
                        expanded = false
                        action.onClick()
                    }
                )
            }
        }
    }
}
