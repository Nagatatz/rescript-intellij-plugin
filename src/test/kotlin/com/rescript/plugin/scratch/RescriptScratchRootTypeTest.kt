package com.rescript.plugin.scratch

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptScratchRootTypeTest {
    @Test
    fun `scratch root type can be instantiated`() {
        val rootType = RescriptScratchRootType()
        assertNotNull(rootType)
    }

    @Test
    fun `scratch root type has correct display name`() {
        val rootType = RescriptScratchRootType()
        assertEquals("ReScript", rootType.displayName)
    }

    @Test
    fun `generateTemplate returns valid ReScript code`() {
        val template = RescriptScratchCreationHelper.generateTemplate()
        assertTrue(template.contains("let"))
        assertTrue(template.contains("Js.log"))
    }

    @Test
    fun `generateTemplate is non-empty`() {
        val template = RescriptScratchCreationHelper.generateTemplate()
        assertTrue(template.isNotBlank())
    }

    @Test
    fun `generateTemplate starts with comment`() {
        val template = RescriptScratchCreationHelper.generateTemplate()
        assertTrue(template.startsWith("//"))
    }
}
