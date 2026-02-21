package com.rescript.plugin.codestyle

import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.rescript.plugin.RescriptLanguage
import junit.framework.TestCase

class RescriptPredefinedCodeStyleTest : BasePlatformTestCase() {
    fun testNameIsReScriptStandard() {
        val style = RescriptPredefinedCodeStyle()
        TestCase.assertEquals("ReScript Standard", style.name)
    }

    fun testApplySets2SpaceIndentWithNoTabs() {
        val style = RescriptPredefinedCodeStyle()
        val settings = CodeStyleSettings.getDefaults()
        style.apply(settings, RescriptLanguage)

        val indentOptions = settings.getCommonSettings(RescriptLanguage).indentOptions!!
        TestCase.assertEquals(2, indentOptions.INDENT_SIZE)
        TestCase.assertEquals(2, indentOptions.CONTINUATION_INDENT_SIZE)
        TestCase.assertEquals(2, indentOptions.TAB_SIZE)
        TestCase.assertFalse(indentOptions.USE_TAB_CHARACTER)
    }
}
