package com.rescript.plugin.codestyle

import com.intellij.openapi.project.Project
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.rescript.plugin.IntelliJPlatformExtension
import com.rescript.plugin.RescriptLanguage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Integration test for ReScript indentation settings using the full IDE platform.
 *
 * Verifies that the ReScript code style provider correctly registers indent
 * options and that they match the expected values.
 */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptIndentIntegrationTest {
    @Suppress("unused")
    private lateinit var myFixture: CodeInsightTestFixture
    private lateinit var project: Project
    private val testDataPath: String = "src/test/testData/indent"

    @Test
    fun testIndentSettingsApplied() {
        val settings =
            com.intellij.psi.codeStyle.CodeStyleSettingsManager
                .getInstance(project)
                .mainProjectCodeStyle!!
        val indentOptions = settings.getCommonSettings(RescriptLanguage).indentOptions
        assertNotNull(indentOptions, "Expected indent options for ReScript")
    }

    @Test
    fun testIndentSize() {
        val settings =
            com.intellij.psi.codeStyle.CodeStyleSettingsManager
                .getInstance(project)
                .mainProjectCodeStyle!!
        val indentOptions = settings.getCommonSettings(RescriptLanguage).indentOptions!!
        assertEquals(2, indentOptions.INDENT_SIZE, "Expected indent size of 2")
    }

    @Test
    fun testUseSpacesNotTabs() {
        val settings =
            com.intellij.psi.codeStyle.CodeStyleSettingsManager
                .getInstance(project)
                .mainProjectCodeStyle!!
        val indentOptions = settings.getCommonSettings(RescriptLanguage).indentOptions!!
        assertFalse(indentOptions.USE_TAB_CHARACTER, "Expected spaces, not tabs")
    }

    @Test
    fun testContinuationIndent() {
        val settings =
            com.intellij.psi.codeStyle.CodeStyleSettingsManager
                .getInstance(project)
                .mainProjectCodeStyle!!
        val indentOptions = settings.getCommonSettings(RescriptLanguage).indentOptions!!
        assertEquals(2, indentOptions.CONTINUATION_INDENT_SIZE, "Expected continuation indent of 2")
    }
}
