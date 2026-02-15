package com.rescript.plugin.lsp

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.ProjectWideLspServerDescriptor
import com.intellij.platform.lsp.api.customization.LspCustomization
import org.eclipse.lsp4j.services.LanguageServer
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

/**
 * Descriptor for the ReScript Language Server.
 *
 * Searches for `rescript-language-server` in:
 *  1. Project's `node_modules/.bin/`
 *  2. Parent directories (monorepo support)
 *  3. Global PATH
 *
 * Launches with `--stdio` for LSP communication.
 */
class RescriptLspServerDescriptor(
    project: Project,
) : ProjectWideLspServerDescriptor(project, "ReScript") {
    override val lsp4jServerClass: Class<out LanguageServer> = LanguageServer::class.java

    override val lspCustomization =
        object : LspCustomization() {
            override val semanticTokensCustomizer = RescriptSemanticTokensSupport()
        }

    override fun isSupportedFile(file: VirtualFile): Boolean = file.extension in RESCRIPT_EXTENSIONS

    override fun createCommandLine(): GeneralCommandLine {
        val serverPath =
            findLanguageServer()
                ?: throw ExecutionException(
                    "Could not find rescript-language-server.\n" +
                        "Install it via: npm install @rescript/language-server\n" +
                        "or globally: npm install -g @rescript/language-server",
                )

        LOG.info("Starting ReScript Language Server from: $serverPath")

        val cmd =
            if (serverPath.endsWith(".js")) {
                GeneralCommandLine("node", serverPath, "--stdio")
            } else {
                GeneralCommandLine(serverPath, "--stdio")
            }

        project.basePath?.let { cmd.workDirectory = File(it) }
        return cmd
    }

    // ── Server discovery ──────────────────────────────────────────────

    private fun findLanguageServer(): String? =
        findInProjectNodeModules()
            ?: findInParentNodeModules()
            ?: findOnPath()

    /** Look in the project's own node_modules. */
    private fun findInProjectNodeModules(): String? = project.basePath?.let { findInNodeModules(Path.of(it)) }

    /** Walk up directories for monorepo layouts. */
    private fun findInParentNodeModules(): String? {
        var dir = project.basePath?.let { Path.of(it).parent }
        while (dir != null) {
            findInNodeModules(dir)?.let { return it }
            dir = dir.parent
        }
        return null
    }

    private fun findInNodeModules(base: Path): String? {
        // .bin executable (npm/pnpm/yarn)
        val bin = base.resolve("node_modules/.bin/rescript-language-server")
        if (Files.isExecutable(bin)) return bin.toString()

        // Direct JS entry-point
        val js = base.resolve("node_modules/@rescript/language-server/out/cli.js")
        if (Files.isRegularFile(js)) return js.toString()

        return null
    }

    /** Fall back to a globally-installed binary. */
    private fun findOnPath(): String? = resolveCommand("rescript-language-server")

    private fun resolveCommand(cmd: String): String? {
        // Unix: which
        tryExec("which", cmd)?.let { return it }
        // Windows: where
        tryExec("where", cmd)?.let { return it }
        return null
    }

    private fun tryExec(vararg args: String): String? =
        runCatching {
            val proc = ProcessBuilder(*args).redirectErrorStream(true).start()
            val output =
                proc.inputStream
                    .bufferedReader()
                    .readLine()
                    ?.trim()
            val ok = proc.waitFor() == 0
            if (ok && !output.isNullOrEmpty() && File(output).exists()) output else null
        }.getOrNull()

    companion object {
        private val LOG = logger<RescriptLspServerDescriptor>()
        private val RESCRIPT_EXTENSIONS = setOf("res", "resi")
    }
}
