package com.rescript.plugin.lang

import com.rescript.plugin.lang.psi.RescriptStringLiteral
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptAstFactoryTest {
    private val factory = RescriptAstFactory()

    @Test
    fun `createLeaf returns RescriptStringLiteral for STRING_VALUE token`() {
        val result = factory.createLeaf(RescriptTokenTypes.STRING_VALUE, "\"hello\"")
        assertNotNull(result)
        assertTrue(result is RescriptStringLiteral)
        assertEquals("\"hello\"", result?.text)
    }

    @Test
    fun `createLeaf returns null for non-STRING_VALUE token`() {
        val result = factory.createLeaf(RescriptTokenTypes.LIDENT, "foo")
        assertNull(result)
    }

    @Test
    fun `createLeaf returns null for keyword token`() {
        val result = factory.createLeaf(RescriptTokenTypes.LET, "let")
        assertNull(result)
    }

    @Test
    fun `createLeaf handles empty string literal`() {
        val result = factory.createLeaf(RescriptTokenTypes.STRING_VALUE, "\"\"")
        assertNotNull(result)
        assertTrue(result is RescriptStringLiteral)
    }
}
