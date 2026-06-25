package com.eztech.core.ui.file

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext

data class CodeFileActions(
    val importCode: () -> Unit,
    val exportCode: (fileName: String) -> Unit,
)

@Composable
fun rememberCodeFileActions(
    code: String,
    onCodeImported: (String) -> Unit,
    onImportSuccess: () -> Unit = {},
    onMessage: (String) -> Unit,
): CodeFileActions {
    val context = LocalContext.current
    val latestCode by rememberUpdatedState(code)
    val latestOnCodeImported by rememberUpdatedState(onCodeImported)
    val latestOnImportSuccess by rememberUpdatedState(onImportSuccess)
    val latestOnMessage by rememberUpdatedState(onMessage)

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        context.contentResolver.readUtf8Text(uri)
            .onSuccess { importedCode ->
                latestOnCodeImported(importedCode)
                latestOnImportSuccess()
                latestOnMessage("File imported.")
            }
            .onFailure { error ->
                latestOnMessage(error.localizedMessage ?: "Unable to import file.")
            }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain"),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        context.contentResolver.writeUtf8Text(uri, latestCode)
            .onSuccess { latestOnMessage("File exported.") }
            .onFailure { error ->
                latestOnMessage(error.localizedMessage ?: "Unable to export file.")
            }
    }

    return remember(importLauncher, exportLauncher) {
        CodeFileActions(
            importCode = { importLauncher.launch(CODE_IMPORT_MIME_TYPES) },
            exportCode = { fileName -> exportLauncher.launch(fileName) },
        )
    }
}

private val CODE_IMPORT_MIME_TYPES = arrayOf("text/*", "application/octet-stream")
