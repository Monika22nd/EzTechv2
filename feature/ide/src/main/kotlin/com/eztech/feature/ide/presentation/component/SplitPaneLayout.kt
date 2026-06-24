package com.eztech.feature.ide.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun SplitPaneLayout(
    editorContent: @Composable () -> Unit,
    consoleContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    var editorRatio by rememberSaveable { mutableFloatStateOf(0.62f) }

    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val availableHeightPx = with(density) { maxHeight.toPx() }

        Column(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(editorRatio),
            ) {
                editorContent()
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .pointerInput(availableHeightPx) {
                        detectVerticalDragGestures { change, dragAmount ->
                            change.consume()
                            editorRatio = (editorRatio + dragAmount / availableHeightPx)
                                .coerceIn(0.35f, 0.78f)
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Icon(
                    imageVector = Icons.Rounded.DragHandle,
                    contentDescription = "Resize editor and console",
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f - editorRatio),
            ) {
                consoleContent()
            }
        }
    }
}
