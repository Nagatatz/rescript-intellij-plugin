package com.rescript.plugin.highlight

import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerFactory
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptHighlightUsagesHandlerFactoryTest {
    @Test
    fun `instance can be created`() {
        val factory = RescriptHighlightUsagesHandlerFactory()
        assertNotNull(factory)
    }

    @Test
    fun `is a HighlightUsagesHandlerFactory`() {
        val factory = RescriptHighlightUsagesHandlerFactory()
        assertTrue(factory is HighlightUsagesHandlerFactory)
    }

    @Test
    fun `KeywordKind enum has expected values`() {
        val values = KeywordKind.entries
        assertTrue(values.contains(KeywordKind.SWITCH))
        assertTrue(values.contains(KeywordKind.IF))
        assertTrue(values.contains(KeywordKind.TRY))
        assertTrue(values.contains(KeywordKind.PIPE))
        assertTrue(values.contains(KeywordKind.CATCH))
        assertTrue(values.contains(KeywordKind.ELSE))
    }

    @Test
    fun `KeywordKind enum has exactly six values`() {
        assertEquals(6, KeywordKind.entries.size)
    }

    private fun assertEquals(
        expected: Int,
        actual: Int,
    ) {
        org.junit.Assert.assertEquals(expected, actual)
    }
}
