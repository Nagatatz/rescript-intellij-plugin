package com.rescript.plugin.refactor

import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.platform.lsp.api.LspServer
import com.intellij.platform.lsp.api.LspServerManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.rename.RenameHandler
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.PrepareRenameParams
import org.eclipse.lsp4j.RenameParams
import org.eclipse.lsp4j.TextEdit
import org.eclipse.lsp4j.WorkspaceEdit
import java.net.URI

/**
 * Handles rename refactoring for ReScript files via LSP textDocument/rename.
 *
 * Intercepts Shift+F6 on .res/.resi files, sends the rename request
 * to the language server, and applies the returned WorkspaceEdit.
 */
class RescriptRenameHandler : RenameHandler {
    override fun isAvailableOnDataContext(dataContext: DataContext): Boolean {
        val file = CommonDataKeys.PSI_FILE.getData(dataContext) ?: return false
        val editor = CommonDataKeys.EDITOR.getData(dataContext) ?: return false
        val extension = file.virtualFile?.extension ?: return false
        if (extension !in RESCRIPT_EXTENSIONS) return false

        // Check that cursor is on an identifier-like character
        val offset = editor.caretModel.offset
        val text = editor.document.text
        if (offset >= text.length && (offset == 0 || !isIdentifierChar(text[offset - 1]))) return false
        val onIdent =
            (offset < text.length && isIdentifierChar(text[offset])) ||
                (offset > 0 && isIdentifierChar(text[offset - 1]))
        return onIdent
    }

    override fun isRenaming(dataContext: DataContext): Boolean = isAvailableOnDataContext(dataContext)

    override fun invoke(
        project: Project,
        editor: Editor?,
        file: PsiFile?,
        dataContext: DataContext?,
    ) {
        if (editor == null || file == null) return
        val virtualFile = file.virtualFile ?: return

        val lspServer = findLspServer(project)
        if (lspServer == null) {
            Messages.showErrorDialog(
                project,
                "ReScript Language Server is not running.\nMake sure @rescript/language-server is installed.",
                "Rename Unavailable",
            )
            return
        }

        val offset = editor.caretModel.offset
        val position = offsetToPosition(editor, offset)
        val textDocId = lspServer.getDocumentIdentifier(virtualFile)

        // Try prepareRename first to check feasibility and get current name
        val currentName = prepareRename(lspServer, textDocId, position, editor, offset)
        if (currentName == null) {
            Messages.showInfoMessage(
                project,
                "Cannot rename the element at the current cursor position.",
                "Rename",
            )
            return
        }

        // Show rename dialog
        val newName =
            Messages.showInputDialog(
                project,
                "Enter new name:",
                "Rename",
                null,
                currentName,
                null,
            )
        if (newName.isNullOrBlank() || newName == currentName) return

        // Send textDocument/rename
        val renameParams = RenameParams(textDocId, position, newName)
        val workspaceEdit =
            try {
                lspServer.sendRequestSync(TIMEOUT_MS) { server ->
                    server.textDocumentService.rename(renameParams)
                }
            } catch (e: Exception) {
                LOG.warn("LSP rename request failed", e)
                Messages.showErrorDialog(project, "Rename failed: ${e.message}", "Rename Error")
                return
            }

        if (workspaceEdit == null) {
            Messages.showInfoMessage(project, "No changes returned from language server.", "Rename")
            return
        }

        // Apply WorkspaceEdit
        applyWorkspaceEdit(project, workspaceEdit)
    }

    override fun invoke(
        project: Project,
        elements: Array<out PsiElement>,
        dataContext: DataContext?,
    ) {
        // Not used for LSP-based rename
    }

    // ── LSP helpers ────────────────────────────────────────────────────

    private fun findLspServer(project: Project): LspServer? {
        val serverManager = LspServerManager.getInstance(project)
        val servers =
            serverManager.getServersForProvider(
                com.rescript.plugin.lsp.RescriptLspServerSupportProvider::class.java,
            )
        return servers.firstOrNull()
    }

    /**
     * Sends textDocument/prepareRename to check if rename is possible.
     * Returns the current name at the position, or null if rename is not possible.
     * Falls back to extracting the word at cursor if prepareRename is unsupported.
     */
    private fun prepareRename(
        server: LspServer,
        textDocId: org.eclipse.lsp4j.TextDocumentIdentifier,
        position: Position,
        editor: Editor,
        offset: Int,
    ): String? =
        try {
            val params = PrepareRenameParams(textDocId, position)
            val result =
                server.sendRequestSync(TIMEOUT_MS) { s ->
                    s.textDocumentService.prepareRename(params)
                }
            when {
                result == null -> null
                result.isLeft -> extractWordAt(editor, offset)
                result.isSecond -> result.second.placeholder
                result.isThird -> extractWordAt(editor, offset)
                else -> null
            }
        } catch (e: Exception) {
            // prepareRename may not be supported — fall back to word extraction
            LOG.info("prepareRename not supported or failed, falling back to word extraction", e)
            extractWordAt(editor, offset)
        }

    /** Extract the identifier word at the given offset. */
    private fun extractWordAt(
        editor: Editor,
        offset: Int,
    ): String? {
        val text = editor.document.text
        if (offset >= text.length && offset == 0) return null

        var start = offset
        var end = offset

        // Expand left
        while (start > 0 && isIdentifierChar(text[start - 1])) start--
        // Expand right
        while (end < text.length && isIdentifierChar(text[end])) end++

        if (start == end) return null
        return text.substring(start, end)
    }

    private fun isIdentifierChar(ch: Char): Boolean = ch.isLetterOrDigit() || ch == '_' || ch == '\''

    private fun offsetToPosition(
        editor: Editor,
        offset: Int,
    ): Position {
        val line = editor.document.getLineNumber(offset)
        val lineStart = editor.document.getLineStartOffset(line)
        val character = offset - lineStart
        return Position(line, character)
    }

    // ── WorkspaceEdit application ──────────────────────────────────────

    private fun applyWorkspaceEdit(
        project: Project,
        edit: WorkspaceEdit,
    ) {
        val changes: Map<String, List<TextEdit>> = edit.changes ?: return

        WriteCommandAction.runWriteCommandAction(project, "ReScript Rename", null, {
            val fileDocManager = FileDocumentManager.getInstance()

            for ((uriString, textEdits) in changes) {
                val vfsUrl = lspUriToVfsUrl(uriString)
                val virtualFile = VirtualFileManager.getInstance().findFileByUrl(vfsUrl)
                if (virtualFile == null) {
                    LOG.warn("Could not find file for URI: $uriString")
                    continue
                }

                val document = fileDocManager.getDocument(virtualFile)
                if (document == null) {
                    LOG.warn("Could not get document for file: ${virtualFile.path}")
                    continue
                }

                // Apply edits in reverse order to preserve offsets
                val sortedEdits =
                    textEdits.sortedWith(
                        compareByDescending<TextEdit> { it.range.start.line }
                            .thenByDescending { it.range.start.character },
                    )

                for (textEdit in sortedEdits) {
                    val startOffset = positionToOffset(document, textEdit.range.start)
                    val endOffset = positionToOffset(document, textEdit.range.end)
                    if (startOffset >= 0 && endOffset >= 0 && endOffset <= document.textLength) {
                        document.replaceString(startOffset, endOffset, textEdit.newText)
                    }
                }
            }

            fileDocManager.saveAllDocuments()
        })
    }

    private fun positionToOffset(
        document: com.intellij.openapi.editor.Document,
        position: Position,
    ): Int {
        if (position.line >= document.lineCount) return -1
        val lineStart = document.getLineStartOffset(position.line)
        return lineStart + position.character
    }

    /** Convert LSP file URI (file:///path) to IntelliJ VFS URL (file:///path). */
    private fun lspUriToVfsUrl(uri: String): String =
        try {
            val parsed = URI(uri)
            "file://${parsed.path}"
        } catch (e: Exception) {
            uri
        }

    companion object {
        private val LOG = logger<RescriptRenameHandler>()
        private val RESCRIPT_EXTENSIONS = setOf("res", "resi")
        private const val TIMEOUT_MS = 10_000
    }
}
