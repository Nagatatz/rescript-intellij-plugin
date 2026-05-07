package com.rescript.plugin.wizard

import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.module.ModuleTypeManager
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.vfs.VfsUtil
import com.rescript.plugin.RescriptIcons
import com.rescript.plugin.analytics.RescriptFeatureUsageCounter
import com.rescript.plugin.wizard.templates.TemplateContext
import java.io.File
import javax.swing.Icon

/**
 * Module builder for the "New Project" wizard that scaffolds a ReScript project.
 *
 * Generates project files based on the selected [ProjectTemplate], which determines
 * the project structure (rescript.json, package.json, starter files, etc.).
 * Package manager selection (npm/pnpm/yarn) is configurable via [RescriptProjectWizardStep].
 *
 * @see ProjectTemplate for available templates
 * @see RescriptProjectGenerator for file content generation
 */
class RescriptModuleBuilder : ModuleBuilder() {
    var packageManager: PackageManager = PackageManager.PNPM
    var validationLibrary: ValidationLibrary = ValidationLibrary.ZOD
    var apiStrategy: ApiStrategy = ApiStrategy.REST
    var database: Database = Database.LIBSQL
    var selectedTemplate: ProjectTemplate = ProjectTemplate.BASIC

    override fun getModuleType(): ModuleType<*> = ModuleTypeManager.getInstance().defaultModuleType

    override fun getNodeIcon(): Icon = RescriptIcons.FILE

    override fun getPresentableName(): String = "ReScript"

    override fun getDescription(): String = "Create a new ReScript project"

    override fun getGroupName(): String = "ReScript"

    override fun getCustomOptionsStep(
        context: WizardContext?,
        parentDisposable: com.intellij.openapi.Disposable?,
    ): ModuleWizardStep = RescriptProjectWizardStep(this)

    override fun setupRootModel(modifiableRootModel: ModifiableRootModel) {
        val contentRoot = doAddContentEntry(modifiableRootModel) ?: return
        val rootPath = contentRoot.file?.path ?: return
        val projectName = modifiableRootModel.module.name

        RescriptFeatureUsageCounter.WIZARD_TEMPLATE_SELECTED.log(selectedTemplate)

        val files =
            RescriptProjectGenerator.generateFiles(
                selectedTemplate,
                TemplateContext(
                    projectName = projectName,
                    packageManager = packageManager,
                    validationLibrary = validationLibrary,
                    apiStrategy = apiStrategy,
                    database = database,
                ),
            )

        // Write all generated files
        for ((relativePath, content) in files) {
            val file = File(rootPath, relativePath)
            file.parentFile.mkdirs()
            file.writeText(content)
        }

        // Mark source roots
        for (sourceRoot in selectedTemplate.sourceRoots) {
            val srcDir = File(rootPath, sourceRoot)
            srcDir.mkdirs()
            val srcVDir = VfsUtil.findFileByIoFile(srcDir, true)
            if (srcVDir != null) {
                contentRoot.addSourceFolder(srcVDir, false)
            }
        }
    }
}
