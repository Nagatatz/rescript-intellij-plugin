package com.rescript.plugin.binding

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFileManager
import com.rescript.plugin.settings.RescriptProjectSettings
import java.io.File

/**
 * Action that generates ReScript binding code from a TypeScript `.d.ts` file.
 *
 * Available in the project tree and editor context menus when a `.d.ts` file
 * is selected. Spawns a Node.js process to parse the file using the TypeScript
 * Compiler API, then converts the result to ReScript binding code.
 *
 * @see DtsParserProcess for the Node.js parser integration
 * @see DtsToRescriptConverter for the code generation logic
 */
class DtsGenerateBindingAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        if (!file.name.endsWith(".d.ts")) return

        val settings = RescriptProjectSettings.getInstance(project)
        val nodePath = DtsNodeDetector.resolveNodePath(settings)

        // Verify Node.js is available
        if (!DtsNodeDetector.isNodeAvailable(nodePath)) {
            Messages.showErrorDialog(
                project,
                "Node.js not found. Please install Node.js or configure the path in " +
                    "Settings > Languages & Frameworks > ReScript.",
                "Generate ReScript Binding",
            )
            return
        }

        // Find TypeScript
        val tsPath =
            DtsNodeDetector.findTypeScriptPath(project.basePath) ?: run {
                Messages.showErrorDialog(
                    project,
                    "TypeScript not found in node_modules.\n" +
                        "Install it via: npm install typescript",
                    "Generate ReScript Binding",
                )
                return
            }

        if (!DtsNodeDetector.isTypeScriptAvailable(tsPath)) {
            Messages.showErrorDialog(
                project,
                "TypeScript package at $tsPath appears incomplete.\n" +
                    "Try reinstalling: npm install typescript",
                "Generate ReScript Binding",
            )
            return
        }

        // Determine output file path
        val dtsPath = file.path
        val outputName =
            file.nameWithoutExtension.let { name ->
                // Remove .d suffix: "lodash.d" → "lodash"
                if (name.endsWith(".d")) name.dropLast(2) else name
            }
        val outputPath = file.parent.path + File.separator + outputName + ".res"
        val outputFile = File(outputPath)

        // Check if output file already exists
        if (outputFile.exists()) {
            val result =
                Messages.showYesNoDialog(
                    project,
                    "File '$outputName.res' already exists. Overwrite?",
                    "Generate ReScript Binding",
                    Messages.getQuestionIcon(),
                )
            if (result != Messages.YES) return
        }

        // Run parsing in background
        ProgressManager.getInstance().run(
            object : Task.Backgroundable(project, "Generating ReScript binding...", true) {
                override fun run(indicator: ProgressIndicator) {
                    indicator.isIndeterminate = false
                    indicator.fraction = 0.1
                    indicator.text = "Parsing ${file.name}..."

                    try {
                        val json = DtsParserProcess.parse(nodePath, dtsPath, tsPath)
                        indicator.fraction = 0.5
                        indicator.text = "Converting to ReScript..."

                        val dtsFile = DtsJsonModel.parse(json)
                        val rescriptCode = DtsToRescriptConverter.convert(dtsFile)
                        indicator.fraction = 0.9
                        indicator.text = "Writing output..."

                        outputFile.writeText(rescriptCode)

                        // Open the generated file in the editor
                        ApplicationManager.getApplication().invokeLater {
                            VirtualFileManager.getInstance().refreshAndFindFileByUrl("file://$outputPath")?.let { vf ->
                                FileEditorManager.getInstance(project).openFile(vf, true)
                            }
                        }

                        indicator.fraction = 1.0
                    } catch (e: DtsParserException) {
                        LOG.warn("dts-to-json parser failed", e)
                        ApplicationManager.getApplication().invokeLater {
                            Messages.showErrorDialog(
                                project,
                                "Failed to parse ${file.name}:\n${e.message}",
                                "Generate ReScript Binding",
                            )
                        }
                    } catch (e: Exception) {
                        LOG.error("Unexpected error generating binding", e)
                        ApplicationManager.getApplication().invokeLater {
                            Messages.showErrorDialog(
                                project,
                                "Unexpected error: ${e.message}",
                                "Generate ReScript Binding",
                            )
                        }
                    }
                }
            },
        )
    }

    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        e.presentation.isEnabledAndVisible = file?.name?.endsWith(".d.ts") == true
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    companion object {
        private val LOG = logger<DtsGenerateBindingAction>()
    }
}
