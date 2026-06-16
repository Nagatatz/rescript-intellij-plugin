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

        // When the context lands on a specific describe/it/test call, narrow the run with a -t filter.
        val testName = resolveTestName(context)
        if (testName != null) {
            configuration.testName = testName
            configuration.name = configurationName(file.nameWithoutExtension, testName)
        } else {
            configuration.testName = ""
            configuration.name = configurationName(file.nameWithoutExtension, null)
        }

        return true
    }

    override fun isConfigurationFromContext(
        configuration: RescriptTestRunConfiguration,
        context: ConfigurationContext,
    ): Boolean {
        val file = context.location?.virtualFile ?: return false
        val testName = resolveTestName(context)
        return configuration.name == configurationName(file.nameWithoutExtension, testName)
    }

    /**
     * Resolves the test name for the call at the context's caret position, if any.
     *
     * Maps the offset of the context's PSI element onto a [RescriptTestCallDetector] result so a
     * gutter run on a single `describe` / `it` / `test` line produces a `-t <name>`-filtered run.
     *
     * @param context the run configuration context (caret / element location)
     * @return the matched test name, or null when the context is not on a recognized call
     */
    private fun resolveTestName(context: ConfigurationContext): String? {
        val element = context.psiLocation ?: return null
        val file = element.containingFile ?: return null
        if (file.fileType != com.rescript.plugin.RescriptFileType) return null
        val offset = element.textRange?.startOffset ?: return null
        return RescriptTestCallDetector
            .detect(file.text)
            .firstOrNull { it.callOffset == offset }
            ?.testName
    }

    companion object {
        /**
         * Builds the configuration display name, optionally scoped to a single test.
         *
         * @param fileNameWithoutExtension the test file name without its extension
         * @param testName the specific test name, or null for a whole-file run
         * @return the display name used for both creation and context matching
         */
        internal fun configurationName(
            fileNameWithoutExtension: String,
            testName: String?,
        ): String =
            if (testName.isNullOrBlank()) {
                "Test: $fileNameWithoutExtension"
            } else {
                "Test: $fileNameWithoutExtension > $testName"
            }

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
