package com.eztech.feature.ide.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

private val ConsoleBackground = Color(0xFF111318)
private val ConsoleText = Color(0xFFDCE2EA)
private val ConsoleMuted = Color(0xFF9AA4B2)
private val ConsoleSuccess = Color(0xFF62D394)
private val ConsoleError = Color(0xFFFF8A8A)

@Composable
fun ConsoleOutputView(
    stdin: String,
    stdout: String,
    stderr: String,
    exitCode: Int?,
    executionTimeMs: Long?,
    isRunning: Boolean,
    onStdinChanged: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.maxValue }.collect { maximum ->
            scrollState.scrollTo(maximum)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ConsoleBackground),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 4.dp, top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Icons.Rounded.Terminal,
                    contentDescription = null,
                    tint = ConsoleMuted,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Console",
                    color = ConsoleText,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            IconButton(onClick = onClear, enabled = !isRunning) {
                Icon(
                    Icons.Rounded.DeleteOutline,
                    contentDescription = "Clear console",
                    tint = ConsoleMuted,
                )
            }
        }

        OutlinedTextField(
            value = stdin,
            onValueChange = onStdinChanged,
            enabled = !isRunning,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 4.dp),
            label = { Text("Program input (one value per line)") },
            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            minLines = 1,
            maxLines = 2,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = ConsoleText,
                unfocusedTextColor = ConsoleText,
                disabledTextColor = ConsoleMuted,
                cursorColor = ConsoleSuccess,
                focusedBorderColor = ConsoleSuccess,
                unfocusedBorderColor = ConsoleMuted,
                focusedLabelColor = ConsoleSuccess,
                unfocusedLabelColor = ConsoleMuted,
            ),
        )

        HorizontalDivider(color = Color(0xFF303640))

        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(12.dp),
        ) {
            SelectionContainer {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    when {
                        isRunning -> Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = ConsoleSuccess,
                            )
                            Text("Running Python...", color = ConsoleMuted)
                        }

                        stdout.isBlank() && stderr.isBlank() && exitCode == null ->
                            Text("Output will appear here.", color = ConsoleMuted)
                    }

                    if (stdout.isNotEmpty()) {
                        Text(
                            text = stdout,
                            color = ConsoleText,
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    if (stderr.isNotEmpty()) {
                        Text(
                            text = stderr,
                            color = ConsoleError,
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    if (!isRunning && exitCode != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            val success = exitCode == 0
                            Icon(
                                imageVector = if (success) {
                                    Icons.Rounded.CheckCircle
                                } else {
                                    Icons.Rounded.ErrorOutline
                                },
                                contentDescription = null,
                                tint = if (success) ConsoleSuccess else ConsoleError,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = buildString {
                                    append("Exit code ")
                                    append(exitCode)
                                    executionTimeMs?.let {
                                        append("  |  ")
                                        append(it)
                                        append(" ms")
                                    }
                                },
                                color = if (success) ConsoleSuccess else ConsoleError,
                                fontFamily = FontFamily.Monospace,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                }
            }
        }
    }
}
