package com.eztech.feature.ide.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

private data class QuickInsertKey(
    val label: String,
    val text: String,
    val cursorOffset: Int = text.length,
)

@Composable
fun QuickKeyboard(
    controller: CodeEditorController,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val keys = remember {
        listOf(
            QuickInsertKey("Tab", "    "),
            QuickInsertKey("( )", "()", 1),
            QuickInsertKey("[ ]", "[]", 1),
            QuickInsertKey("{ }", "{}", 1),
            QuickInsertKey(":", ":"),
            QuickInsertKey("\"", "\"\"", 1),
            QuickInsertKey("'", "''", 1),
            QuickInsertKey("#", "# "),
            QuickInsertKey("=", " = "),
            QuickInsertKey("+", " + "),
            QuickInsertKey("-", " - "),
        )
    }

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .height(48.dp)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            IconButton(
                onClick = controller::moveLeft,
                enabled = enabled,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Move cursor left")
            }
            IconButton(
                onClick = controller::moveRight,
                enabled = enabled,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = "Move cursor right")
            }

            keys.forEach { key ->
                Surface(
                    onClick = {
                        controller.insertText(key.text, key.cursorOffset)
                        controller.requestFocus()
                    },
                    enabled = enabled,
                    modifier = Modifier
                        .height(36.dp)
                        .sizeIn(minWidth = 40.dp),
                    shape = MaterialTheme.shapes.extraSmall,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = key.label,
                            style = MaterialTheme.typography.labelLarge,
                            fontFamily = FontFamily.Monospace,
                        )
                    }
                }
            }
        }
    }
}
