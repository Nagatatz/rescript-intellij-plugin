package com.rescript.plugin.codestyle

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.rescript.plugin.RescriptLanguage

/**
 * Integration test for ReScript indentation settings using the full IDE platform.
 *
 * Verifies that the ReScript code style provider correctly registers indent
 * options and that they match the expected values.
 */
class RescriptIndentIntegrationTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String = "src/test/testData/indent"

    fun testIndentSettingsApplied() {
        val settings =
            com.intellij.psi.codeStyle.CodeStyleSettingsManager
                .getInstance(project)
                .mainProjectCodeStyle!!
        val indentOptions = settings.getCommonSettings(RescriptLanguage).indentOptions
        assertNotNull("Expected indent options for ReScript", indentOptions)
    }

    fun testIndentSize() {
        val settings =
            com.intellij.psi.codeStyle.CodeStyleSettingsManager
                .getInstance(project)
                .mainProjectCodeStyle!!
        val indentOptions = settings.getCommonSettings(RescriptLanguage).indentOptions!!
        assertEquals("Expected indent size of 2", 2, indentOptions.INDENT_SIZE)
    }

    fun testUseSpacesNotTabs() {
        val settings =
            com.intellij.psi.codeStyle.CodeStyleSettingsManager
                .getInstance(project)
                .mainProjectCodeStyle!!
        val indentOptions = settings.getCommonSettings(RescriptLanguage).indentOptions!!
        assertFalse("Expected spaces, not tabs", indentOptions.USE_TAB_CHARACTER)
    }

    fun testContinuationIndent() {
        val settings =
            com.intellij.psi.codeStyle.CodeStyleSettingsManager
                .getInstance(project)
                .mainProjectCodeStyle!!
        val indentOptions = settings.getCommonSettings(RescriptLanguage).indentOptions!!
        assertEquals("Expected continuation indent of 2", 2, indentOptions.CONTINUATION_INDENT_SIZE)
    }
}
