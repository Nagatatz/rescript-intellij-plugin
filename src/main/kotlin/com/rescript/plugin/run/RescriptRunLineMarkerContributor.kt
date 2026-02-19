package com.rescript.plugin.run

import com.intellij.execution.lineMarker.ExecutorAction
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement
import com.rescript.plugin.RescriptFileType
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lang.psi.RescriptElementTypes

/**
 * Adds a run gutter icon to the first top-level declaration in ReScript files.
 *
 * The icon appears only when `rescript.json` exists in the project root,
 * indicating that the file belongs to a ReScript project that can be built.
 */
class RescriptRunLineMarkerContributor : RunLineMarkerContributor() {
    companion object {
        private val DECLARATION_KEYWORDS =
            setOf(
                RescriptTokenTypes.LET,
                RescriptTokenTypes.TYPE,
                RescriptTokenTypes.MODULE,
            )

        private val DECLARATION_ELEMENT_TYPES =
            setOf(
                RescriptElementTypes.LET_DECLARATION,
                RescriptElementTypes.TYPE_DECLARATION,
                RescriptElementTypes.MODULE_DECLARATION,
            )

        /**
         * Determines whether a run gutter icon should be shown for the given element.
         * Returns true only for the keyword token of the first top-level declaration
         * in a `.res` file within a project that has `rescript.json`.
         */
        fun shouldShowMarker(element: PsiElement): Boolean {
            val tokenType = element.node?.elementType ?: return false
            if (tokenType !in DECLARATION_KEYWORDS) return false

            val file = element.containingFile ?: return false
            if (file.fileType != RescriptFileType) return false

            if (!hasRescriptJson(element)) return false

            val parent = element.parent ?: return false
            if (parent.node?.elementType !in DECLARATION_ELEMENT_TYPES) return false

            return isFirstTopLevelDeclaration(parent)
        }

        private fun hasRescriptJson(element: PsiElement): Boolean {
            val basePath = element.project.basePath ?: return false
            val baseDir =
                java.nio.file.Path
                    .of(basePath)
            return java.nio.file.Files
                .exists(baseDir.resolve("rescript.json"))
        }

        private fun isFirstTopLevelDeclaration(declaration: PsiElement): Boolean {
            val file = declaration.containingFile ?: return false
            var child = file.firstChild
            while (child != null) {
                val childType = child.node?.elementType
                if (childType in DECLARATION_ELEMENT_TYPES) {
                    return child === declaration
                }
                child = child.nextSibling
            }
            return false
        }
    }

    override fun getInfo(element: PsiElement): Info? {
        if (!shouldShowMarker(element)) return null

        val actions = ExecutorAction.getActions(0)
        return Info(
            AllIcons.RunConfigurations.TestState.Run,
            actions,
        ) { "Run ReScript Build" }
    }
}
