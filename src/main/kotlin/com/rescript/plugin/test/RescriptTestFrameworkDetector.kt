package com.rescript.plugin.test

import com.google.gson.JsonParser
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile

/**
 * Supported test frameworks for running ReScript tests.
 *
 * Each entry provides its persistent [id], human-readable [displayName],
 * and [defaultCommand] used when constructing the test runner command line.
 */
enum class TestFramework(
    val id: String,
    val displayName: String,
    val defaultCommand: String,
) {
    JEST("jest", "Jest", "npx jest"),
    VITEST("vitest", "Vitest", "npx vitest run"),
    CUSTOM("custom", "Custom", ""),
}

/**
 * Detects the test framework (Jest or Vitest) from the project's `package.json`
 * dependencies or from the presence of framework-specific config files
 * (e.g., `vitest.config.ts`, `jest.config.js`).
 */
object RescriptTestFrameworkDetector {
    private val LOG = logger<RescriptTestFrameworkDetector>()

    /**
     * Detects the test framework from the project's package.json devDependencies.
     * Returns null if no known framework is found.
     */
    fun detect(project: Project): TestFramework? {
        val projectDir = project.guessProjectDir() ?: return null
        return detectFromDirectory(projectDir)
    }

    /**
     * Detects test framework from a directory containing package.json.
     * Visible for testing.
     */
    internal fun detectFromDirectory(dir: VirtualFile): TestFramework? {
        val packageJson = dir.findChild("package.json") ?: return null
        return detectFromPackageJson(packageJson)
    }

    /**
     * Detects test framework from a package.json file content.
     * Visible for testing.
     */
    internal fun detectFromPackageJson(packageJson: VirtualFile): TestFramework? =
        try {
            val content = String(packageJson.contentsToByteArray(), Charsets.UTF_8)
            detectFromPackageJsonContent(content)
        } catch (e: Exception) {
            LOG.debug("Failed to read package.json", e)
            null
        }

    /**
     * Detects test framework from package.json content string.
     * Visible for testing.
     */
    internal fun detectFromPackageJsonContent(content: String): TestFramework? =
        try {
            val root = JsonParser.parseString(content).asJsonObject
            val allDeps = mutableSetOf<String>()

            root.getAsJsonObject("devDependencies")?.keySet()?.let { allDeps.addAll(it) }
            root.getAsJsonObject("dependencies")?.keySet()?.let { allDeps.addAll(it) }

            when {
                "vitest" in allDeps -> TestFramework.VITEST
                "jest" in allDeps -> TestFramework.JEST
                else -> null
            }
        } catch (e: Exception) {
            LOG.debug("Failed to parse package.json", e)
            null
        }

    /**
     * Detects test framework from config file existence.
     * Visible for testing.
     */
    internal fun detectFromConfigFiles(dir: VirtualFile): TestFramework? {
        val configFiles =
            mapOf(
                "vitest.config.ts" to TestFramework.VITEST,
                "vitest.config.js" to TestFramework.VITEST,
                "vitest.config.mts" to TestFramework.VITEST,
                "jest.config.js" to TestFramework.JEST,
                "jest.config.ts" to TestFramework.JEST,
                "jest.config.mjs" to TestFramework.JEST,
            )

        for ((fileName, framework) in configFiles) {
            if (dir.findChild(fileName) != null) {
                return framework
            }
        }
        return null
    }
}
