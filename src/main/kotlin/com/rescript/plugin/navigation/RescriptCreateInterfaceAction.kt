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
import com.intellij.platform.lsp.api.LspServerManager
import com.rescript.plugin.lsp.RescriptLanguageServer
import com.rescript.plugin.lsp.RescriptLspServerSupportProvider
import org.eclipse.lsp4j.TextDocumentIdentifier

/**
 * Action to generate a `.resi` interface file from a `.res` source file.
 *
 * Sends the LSP custom request `textDocument/createInterface` to the
 * rescript-language-server and opens the generated interface file in the editor.
 */
class RescriptCreateInterfaceAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        if (file.extension != "res") return

        // Check if .resi already exists
        val resiFile = file.parent?.findChild("${file.nameWithoutExtension}.resi")
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
        @Suppress("UnstableApiUsage")
        val servers =
            LspServerManager
                .getInstance(project)
                .getServersForProvider(RescriptLspServerSupportProvider::class.java)

        val server =
            servers.firstOrNull() ?: run {
                LOG.warn("No ReScript LSP server available")
                return
            }

        val uri =
            file.url.let { url ->
                if (url.startsWith("file://")) url else "file://${file.path}"
            }

        server
            .sendRequestSync { languageServer ->
                (languageServer as RescriptLanguageServer).createInterface(TextDocumentIdentifier(uri))
            }?.let { response ->
                val resultUrl =
                    response.uri.let { u ->
                        if (u.startsWith("file://")) u else "file://$u"
                    }
                ApplicationManager.getApplication().invokeLater {
                    VirtualFileManager.getInstance().refreshAndFindFileByUrl(resultUrl)?.let { vf ->
                        FileEditorManager.getInstance(project).openFile(vf, true)
                    }
                }
            }
    }

    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        e.presentation.isEnabledAndVisible = file?.extension == "res"
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    companion object {
        private val LOG = logger<RescriptCreateInterfaceAction>()
    }
}
