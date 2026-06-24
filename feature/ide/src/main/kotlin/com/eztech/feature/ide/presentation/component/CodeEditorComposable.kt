package com.eztech.feature.ide.presentation.component

import android.graphics.Typeface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import io.github.rosemoe.sora.event.ContentChangeEvent
import io.github.rosemoe.sora.widget.CodeEditor

@Composable
fun rememberCodeEditorController(): CodeEditorController = remember {
    CodeEditorController()
}

@Composable
fun CodeEditorComposable(
    code: String,
    fontSizeSp: Float,
    controller: CodeEditorController,
    onCodeChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnCodeChanged = rememberUpdatedState(onCodeChanged)

    AndroidView(
        factory = { context ->
            CodeEditor(context).apply {
                controller.attach(this)
                typefaceText = Typeface.MONOSPACE
                setLineNumberEnabled(true)
                setWordwrap(false)
                setTabWidth(4)
                setTextSize(fontSizeSp)
                PythonTextMateSupport.configure(this, context)
                setText(code)
                subscribeEvent(ContentChangeEvent::class.java) { _, _ ->
                    currentOnCodeChanged.value(text.toString())
                }
            }
        },
        update = { editor ->
            editor.setTextSize(fontSizeSp)
            if (editor.text.toString() != code) {
                editor.setText(code)
            }
        },
        modifier = modifier,
    )

    DisposableEffect(controller) {
        onDispose(controller::release)
    }
}
