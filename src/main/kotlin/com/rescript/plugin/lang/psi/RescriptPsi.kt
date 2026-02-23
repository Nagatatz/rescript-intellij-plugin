package com.rescript.plugin.lang.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.RescriptLanguage

/**
 * Custom [IElementType] subclass for ReScript composite PSI elements.
 *
 * Used to define element types for non-stub declarations, statements, and JSX structures
 * recognized by the lightweight parser. Stub-backed declarations use
 * [RescriptDeclarationElementType] instead.
 */
class RescriptElementType(
    debugName: String,
) : IElementType(debugName, RescriptLanguage)

/**
 * Composite element types used by the lightweight parser.
 *
 * Declaration types (LET, TYPE, MODULE, EXTERNAL, EXCEPTION) delegate to
 * [RescriptStubElementTypes] for stub-aware indexing. Non-declaration types
 * remain plain [RescriptElementType] instances.
 */
object RescriptElementTypes {
    // ── Stub-backed declaration types (delegated to RescriptStubElementTypes) ──
    @JvmField val LET_DECLARATION = RescriptStubElementTypes.LET_DECLARATION

    @JvmField val TYPE_DECLARATION = RescriptStubElementTypes.TYPE_DECLARATION

    @JvmField val MODULE_DECLARATION = RescriptStubElementTypes.MODULE_DECLARATION

    @JvmField val EXTERNAL_DECLARATION = RescriptStubElementTypes.EXTERNAL_DECLARATION

    @JvmField val EXCEPTION_DECLARATION = RescriptStubElementTypes.EXCEPTION_DECLARATION

    // ── Non-stub element types ────────────────────────────────────
    @JvmField val OPEN_STATEMENT = RescriptElementType("OPEN_STATEMENT")

    @JvmField val INCLUDE_STATEMENT = RescriptElementType("INCLUDE_STATEMENT")

    @JvmField val ANNOTATION = RescriptElementType("ANNOTATION")

    // ── JSX elements ───────────────────────────────────────────────
    @JvmField val JSX_ELEMENT = RescriptElementType("JSX_ELEMENT")

    @JvmField val JSX_SELF_CLOSING_ELEMENT = RescriptElementType("JSX_SELF_CLOSING_ELEMENT")

    @JvmField val JSX_FRAGMENT = RescriptElementType("JSX_FRAGMENT")
}

/**
 * PSI file representation for ReScript source and interface files.
 *
 * Serves as the root of the PSI tree for `.res` and `.resi` files.
 */
class RescriptFile(
    viewProvider: FileViewProvider,
) : PsiFileBase(viewProvider, RescriptLanguage) {
    override fun getFileType(): FileType = viewProvider.fileType

    override fun toString(): String = "ReScript File"
}
