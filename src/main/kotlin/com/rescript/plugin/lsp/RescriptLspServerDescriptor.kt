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
import com.rescript.plugin.util.RescriptFileUtil
import com.rescript.plugin.util.RescriptPaths
import com.rescript.plugin.util.RescriptProcessUtils
import com.rescript.plugin.util.RescriptSecurityUtils
import org.eclipse.lsp4j.services.LanguageServer
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

// ProjectWideLspServerDescriptor and its overridden members are deprecated in 2026.2 EAP;
// the replacement ProjectWideLspClientDescriptor API does not exist on the 2026.1.2 compile target.

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
@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
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
                    "signatureHelp" to
                        mapOf(
                            "enabled" to settings.signatureHelpEnabled,
                            "forConstructorPayloads" to settings.signatureHelpForConstructorPayloads,
                        ),
                    "cache" to
                        mapOf(
                            "projectConfig" to
                                mapOf(
                                    "enable" to settings.cacheProjectConfigEnabled,
                                ),
                        ),
                    "inlayHints" to
                        mapOf(
                            "enable" to settings.inlayHintsEnabled,
                            "maxLength" to settings.inlayHintsMaxLength,
                        ),
                    "compileStatus" to
                        mapOf(
                            "enable" to settings.compileStatusEnabled,
                        ),
                ),
        )
    }

    override fun isSupportedFile(file: VirtualFile): Boolean = RescriptFileUtil.isRescriptFile(file)

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
            ?: findInDetectedPackageRoots()
            ?: findInParentNodeModules()
            ?: findOnPath()

    /** Look in the project's own node_modules. */
    private fun findInProjectNodeModules(): String? = project.basePath?.let { findInNodeModules(Path.of(it)) }

    /**
     * Look inside every workspace package root discovered by
     * [RescriptWorkspaceDiscovery]. Required for pnpm monorepos where the LSP
     * binary lives under `packages/<name>/node_modules/.bin/` and is never
     * hoisted to the workspace root.
     */
    private fun findInDetectedPackageRoots(): String? {
        val layout = RescriptWorkspaceDiscovery.discover(project)
        for (root in layout.packageRoots) {
            findInNodeModules(root)?.let { return it }
        }
        return null
    }

    /** Walk up directories for monorepo layouts. */
    private fun findInParentNodeModules(): String? {
        var dir = project.basePath?.let { Path.of(it).parent }
        var depth = 0
        while (dir != null && depth < RescriptSecurityUtils.MAX_PARENT_TRAVERSAL_DEPTH) {
            findInNodeModules(dir)?.let { return it }
            dir = dir.parent
            depth++
        }
        return null
    }

    private fun findInNodeModules(base: Path): String? {
        // .bin executable (npm/pnpm/yarn)
        val bin = base.resolve("${RescriptPaths.NODE_MODULES_BIN}/${RescriptPaths.LSP_BIN_NAME}")
        if (Files.isExecutable(bin)) return bin.toString()

        // Direct JS entry-point
        val js = base.resolve(RescriptPaths.LSP_CLI_JS)
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

    private fun tryExec(vararg args: String): String? {
        val result = RescriptProcessUtils.runSimpleCommand(*args)
        if (result.timedOut || result.exitCode != 0) return null
        val output = result.firstLine
        return if (output.isNotEmpty() && File(output).exists()) output else null
    }

    companion object {
        private val LOG = logger<RescriptLspServerDescriptor>()
    }
}
