package com.rescript.plugin.worksheet

import com.intellij.openapi.fileTypes.LanguageFileType
import com.rescript.plugin.RescriptLanguage
import javax.swing.Icon

/**
 * File type for ReScript worksheet files (`.resw` extension).
 *
 * Worksheets are interactive files where each top-level expression is
 * evaluated and the result is displayed inline. They reuse the ReScript
 * language definition for syntax highlighting and parsing.
 *
 * @see LanguageFileType
 * @see RescriptWorksheetRunner for execution logic
 */
class RescriptWorksheetFileType private constructor() : LanguageFileType(RescriptLanguage) {
    override fun getName(): String = "ReScript Worksheet"

    override fun getDescription(): String = "ReScript worksheet file"

    override fun getDefaultExtension(): String = "resw"

    override fun getIcon(): Icon = com.rescript.plugin.RescriptIcons.FILE

    companion object {
        @Suppress("unused") // Standard FileType singleton pattern
        @JvmStatic
        val INSTANCE = RescriptWorksheetFileType()
    }
}
