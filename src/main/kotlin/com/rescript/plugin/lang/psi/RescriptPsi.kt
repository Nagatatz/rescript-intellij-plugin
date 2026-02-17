package com.rescript.plugin.lang.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.RescriptLanguage

class RescriptElementType(
    debugName: String,
) : IElementType(debugName, RescriptLanguage)

/** Composite element types used by the lightweight parser. */
object RescriptElementTypes {
    @JvmField val LET_DECLARATION = RescriptElementType("LET_DECLARATION")

    @JvmField val TYPE_DECLARATION = RescriptElementType("TYPE_DECLARATION")

    @JvmField val MODULE_DECLARATION = RescriptElementType("MODULE_DECLARATION")

    @JvmField val EXTERNAL_DECLARATION = RescriptElementType("EXTERNAL_DECLARATION")

    @JvmField val OPEN_STATEMENT = RescriptElementType("OPEN_STATEMENT")

    @JvmField val INCLUDE_STATEMENT = RescriptElementType("INCLUDE_STATEMENT")

    @JvmField val EXCEPTION_DECLARATION = RescriptElementType("EXCEPTION_DECLARATION")

    @JvmField val ANNOTATION = RescriptElementType("ANNOTATION")

    // ── JSX elements ───────────────────────────────────────────────
    @JvmField val JSX_ELEMENT = RescriptElementType("JSX_ELEMENT")

    @JvmField val JSX_SELF_CLOSING_ELEMENT = RescriptElementType("JSX_SELF_CLOSING_ELEMENT")

    @JvmField val JSX_FRAGMENT = RescriptElementType("JSX_FRAGMENT")
}

class RescriptFile(
    viewProvider: FileViewProvider,
) : PsiFileBase(viewProvider, RescriptLanguage) {
    override fun getFileType(): FileType = viewProvider.fileType

    override fun toString(): String = "ReScript File"
}
