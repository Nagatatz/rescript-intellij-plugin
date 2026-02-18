package com.rescript.plugin.navigation

import com.intellij.ide.actions.QualifiedNameProvider
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.rescript.plugin.lang.psi.RescriptElementTypes
import com.rescript.plugin.lang.psi.RescriptFile
import com.rescript.plugin.lang.psi.RescriptPsiUtils

class RescriptQualifiedNameProvider : QualifiedNameProvider {
    companion object {
        internal val SUPPORTED_TYPES = RescriptPsiUtils.NAVIGABLE_TYPES
    }

    override fun adjustElementToCopy(element: PsiElement): PsiElement? = findDeclarationElement(element)

    override fun getQualifiedName(element: PsiElement): String? {
        val declaration = findDeclarationElement(element) ?: return null
        val file = declaration.containingFile as? RescriptFile ?: return null

        val fileName = file.name.substringBeforeLast(".")
        val rootModule = fileName.replaceFirstChar { it.uppercaseChar() }

        val name = RescriptPsiUtils.extractName(declaration)
        if (name == "(unknown)" || name == "(anonymous)") return null

        val modulePath = buildModulePath(declaration)

        return if (modulePath.isEmpty()) {
            "$rootModule.$name"
        } else {
            "$rootModule.$modulePath.$name"
        }
    }

    override fun qualifiedNameToElement(
        fqn: String,
        project: Project,
    ): PsiElement? = null

    internal fun findDeclarationElement(element: PsiElement): PsiElement? {
        if (element.node?.elementType in SUPPORTED_TYPES) return element
        return PsiTreeUtil.findFirstParent(element, false) { parent ->
            parent.node?.elementType in SUPPORTED_TYPES
        }
    }

    internal fun buildModulePath(element: PsiElement): String {
        val parts = mutableListOf<String>()
        var current = element.parent
        while (current != null && current !is RescriptFile) {
            if (current.node?.elementType == RescriptElementTypes.MODULE_DECLARATION) {
                val name = RescriptPsiUtils.extractName(current)
                if (name != "(unknown)" && name != "(anonymous)") {
                    parts.add(0, name)
                }
            }
            current = current.parent
        }
        return parts.joinToString(".")
    }
}
