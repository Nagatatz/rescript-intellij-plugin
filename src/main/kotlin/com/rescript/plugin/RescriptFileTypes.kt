package com.rescript.plugin

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

object RescriptFileType : LanguageFileType(RescriptLanguage) {
    override fun getName(): String = "ReScript"

    override fun getDescription(): String = "ReScript source file"

    override fun getDefaultExtension(): String = "res"

    override fun getIcon(): Icon = RescriptIcons.FILE
}

object RescriptInterfaceFileType : LanguageFileType(RescriptLanguage) {
    override fun getName(): String = "ReScript Interface"

    override fun getDisplayName(): String = "ReScript Interface"

    override fun getDescription(): String = "ReScript interface file"

    override fun getDefaultExtension(): String = "resi"

    override fun getIcon(): Icon = RescriptIcons.INTERFACE_FILE
}
