package com.rescript.plugin.codevision

import com.intellij.codeInsight.codeVision.CodeVisionAnchorKind
import com.intellij.codeInsight.codeVision.CodeVisionEntry
import com.intellij.codeInsight.codeVision.CodeVisionRelativeOrdering
import com.intellij.codeInsight.codeVision.ui.model.TextCodeVisionEntry
import com.intellij.codeInsight.hints.codeVision.DaemonBoundCodeVisionProvider
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.platform.lsp.api.LspServerManager
import com.intellij.platform.lsp.api.LspServerState
import com.intellij.psi.PsiFile
import com.rescript.plugin.lsp.RescriptLspServerSupportProvider
import org.eclipse.lsp4j.CodeLensParams
import org.eclipse.lsp4j.TextDocumentIdentifier

/**
 * Displays inferred type signatures above function definitions using
 * the IntelliJ CodeVision API, backed by the LSP `textDocument/codeLens` request.
 *
 * The rescript-language-server returns type annotations as Code Lens entries
 * with `command.title` containing the type string and `command.command` empty
 * (display-only, no click action).
 *
 * Code Lenses are not shown for `.resi` files since interface files contain
 * explicit type declarations.
 */
class RescriptCodeVisionProvider : DaemonBoundCodeVisionProvider {
    companion object {
        const val ID = "rescript.codeLens"
    }

    override val id: String = ID
    override val name: String = "ReScript Type Annotations"
    override val defaultAnchor: CodeVisionAnchorKind = CodeVisionAnchorKind.Top
    override val relativeOrderings: List<CodeVisionRelativeOrdering> = emptyList()

    override fun computeForEditor(
        editor: Editor,
        file: PsiFile,
    ): List<Pair<TextRange, CodeVisionEntry>> {
        val virtualFile = file.virtualFile ?: return emptyList()
        if (virtualFile.extension == "resi") return emptyList()

        val project = file.project
        val document = editor.document

        val servers =
            LspServerManager
                .getInstance(project)
                .getServersForProvider(RescriptLspServerSupportProvider::class.java)

        val server = servers.firstOrNull { it.state == LspServerState.Running } ?: return emptyList()

        val fileUri = server.descriptor.getFileUri(virtualFile)
        val params = CodeLensParams(TextDocumentIdentifier(fileUri))

        val codeLenses =
            try {
                server.sendRequestSync { languageServer ->
                    languageServer.textDocumentService.codeLens(params)
                }
            } catch (_: Exception) {
                return emptyList()
            }

        return codeLenses?.mapNotNull { codeLens ->
            val range = codeLens.range ?: return@mapNotNull null
            val title = codeLens.command?.title ?: return@mapNotNull null

            val startLine = range.start.line
            if (startLine < 0 || startLine >= document.lineCount) return@mapNotNull null

            val lineStartOffset = document.getLineStartOffset(startLine)
            val lineEndOffset = document.getLineEndOffset(startLine)
            val textRange = TextRange(lineStartOffset, lineEndOffset)

            val entry =
                TextCodeVisionEntry(
                    title,
                    id,
                    null,
                    title,
                    title,
                    emptyList(),
                )

            textRange to entry
        } ?: emptyList()
    }
}
