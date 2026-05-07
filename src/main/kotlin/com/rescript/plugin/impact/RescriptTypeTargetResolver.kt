package com.rescript.plugin.impact

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.rescript.plugin.lang.psi.RescriptDeclarationPsiElement
import com.rescript.plugin.lang.psi.RescriptElementTypes
import com.rescript.plugin.lang.psi.RescriptPsiUtils

/**
 * Resolves the [TypeTarget] referred to by the caret position inside
 * a ReScript file, by walking up the PSI tree until a `type`
 * declaration is found.
 *
 * Returns `null` for any caret position not inside a type declaration
 * so the panel can render its placeholder. Module-qualified names are
 * reconstructed by concatenating the names of enclosing
 * [RescriptElementTypes.MODULE_DECLARATION] ancestors.
 */
object RescriptTypeTargetResolver {
    /**
     * Returns the type target enclosing [offset] in [file], or `null`
     * if no `type` declaration contains that offset.
     *
     * @param file the PSI file the caret is in
     * @param offset zero-based caret offset
     */
    fun resolveAt(
        file: PsiFile,
        offset: Int,
    ): TypeTarget? {
        val virtualFile = file.virtualFile ?: return null
        val element = file.findElementAt(offset) ?: return null
        val typeDecl =
            PsiTreeUtil.getParentOfType(element, RescriptDeclarationPsiElement::class.java, false)
                ?: return null
        if (typeDecl.node?.elementType != RescriptElementTypes.TYPE_DECLARATION) return null

        val localName = RescriptPsiUtils.extractName(typeDecl).ifBlank { return null }
        val modulePath = enclosingModulePath(typeDecl)
        val fullName = if (modulePath.isEmpty()) localName else "$modulePath.$localName"

        // Identifier offset: locate the local name's position inside the
        // declaration so the panel can suppress it from the reference list.
        val declarationOffset =
            typeDecl.text.indexOf(localName).let { idx ->
                if (idx < 0) typeDecl.textOffset else typeDecl.textOffset + idx
            }

        return TypeTarget(
            name = fullName,
            localName = localName,
            declarationFile = virtualFile,
            declarationOffset = declarationOffset,
        )
    }

    /**
     * Walks up [start]'s ancestors, collecting module names, and joins
     * them with `.` to produce a dotted path (`Outer.Inner`).
     */
    private fun enclosingModulePath(start: PsiElement): String {
        val parts = mutableListOf<String>()
        var current: PsiElement? = start.parent
        while (current != null) {
            if (current is RescriptDeclarationPsiElement &&
                current.node?.elementType == RescriptElementTypes.MODULE_DECLARATION
            ) {
                val name = RescriptPsiUtils.extractName(current)
                if (name.isNotBlank()) parts.add(0, name)
            }
            current = current.parent
        }
        return parts.joinToString(".")
    }
}
