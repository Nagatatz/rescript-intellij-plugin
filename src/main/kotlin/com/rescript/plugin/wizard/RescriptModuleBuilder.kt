package com.rescript.plugin.wizard

import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.module.ModuleTypeManager
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.vfs.VfsUtil
import com.rescript.plugin.RescriptIcons
import java.io.File
import javax.swing.Icon

/**
 * Module builder for the "New Project" wizard that scaffolds a ReScript project.
 *
 * Generates `rescript.json`, `package.json`, a `src/` directory with a starter file,
 * and optionally includes React support. Package manager selection (npm/pnpm/yarn)
 * is configurable via [RescriptProjectWizardStep].
 *
 * @see RescriptProjectGenerator for the file content generation
 */
class RescriptModuleBuilder : ModuleBuilder() {
    var packageManager: PackageManager = PackageManager.NPM
    var includeReact: Boolean = false

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

        // Create src directory
        val srcDir = File(rootPath, "src")
        srcDir.mkdirs()

        // Generate rescript.json
        val rescriptJson = File(rootPath, "rescript.json")
        rescriptJson.writeText(RescriptProjectGenerator.generateRescriptJson(projectName, includeReact))

        // Generate package.json
        val packageJson = File(rootPath, "package.json")
        packageJson.writeText(RescriptProjectGenerator.generatePackageJson(projectName, includeReact))

        // Generate starter file
        val starterFile = File(srcDir, "App.res")
        if (includeReact) {
            starterFile.writeText(RescriptProjectGenerator.generateReactComponent())
        } else {
            starterFile.writeText(RescriptProjectGenerator.generateStarterModule())
        }

        // Mark src/ as source root
        val srcVDir = VfsUtil.findFileByIoFile(srcDir, true)
        if (srcVDir != null) {
            contentRoot.addSourceFolder(srcVDir, false)
        }
    }
}
