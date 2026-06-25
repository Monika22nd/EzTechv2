package com.eztech.feature.ide.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eztech.core.ui.file.readUtf8Text
import com.eztech.core.ui.file.writeUtf8Text
import com.eztech.feature.ide.presentation.component.CodeEditorComposable
import com.eztech.feature.ide.presentation.component.ConsoleOutputView
import com.eztech.feature.ide.presentation.component.EditorToolbar
import com.eztech.feature.ide.presentation.component.QuickKeyboard
import com.eztech.feature.ide.presentation.component.SplitPaneLayout
import com.eztech.feature.ide.presentation.component.rememberCodeEditorController
import kotlinx.coroutines.launch

/**
 * Main in-app Python IDE screen.
 *
 * Besides editing/running code, this screen owns the Android Storage Access Framework launchers for
 * importing and exporting `.py` files. File IO is kept in small helpers so this composable only
 * coordinates picker results, editor state, and snackbars.
 */
@Composable
fun IdeScreen(
    modifier: Modifier = Modifier,
    viewModel: IdeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val editorController = rememberCodeEditorController()
    var fontSizeSp by rememberSaveable { mutableFloatStateOf(14f) }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    /** Displays file import/export feedback without interrupting the editor state. */
    fun showFileMessage(message: String) {
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    /** Opens a text/Python file and replaces the current editor content with its UTF-8 text. */
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        context.contentResolver.readUtf8Text(uri)
            .onSuccess { code ->
                viewModel.onCodeChanged(code)
                editorController.requestFocus()
                showFileMessage("File imported.")
            }
            .onFailure { error ->
                showFileMessage(error.localizedMessage ?: "Unable to import file.")
            }
    }

    /** Creates a document and writes the current editor content into it. */
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain"),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        context.contentResolver.writeUtf8Text(uri, uiState.code)
            .onSuccess { showFileMessage("File exported.") }
            .onFailure { error ->
                showFileMessage(error.localizedMessage ?: "Unable to export file.")
            }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                EditorToolbar(
                    isRunning = uiState.isRunning,
                    fontSizeSp = fontSizeSp,
                    controller = editorController,
                    onRun = viewModel::runCode,
                    onImport = {
                        importLauncher.launch(arrayOf("text/*", "application/octet-stream"))
                    },
                    onExport = {
                        exportLauncher.launch("eztech_code.py")
                    },
                    onClear = viewModel::clearEditor,
                    onFontSizeChange = { fontSizeSp = it },
                )

                SplitPaneLayout(
                    editorContent = {
                        CodeEditorComposable(
                            code = uiState.code,
                            fontSizeSp = fontSizeSp,
                            controller = editorController,
                            onCodeChanged = viewModel::onCodeChanged,
                            modifier = Modifier.fillMaxSize(),
                        )
                    },
                    consoleContent = {
                        ConsoleOutputView(
                            stdin = uiState.stdin,
                            stdout = uiState.stdout,
                            stderr = uiState.stderr,
                            exitCode = uiState.exitCode,
                            executionTimeMs = uiState.executionTimeMs,
                            isRunning = uiState.isRunning,
                            onStdinChanged = viewModel::onStdinChanged,
                            onClear = viewModel::clearConsole,
                            modifier = Modifier.fillMaxSize(),
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                )

                QuickKeyboard(
                    controller = editorController,
                    enabled = !uiState.isRunning,
                )
            }
        }
    }
}
