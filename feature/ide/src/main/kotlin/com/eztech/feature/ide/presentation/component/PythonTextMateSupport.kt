package com.eztech.feature.ide.presentation.component

import android.content.Context
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver
import io.github.rosemoe.sora.widget.CodeEditor
import org.eclipse.tm4e.core.registry.IThemeSource

internal object PythonTextMateSupport {
    private var initialized = false

    @Synchronized
    fun configure(editor: CodeEditor, context: Context) {
        if (!initialized) {
            val fileProviders = FileProviderRegistry.getInstance()
            fileProviders.addFileProvider(
                AssetsFileResolver(context.applicationContext.assets),
            )

            val themeRegistry = ThemeRegistry.getInstance()
            themeRegistry.loadTheme(
                ThemeModel(
                    IThemeSource.fromInputStream(
                        fileProviders.tryGetInputStream(THEME_PATH),
                        THEME_PATH,
                        null,
                    ),
                    THEME_NAME,
                ).apply {
                    isDark = true
                },
            )
            themeRegistry.setTheme(THEME_NAME)
            GrammarRegistry.getInstance().loadGrammars(LANGUAGES_PATH)
            initialized = true
        }

        editor.colorScheme = TextMateColorScheme.create(ThemeRegistry.getInstance())
        editor.setEditorLanguage(
            TextMateLanguage.create(PYTHON_SCOPE, true),
        )
    }

    private const val PYTHON_SCOPE = "source.python"
    private const val LANGUAGES_PATH = "textmate/languages.json"
    private const val THEME_PATH = "textmate/eztech_dark.json"
    private const val THEME_NAME = "eztech-dark"
}
