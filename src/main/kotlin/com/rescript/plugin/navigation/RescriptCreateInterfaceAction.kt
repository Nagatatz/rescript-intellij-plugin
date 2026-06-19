package com.rescript.plugin.navigation

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFileManager
import com.rescript.plugin.lsp.RescriptLanguageServer
import com.rescript.plugin.lsp.RescriptLspUtils
import com.rescript.plugin.util.RescriptFileUtil
import com.rescript.plugin.util.RescriptSecurityUtils
import org.eclipse.lsp4j.TextDocumentIdentifier

/**
 * Action to generate a `.resi` interface file from a `.res` source file.
 *
 * Sends the LSP custom request `textDocument/createInterface` to the
 * rescript-language-server and opens the generated interface file in the editor.
 */
class RescriptCreateInterfaceAction : AnAction() {
    // LspServer / sendRequestSync are deprecated in 2026.2 EAP; the replacement
    // LspClientDescriptor API does not exist on the 2026.1.2 compile target.
    @Suppress("DEPRECATION")
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        if (!RescriptFileUtil.isResFile(file)) return

        // Check if .resi already exists
        val resiFile = RescriptFileUtil.findInterfaceFile(file)
        if (resiFile != null) {
            val result =
                Messages.showYesNoDialog(
                    project,
                    "Interface file '${file.nameWithoutExtension}.resi' already exists. Overwrite?",
                    "Create Interface File",
                    Messages.getQuestionIcon(),
                )
            if (result != Messages.YES) return
        }

        // Get LSP server and send createInterface request
        val server =
            RescriptLspUtils.getServer(project) ?: run {
                LOG.warn("No ReScript LSP server available")
                return
            }

        val uri = RescriptLspUtils.toLspUri(file)

        server
            // Explicit 10s timeout (the platform default). Omitting it emits
            // the synthetic sendRequestSync$default, which 2026.2 relocated to
            // the new LspClient super-interface and no longer resolves here.
            .sendRequestSync(10_000) { languageServer ->
                (languageServer as RescriptLanguageServer).createInterface(TextDocumentIdentifier(uri))
            }?.let { response ->
                val resultUrl = RescriptLspUtils.lspUriToVfsUrl(response.uri)
                ApplicationManager.getApplication().invokeLater {
                    VirtualFileManager.getInstance().refreshAndFindFileByUrl(resultUrl)?.let { vf ->
                        // Validate the resolved file is within the project directory
                        if (!RescriptSecurityUtils.isWithinProject(project, vf)) {
                            LOG.warn("LSP createInterface returned a file outside the project scope, ignoring")
                            return@invokeLater
                        }
                        FileEditorManager.getInstance(project).openFile(vf, true)
                    }
                }
            }
    }

    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        e.presentation.isEnabledAndVisible = file != null && RescriptFileUtil.isResFile(file)
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    companion object {
        private val LOG = logger<RescriptCreateInterfaceAction>()
    }
}
