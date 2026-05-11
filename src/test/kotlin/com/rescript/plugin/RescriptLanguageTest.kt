package com.rescript.plugin

import com.intellij.lang.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Verifies the singleton ReScript [Language] definition: identifier,
 * case sensitivity, and that the platform's `Language.findInstance`
 * lookup returns the same object.
 */
class RescriptLanguageTest {
    @Test
    fun `id is ReScript`() {
        assertEquals("ReScript", RescriptLanguage.id)
    }

    @Test
    fun `case sensitive matches ReScript identifier rules`() {
        assertTrue(RescriptLanguage.isCaseSensitive)
    }

    @Test
    fun `findInstance returns the same singleton`() {
        val resolved = Language.findInstance(RescriptLanguage::class.java)
        assertSame(RescriptLanguage, resolved)
    }

    @Test
    fun `findLanguageByID resolves the registered id`() {
        assertSame(RescriptLanguage, Language.findLanguageByID("ReScript"))
    }
}
