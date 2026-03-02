package com.rescript.plugin.codestyle

import com.intellij.lang.Language
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.PredefinedCodeStyle
import com.rescript.plugin.RescriptLanguage

/**
 * Predefined code style preset for ReScript that matches `rescript format` output.
 *
 * Provides a "ReScript Standard" option in Settings > Code Style > "Set from Predefined Style".
 * Sets 2-space indentation with no tabs to match the ReScript formatter defaults.
 *
 * @see RescriptCodeStyleSettingsProvider for the default code style configuration
 */
@Suppress("DialogTitleCapitalization") // "ReScript Standard" is a proper name
class RescriptPredefinedCodeStyle : PredefinedCodeStyle("ReScript Standard", RescriptLanguage) {
    override fun apply(
        settings: CodeStyleSettings,
        language: Language,
    ) {
        val commonSettings = settings.getCommonSettings(language)
        val indentOptions = commonSettings.indentOptions ?: return
        // rescript format uses 2-space indentation consistently
        indentOptions.INDENT_SIZE = 2
        indentOptions.CONTINUATION_INDENT_SIZE = 2
        indentOptions.TAB_SIZE = 2
        indentOptions.USE_TAB_CHARACTER = false
    }
}
