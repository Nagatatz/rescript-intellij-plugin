package com.rescript.plugin.lsp

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspIntegrationProvider
import com.rescript.plugin.util.RescriptFileUtil

/**
 * Triggers the ReScript LSP server when a .res or .resi file is opened.
 *
 * Implements [LspIntegrationProvider], registered under the
 * `com.intellij.platform.lsp.integrationProvider` extension point. The class keeps its
 * historical `ServerSupportProvider` name; only the platform supertype was renamed.
 *
 * @see RescriptLspServerDescriptor
 */
class RescriptLspServerSupportProvider : LspIntegrationProvider {
    override fun fileOpened(
        project: Project,
        file: VirtualFile,
        clientStarter: LspIntegrationProvider.LspClientStarter,
    ) {
        if (RescriptFileUtil.isRescriptFile(file)) {
            clientStarter.ensureClientStarted(RescriptLspServerDescriptor(project))
        }
    }
}
