package com.rescript.plugin.analysis

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.rescript.plugin.lsp.RescriptLspDetector
import com.rescript.plugin.settings.RescriptProjectSettings

/**
 * Automatically starts the reanalyze server when a ReScript project is opened,
 * if the setting is enabled and the installed ReScript version supports server mode.
 *
 * Performs the following checks before starting:
 * 1. `reanalyzeServerEnabled` setting is true
 * 2. The project is a ReScript project (has `rescript.json` or `bsconfig.json`)
 * 3. ReScript >= 12.1.0 is installed (supports `reanalyze-server`)
 *
 * @see RescriptReanalyzeServerService
 * @see RescriptReanalyzeVersionDetector
 * @see com.rescript.plugin.lsp.RescriptLspStartupActivity for a similar startup activity pattern
 */
class RescriptReanalyzeServerStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        val settings = RescriptProjectSettings.getInstance(project)
        if (!settings.reanalyzeServerEnabled) {
            LOG.debug("Reanalyze server mode disabled in settings")
            return
        }

        if (!RescriptLspDetector.isRescriptProject(project)) {
            LOG.debug("Not a ReScript project, skipping reanalyze server start")
            return
        }

        val basePath = project.basePath ?: return
        if (!RescriptReanalyzeVersionDetector.isServerModeSupported(basePath)) {
            LOG.debug("ReScript version does not support reanalyze-server mode")
            return
        }

        val service = RescriptReanalyzeServerService.getInstance(project)
        service.startServer()
    }

    companion object {
        private val LOG = logger<RescriptReanalyzeServerStartupActivity>()
    }
}
