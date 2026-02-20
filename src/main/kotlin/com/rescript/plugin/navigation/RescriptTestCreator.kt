package com.rescript.plugin.navigation

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.testIntegration.TestCreator
import com.rescript.plugin.lang.psi.RescriptFile
import com.rescript.plugin.test.RescriptTestFrameworkDetector
import com.rescript.plugin.test.TestFramework

/**
 * Provides "Navigate to Test" (Ctrl+Shift+T) support for ReScript files.
 *
 * Navigates between implementation and test files using naming conventions:
 * `Foo.res` ↔ `Foo_test.res` / `Foo.test.res` / `__tests__/Foo_test.res`.
 * If no test file exists, offers to create one with appropriate boilerplate
 * based on the detected test framework (Jest or Vitest).
 */
class RescriptTestCreator : TestCreator {
    companion object {
        private val TEST_SUFFIXES = listOf("_test", ".test")

        /**
         * Derives test file name candidates from a source file name.
         * Example: "Foo.res" → ["Foo_test.res", "Foo.test.res"]
         */
        internal fun testFileNames(sourceFileName: String): List<String> {
            val ext = if (sourceFileName.endsWith(".resi")) ".resi" else ".res"
            val baseName = sourceFileName.removeSuffix(ext)
            return TEST_SUFFIXES.map { suffix -> "$baseName$suffix$ext" }
        }

        /**
         * Derives source file name from a test file name.
         * Example: "Foo_test.res" → "Foo.res"
         */
        internal fun sourceFileName(testFileName: String): String? {
            val ext = if (testFileName.endsWith(".resi")) ".resi" else ".res"
            val baseName = testFileName.removeSuffix(ext)
            for (suffix in TEST_SUFFIXES) {
                if (baseName.endsWith(suffix)) {
                    return baseName.removeSuffix(suffix) + ext
                }
            }
            return null
        }

        /**
         * Checks if a file name matches the test file naming convention.
         */
        internal fun isTestFile(fileName: String): Boolean {
            val ext =
                if (fileName.endsWith(".resi")) {
                    ".resi"
                } else if (fileName.endsWith(".res")) {
                    ".res"
                } else {
                    return false
                }
            val baseName = fileName.removeSuffix(ext)
            return TEST_SUFFIXES.any { baseName.endsWith(it) }
        }
    }

    override fun isAvailable(
        project: Project,
        editor: Editor,
        file: PsiFile,
    ): Boolean = file is RescriptFile

    override fun createTest(
        project: Project,
        editor: Editor,
        file: PsiFile,
    ) {
        if (file !is RescriptFile) return

        val virtualFile = file.virtualFile ?: return
        val fileName = virtualFile.name

        if (isTestFile(fileName)) {
            // Navigate from test to implementation
            navigateToSource(project, virtualFile)
        } else {
            // Navigate from implementation to test
            navigateToTest(project, virtualFile)
        }
    }

    private fun navigateToTest(
        project: Project,
        sourceFile: VirtualFile,
    ) {
        val testFile = findTestFile(sourceFile)
        if (testFile != null) {
            FileEditorManager.getInstance(project).openFile(testFile, true)
            return
        }

        // Offer to create test file
        val testNames = testFileNames(sourceFile.name)
        val parentDir = sourceFile.parent ?: return
        val testFileName = testNames.first() // default: Foo_test.res

        val result =
            Messages.showYesNoDialog(
                project,
                "Test file '$testFileName' not found. Create it?",
                "Create Test File",
                Messages.getQuestionIcon(),
            )
        if (result != Messages.YES) return

        createTestFile(project, parentDir, testFileName, sourceFile.nameWithoutExtension)
    }

    private fun navigateToSource(
        project: Project,
        testFile: VirtualFile,
    ) {
        val sourceName = sourceFileName(testFile.name) ?: return
        val sourceFile = findSourceFile(testFile, sourceName)
        if (sourceFile != null) {
            FileEditorManager.getInstance(project).openFile(sourceFile, true)
        }
    }

    /**
     * Searches for a test file corresponding to the given source file.
     * Checks the same directory, `__tests__/` subdirectory, and parent's `__tests__/`.
     */
    internal fun findTestFile(sourceFile: VirtualFile): VirtualFile? {
        val testNames = testFileNames(sourceFile.name)
        val parentDir = sourceFile.parent ?: return null

        // Check same directory
        for (name in testNames) {
            parentDir.findChild(name)?.let { return it }
        }

        // Check __tests__/ subdirectory
        val testsDir = parentDir.findChild("__tests__")
        if (testsDir != null && testsDir.isDirectory) {
            for (name in testNames) {
                testsDir.findChild(name)?.let { return it }
            }
        }

        // Check parent's __tests__/ directory
        val grandParent = parentDir.parent
        val parentTestsDir = grandParent?.findChild("__tests__")
        if (parentTestsDir != null && parentTestsDir.isDirectory) {
            for (name in testNames) {
                parentTestsDir.findChild(name)?.let { return it }
            }
        }

        return null
    }

    /**
     * Searches for the source file corresponding to a test file.
     */
    private fun findSourceFile(
        testFile: VirtualFile,
        sourceName: String,
    ): VirtualFile? {
        val parentDir = testFile.parent ?: return null

        // Check same directory
        parentDir.findChild(sourceName)?.let { return it }

        // If in __tests__, check parent directory
        if (parentDir.name == "__tests__") {
            parentDir.parent?.findChild(sourceName)?.let { return it }
        }

        return null
    }

    private fun createTestFile(
        project: Project,
        directory: VirtualFile,
        fileName: String,
        moduleName: String,
    ) {
        val framework = RescriptTestFrameworkDetector.detect(project)
        val content = generateTestContent(moduleName, framework)

        com.intellij.openapi.application.runWriteAction {
            val newFile = directory.createChildData(this, fileName)
            newFile.setBinaryContent(content.toByteArray(Charsets.UTF_8))
            FileEditorManager.getInstance(project).openFile(newFile, true)
        }
    }

    /**
     * Generates test file boilerplate content for the given module name and framework.
     */
    internal fun generateTestContent(
        moduleName: String,
        framework: TestFramework?,
    ): String =
        when (framework) {
            TestFramework.VITEST ->
                """
                |open Vitest
                |
                |describe("$moduleName", () => {
                |  test("should work", () => {
                |    expect(true)->toBe(true)
                |  })
                |})
                """.trimMargin()

            TestFramework.JEST ->
                """
                |open Jest
                |
                |describe("$moduleName", () => {
                |  test("should work", () => {
                |    expect(true)->toBe(true)
                |  })
                |})
                """.trimMargin()

            else ->
                """
                |// Test for $moduleName
                |
                |let () = {
                |  Js.log("$moduleName tests")
                |}
                """.trimMargin()
        }
}
