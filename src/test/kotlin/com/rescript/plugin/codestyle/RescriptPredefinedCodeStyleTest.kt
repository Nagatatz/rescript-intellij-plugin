package com.rescript.plugin.codestyle

import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.rescript.plugin.IntelliJPlatformExtension
import com.rescript.plugin.RescriptLanguage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(IntelliJPlatformExtension::class)
class RescriptPredefinedCodeStyleTest {
    @Suppress("unused")
    private lateinit var myFixture: CodeInsightTestFixture

    @Test
    fun testNameIsReScriptStandard() {
        val style = RescriptPredefinedCodeStyle()
        assertEquals("ReScript Standard", style.name)
    }

    @Test
    fun testApplySets2SpaceIndentWithNoTabs() {
        val style = RescriptPredefinedCodeStyle()
        val settings = CodeStyleSettings.getDefaults()
        style.apply(settings, RescriptLanguage)

        val indentOptions = settings.getCommonSettings(RescriptLanguage).indentOptions!!
        assertEquals(2, indentOptions.INDENT_SIZE)
        assertEquals(2, indentOptions.CONTINUATION_INDENT_SIZE)
        assertEquals(2, indentOptions.TAB_SIZE)
        assertFalse(indentOptions.USE_TAB_CHARACTER)
    }
}
