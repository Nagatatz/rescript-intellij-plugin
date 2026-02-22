package com.rescript.plugin.lsp

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.Lsp4jClient
import com.intellij.platform.lsp.api.LspServerNotificationsHandler
import com.intellij.platform.lsp.api.ProjectWideLspServerDescriptor
import com.intellij.platform.lsp.api.customization.LspCustomization
import com.rescript.plugin.settings.RescriptProjectSettings
import com.rescript.plugin.util.RescriptSecurityUtils
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
    override val lsp4jServerClass: Class<out LanguageServer> = RescriptLanguageServer::class.java

    override fun createLsp4jClient(handler: LspServerNotificationsHandler): Lsp4jClient =
        RescriptLsp4jClient(handler, project)

    override val lspCustomization =
        object : LspCustomization() {
            override val semanticTokensCustomizer = RescriptSemanticTokensSupport()
        }

    override fun createInitializationOptions(): Any {
        val settings = RescriptProjectSettings.getInstance(project)
        return mapOf(
            "extensionConfiguration" to
                mapOf(
                    "binaryPath" to settings.rescriptBinaryPath.ifEmpty { null },
                    "platformPath" to settings.platformPath.ifEmpty { null },
                    "runtimePath" to settings.runtimePath.ifEmpty { null },
                    "logLevel" to settings.logLevel,
                    "codeLens" to true,
                    "incrementalTypechecking" to
                        mapOf(
                            "enabled" to settings.incrementalTypecheckingEnabled,
                            "acrossFiles" to settings.incrementalTypecheckingAcrossFiles,
                        ),
                ),
        )
    }

    override fun isSupportedFile(file: VirtualFile): Boolean =
        file.extension in RescriptLspServerSupportProvider.RESCRIPT_EXTENSIONS

    override fun createCommandLine(): GeneralCommandLine {
        val settings = RescriptProjectSettings.getInstance(project)
        val customPath = settings.lspServerPath.takeIf { it.isNotEmpty() }
        val customNode = settings.nodePath.takeIf { it.isNotEmpty() }

        // Validate custom paths at consumption time, falling back to auto-detection if invalid
        val validatedCustomPath = customPath?.let { path -> validateServerPath(path) }
        val validatedCustomNode =
            customNode?.let { path ->
                if (RescriptSecurityUtils.isValidExecutable(path)) {
                    path
                } else {
                    LOG.warn("Custom Node.js path is not a valid executable, falling back to 'node'")
                    null
                }
            }

        val serverPath =
            validatedCustomPath
                ?: findLanguageServer()
                ?: throw ExecutionException(
                    "Could not find rescript-language-server.\n" +
                        "Install it via: npm install @rescript/language-server\n" +
                        "or globally: npm install -g @rescript/language-server\n" +
                        "Or configure the path in Settings > Languages & Frameworks > ReScript.",
                )

        LOG.info("Starting ReScript Language Server from: $serverPath")

        val nodeCmd = validatedCustomNode ?: "node"
        val cmd =
            if (serverPath.endsWith(".js")) {
                GeneralCommandLine(nodeCmd, serverPath, "--stdio")
            } else {
                GeneralCommandLine(serverPath, "--stdio")
            }

        project.basePath?.let { cmd.workDirectory = File(it) }
        return cmd
    }

    /**
     * Validates a custom server path: `.js` files must exist; binaries must be executable.
     *
     * @param path the user-configured server path to validate
     * @return the validated path, or null if invalid (with a logged warning)
     */
    private fun validateServerPath(path: String): String? {
        val file = File(path)
        return if (path.endsWith(".js")) {
            if (file.isFile) {
                path
            } else {
                LOG.warn("Custom LSP server path is not a valid file, falling back to auto-detection")
                null
            }
        } else {
            if (RescriptSecurityUtils.isValidExecutable(path)) {
                path
            } else {
                LOG.warn("Custom LSP server path is not a valid executable, falling back to auto-detection")
                null
            }
        }
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
        try {
            val proc = ProcessBuilder(*args).redirectErrorStream(true).start()
            val output =
                proc.inputStream.use { stream ->
                    stream.bufferedReader().readLine()?.trim()
                }
            val ok = proc.waitFor() == 0
            if (ok && !output.isNullOrEmpty() && File(output).exists()) output else null
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            null
        } catch (_: Exception) {
            null
        }

    companion object {
        private val LOG = logger<RescriptLspServerDescriptor>()
    }
}
