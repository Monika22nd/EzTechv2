package com.eztech.feature.ide.presentation.component

import androidx.compose.runtime.Stable
import io.github.rosemoe.sora.widget.CodeEditor

@Stable
class CodeEditorController {
    private var editor: CodeEditor? = null

    internal fun attach(editor: CodeEditor) {
        this.editor = editor
    }

    internal fun release() {
        editor?.release()
        editor = null
    }

    fun undo() {
        editor?.undo()
    }

    fun redo() {
        editor?.redo()
    }

    fun insertText(text: String, selectionOffset: Int = text.length) {
        editor?.insertText(text, selectionOffset)
    }

    fun moveLeft() {
        editor?.moveSelectionLeft()
    }

    fun moveRight() {
        editor?.moveSelectionRight()
    }

    fun requestFocus() {
        editor?.requestFocus()
    }
}
