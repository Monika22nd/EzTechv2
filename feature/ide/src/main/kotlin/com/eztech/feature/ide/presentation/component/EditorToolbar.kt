package com.eztech.feature.ide.presentation.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SaveAlt
import androidx.compose.material.icons.rounded.TextIncrease
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.unit.dp

/**
 * Toolbar for the Python editor.
 *
 * Buttons are intentionally icon-based for repeated use: run, undo/redo, import/export, clear,
 * paste, and font-size cycling. The parent screen supplies callbacks so the toolbar stays UI-only.
 */
@Composable
fun EditorToolbar(
    isRunning: Boolean,
    fontSizeSp: Float,
    controller: CodeEditorController,
    onRun: () -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onClear: () -> Unit,
    onFontSizeChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val clipboard = LocalClipboardManager.current

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .height(56.dp)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = "Python 3.11",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 8.dp),
            )

            FilledTonalButton(
                onClick = onRun,
                enabled = !isRunning,
            ) {
                if (isRunning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                }
                Spacer(Modifier.width(6.dp))
                Text(if (isRunning) "Running" else "Run")
            }

            IconButton(onClick = controller::undo, enabled = !isRunning) {
                Icon(Icons.AutoMirrored.Rounded.Undo, contentDescription = "Undo")
            }
            IconButton(onClick = controller::redo, enabled = !isRunning) {
                Icon(Icons.AutoMirrored.Rounded.Redo, contentDescription = "Redo")
            }
            IconButton(onClick = onImport, enabled = !isRunning) {
                Icon(Icons.Rounded.FileOpen, contentDescription = "Import Python file")
            }
            IconButton(onClick = onExport, enabled = !isRunning) {
                Icon(Icons.Rounded.SaveAlt, contentDescription = "Export Python file")
            }
            IconButton(onClick = onClear, enabled = !isRunning) {
                Icon(Icons.Rounded.DeleteSweep, contentDescription = "Clear editor")
            }
            IconButton(
                onClick = {
                    clipboard.getText()?.text?.let(controller::insertText)
                    controller.requestFocus()
                },
                enabled = !isRunning,
            ) {
                Icon(Icons.Rounded.ContentPaste, contentDescription = "Paste")
            }
            IconButton(
                onClick = {
                    val nextSize = if (fontSizeSp >= 20f) 12f else fontSizeSp + 2f
                    onFontSizeChange(nextSize)
                },
            ) {
                Icon(
                    Icons.Rounded.TextIncrease,
                    contentDescription = "Change font size, currently ${fontSizeSp.toInt()}",
                )
            }
            Text(
                text = "${fontSizeSp.toInt()}sp",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 8.dp),
            )
        }
        HorizontalDivider()
    }
}
