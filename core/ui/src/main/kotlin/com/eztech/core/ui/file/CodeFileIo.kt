package com.eztech.core.ui.file

import android.content.ContentResolver
import android.net.Uri
import java.nio.charset.StandardCharsets

/**
 * Reads a selected document as UTF-8 text.
 *
 * Used by both the IDE and Solve screen import buttons. Returning Result keeps file permission and
 * encoding failures out of Compose state logic and lets the caller show a snackbar.
 */
fun ContentResolver.readUtf8Text(uri: Uri): Result<String> = runCatching {
    openInputStream(uri)?.bufferedReader(StandardCharsets.UTF_8)?.use { reader ->
        reader.readText()
    } ?: error("Unable to open selected file.")
}

/**
 * Writes code to a Storage Access Framework document as UTF-8 text.
 *
 * The caller owns the file picker flow; this helper only handles the stream lifecycle and converts
 * failures into Result.failure.
 */
fun ContentResolver.writeUtf8Text(uri: Uri, text: String): Result<Unit> = runCatching {
    openOutputStream(uri, "wt")?.writer(StandardCharsets.UTF_8)?.use { writer ->
        writer.write(text)
    } ?: error("Unable to save file.")
}
