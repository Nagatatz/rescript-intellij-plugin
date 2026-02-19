package com.rescript.plugin

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

/**
 * File type for ReScript source files (.res).
 *
 * Associates `.res` files with the [RescriptLanguage] for editor support,
 * syntax highlighting, and all other language-aware IDE features.
 */
object RescriptFileType : LanguageFileType(RescriptLanguage) {
    override fun getName(): String = "ReScript"

    override fun getDescription(): String = "ReScript source file"

    override fun getDefaultExtension(): String = "res"

    override fun getIcon(): Icon = RescriptIcons.FILE
}

/**
 * File type for ReScript interface files (.resi).
 *
 * Associates `.resi` files with the [RescriptLanguage]. Interface files define
 * module signatures and are the ReScript equivalent of header/type declaration files.
 */
object RescriptInterfaceFileType : LanguageFileType(RescriptLanguage) {
    override fun getName(): String = "ReScript Interface"

    override fun getDisplayName(): String = "ReScript Interface"

    override fun getDescription(): String = "ReScript interface file"

    override fun getDefaultExtension(): String = "resi"

    override fun getIcon(): Icon = RescriptIcons.INTERFACE_FILE
}
