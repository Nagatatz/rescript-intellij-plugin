package com.rescript.plugin.lsp

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServerSupportProvider

/**
 * Triggers the ReScript LSP server when a .res or .resi file is opened.
 */
class RescriptLspServerSupportProvider : LspServerSupportProvider {
    override fun fileOpened(
        project: Project,
        file: VirtualFile,
        serverStarter: LspServerSupportProvider.LspServerStarter,
    ) {
        if (file.extension in RESCRIPT_EXTENSIONS) {
            serverStarter.ensureServerStarted(RescriptLspServerDescriptor(project))
        }
    }

    companion object {
        internal val RESCRIPT_EXTENSIONS = setOf("res", "resi")
    }
}
