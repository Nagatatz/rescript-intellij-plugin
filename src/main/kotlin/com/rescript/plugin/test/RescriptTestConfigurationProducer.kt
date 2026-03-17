package com.rescript.plugin.test

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.rescript.plugin.util.RescriptFileUtil

/**
 * Automatically creates [RescriptTestRunConfiguration] from context (e.g., right-clicking a test file).
 *
 * Only produces configurations for `.res`/`.resi` files whose names end with `_test`, `Test`,
 * `_spec`, or `Spec`. The test framework is auto-detected via [RescriptTestFrameworkDetector].
 */
class RescriptTestConfigurationProducer : LazyRunConfigurationProducer<RescriptTestRunConfiguration>() {
    override fun getConfigurationFactory(): ConfigurationFactory =
        RescriptTestRunConfigurationType().configurationFactories.first()

    override fun setupConfigurationFromContext(
        configuration: RescriptTestRunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>,
    ): Boolean {
        val file = context.location?.virtualFile ?: return false
        if (!RescriptFileUtil.isRescriptFile(file)) return false

        // Only produce for files that look like tests
        val name = file.nameWithoutExtension
        if (!isTestFileName(name)) {
            return false
        }

        val project = context.project
        val framework = RescriptTestFrameworkDetector.detect(project) ?: return false

        configuration.framework = framework
        configuration.workingDirectory = project.basePath
        configuration.name = "Test: ${file.nameWithoutExtension}"

        return true
    }

    override fun isConfigurationFromContext(
        configuration: RescriptTestRunConfiguration,
        context: ConfigurationContext,
    ): Boolean {
        val file = context.location?.virtualFile ?: return false
        return configuration.name == "Test: ${file.nameWithoutExtension}"
    }

    companion object {
        /**
         * Checks whether a file name (without extension) matches a test file naming convention.
         *
         * Recognized suffixes: `_test`, `Test`, `_spec`, `Spec`.
         *
         * @param nameWithoutExtension the file name without its extension
         * @return true if the name matches a known test file pattern
         */
        fun isTestFileName(nameWithoutExtension: String): Boolean =
            nameWithoutExtension.endsWith("_test") ||
                nameWithoutExtension.endsWith("Test") ||
                nameWithoutExtension.endsWith("_spec") ||
                nameWithoutExtension.endsWith("Spec")
    }
}
