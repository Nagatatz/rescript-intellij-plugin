package com.rescript.plugin.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.rescript.plugin.lang.psi.RescriptElementTypes
import com.rescript.plugin.lang.psi.RescriptFile
import com.rescript.plugin.util.RescriptFileUtil
import org.eclipse.lsp4j.TextDocumentIdentifier

/**
 * Local inspection that detects when `.res` and `.resi` file declarations are out of sync.
 *
 * Compares top-level declaration names in the implementation file (`.res`) against
 * those in the corresponding interface file (`.resi`). Reports declarations that
 * exist in `.res` but are missing in `.resi`, or vice versa.
 *
 * Provides a quick fix to regenerate the interface file using LSP `createInterface`.
 *
 * @see com.rescript.plugin.navigation.RescriptCreateInterfaceAction
 */
class RescriptSignatureSyncInspection : LocalInspectionTool() {
    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
    ): PsiElementVisitor =
        object : PsiElementVisitor() {
            override fun visitFile(file: PsiFile) {
                if (file !is RescriptFile) return
                val virtualFile = file.virtualFile ?: return

                // Only inspect .res files that have a corresponding .resi
                if (!RescriptFileUtil.isResFile(virtualFile)) return
                val resiFile = RescriptFileUtil.findInterfaceFile(virtualFile) ?: return

                val resDeclarations = collectDeclarationNames(file)
                val resiText =
                    try {
                        String(resiFile.contentsToByteArray())
                    } catch (e: Exception) {
                        LOG.debug("Failed to read interface file: ${resiFile.name}", e)
                        return
                    }
                val resiDeclarations = parseDeclarationNames(resiText)

                // Find declarations in .res that are missing in .resi
                val missingInResi = resDeclarations - resiDeclarations
                if (missingInResi.isEmpty()) return

                // Report on the first element of the file
                val firstChild = file.firstChild ?: return
                val missingList = missingInResi.joinToString(", ")
                holder.registerProblem(
                    firstChild,
                    "Interface file is out of sync: missing declarations: $missingList",
                    RegenerateInterfaceQuickFix(),
                )
            }
        }

    /** Quick fix that regenerates the .resi file via LSP createInterface. */
    private class RegenerateInterfaceQuickFix : LocalQuickFix {
        override fun getFamilyName(): String = "Regenerate interface file"

        override fun applyFix(
            project: Project,
            descriptor: ProblemDescriptor,
        ) {
            val file = descriptor.psiElement?.containingFile?.virtualFile ?: return

            // Delegate to the existing CreateInterface action via LSP
            try {
                val server =
                    com.rescript.plugin.lsp.RescriptLspUtils
                        .getServer(project) ?: return
                val uri =
                    com.rescript.plugin.lsp.RescriptLspUtils
                        .toLspUri(file)

                // Explicit 10s timeout (the platform default). Omitting it emits
                // the synthetic sendRequestSync$default, which 2026.2 relocated to
                // the new LspClient super-interface and no longer resolves here.
                server.sendRequestSync(10_000) { languageServer ->
                    @Suppress("UNCHECKED_CAST")
                    (languageServer as com.rescript.plugin.lsp.RescriptLanguageServer)
                        .createInterface(TextDocumentIdentifier(uri))
                }
            } catch (e: Exception) {
                LOG.debug("Failed to regenerate interface via LSP", e)
            }
        }
    }

    companion object {
        private val LOG = logger<RescriptSignatureSyncInspection>()

        /**
         * Collects top-level declaration names from a PSI file.
         *
         * @param file the ReScript PSI file
         * @return set of declaration names
         */
        internal fun collectDeclarationNames(file: PsiFile): Set<String> {
            val names = mutableSetOf<String>()
            for (child in file.children) {
                val elementType = child.node?.elementType ?: continue
                when (elementType) {
                    RescriptElementTypes.LET_DECLARATION -> extractName(child, "let")?.let { names.add(it) }
                    RescriptElementTypes.TYPE_DECLARATION -> extractName(child, "type")?.let { names.add(it) }
                    RescriptElementTypes.MODULE_DECLARATION -> extractName(child, "module")?.let { names.add(it) }
                    RescriptElementTypes.EXTERNAL_DECLARATION -> extractName(child, "external")?.let { names.add(it) }
                }
            }
            return names
        }

        /** Pattern matching top-level declaration keywords followed by an identifier. */
        private val DECLARATION_PATTERN = Regex("""(?m)^(?:let|type|module|external)\s+(\w+)""")

        /**
         * Parses declaration names from raw text.
         *
         * @param text the file content
         * @return set of declaration names
         */
        internal fun parseDeclarationNames(text: String): Set<String> {
            val names = mutableSetOf<String>()
            for (match in DECLARATION_PATTERN.findAll(text)) {
                names.add(match.groupValues[1])
            }
            return names
        }

        private fun extractName(
            element: PsiElement,
            keyword: String,
        ): String? {
            val text = element.text.trim()
            if (!text.startsWith(keyword)) return null
            val afterKeyword = text.substring(keyword.length)
            if (afterKeyword.isEmpty() || !afterKeyword[0].isWhitespace()) return null
            val trimmed = afterKeyword.trimStart()
            if (trimmed.isEmpty()) return null
            // Extract identifier: word characters (letters, digits, underscore)
            val name = trimmed.takeWhile { it.isLetterOrDigit() || it == '_' }
            return name.ifEmpty { null }
        }
    }
}
