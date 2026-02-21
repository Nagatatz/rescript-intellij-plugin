package com.rescript.plugin.codestyle

import com.intellij.psi.codeStyle.CodeStyleSettings
import com.rescript.plugin.RescriptLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class RescriptPredefinedCodeStyleTest {
    @Test
    fun `name is ReScript Standard`() {
        val style = RescriptPredefinedCodeStyle()
        assertEquals("ReScript Standard", style.name)
    }

    @Test
    fun `apply sets 2-space indent with no tabs`() {
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
