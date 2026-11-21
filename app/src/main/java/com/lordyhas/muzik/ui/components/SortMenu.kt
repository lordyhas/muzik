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
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ArrowDownWideNarrow
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import com.lordyhas.muzik.ui.theme.AccentPrimary
import com.lordyhas.muzik.ui.theme.SurfaceElevated
import com.lordyhas.muzik.ui.theme.TextMuted
import com.lordyhas.muzik.ui.theme.TextPrimary
import com.lordyhas.muzik.ui.theme.TextSecondary

/** Header button that opens a "Sort by" menu with a check next to the current order. */
@Composable
fun <T> SortMenuButton(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        CircleIconButton(
            icon = Lucide.ArrowDownWideNarrow,
            contentDescription = "Sort by ${label(selected)}",
            onClick = { expanded = true },
            tint = TextSecondary
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(SurfaceElevated)
        ) {
            DropdownMenuItem(
                text = { Text("Sort by", style = MaterialTheme.typography.labelMedium, color = TextMuted) },
                onClick = {},
                enabled = false
            )
            options.forEach { option ->
                val isSelected = option == selected
                DropdownMenuItem(
                    text = {
                        Text(
                            label(option),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSelected) AccentPrimary else TextPrimary
                        )
                    },
                    trailingIcon = {
                        if (isSelected) {
                            Icon(Lucide.Check, contentDescription = null, tint = AccentPrimary, modifier = Modifier.size(18.dp))
                        }
                    },
                    onClick = {
                        expanded = false
                        onSelect(option)
                    }
                )
            }
        }
    }
}
