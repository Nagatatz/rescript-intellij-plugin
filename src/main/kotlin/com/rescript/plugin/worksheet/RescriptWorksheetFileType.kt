package com.rescript.plugin.worksheet

import com.intellij.openapi.fileTypes.LanguageFileType
import com.rescript.plugin.RescriptLanguage
import javax.swing.Icon

/**
 * File type for ReScript worksheet files (`.resw` extension).
 *
 * Worksheets reuse the ReScript language definition for syntax highlighting
 * and parsing, and surface doc-comment evaluation results as inline hints.
 * Per-expression evaluation of the whole file is not yet wired up (see
 * [RescriptWorksheetRunner], currently a placeholder).
 *
 * @see LanguageFileType
 * @see RescriptWorksheetRunner for the (unimplemented) execution logic
 */
class RescriptWorksheetFileType private constructor() : LanguageFileType(RescriptLanguage) {
    override fun getName(): String = "ReScript Worksheet"

    override fun getDisplayName(): String = "ReScript Worksheet"

    override fun getDescription(): String = "ReScript worksheet file"

    override fun getDefaultExtension(): String = "resw"

    override fun getIcon(): Icon = com.rescript.plugin.RescriptIcons.FILE

    companion object {
        @Suppress("unused") // Standard FileType singleton pattern
        @JvmStatic
        val INSTANCE = RescriptWorksheetFileType()
    }
}
