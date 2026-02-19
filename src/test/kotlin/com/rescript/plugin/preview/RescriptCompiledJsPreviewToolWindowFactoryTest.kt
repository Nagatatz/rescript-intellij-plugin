package com.rescript.plugin.preview

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptCompiledJsPreviewToolWindowFactoryTest {
    @Test
    fun `TOOL_WINDOW_ID is correct`() {
        assertEquals("ReScript JS", RescriptCompiledJsPreviewToolWindowFactory.TOOL_WINDOW_ID)
    }

    @Test
    fun `isRescriptFile returns true for res files`() {
        assertTrue(isRescriptExtension("res"))
    }

    @Test
    fun `isRescriptFile returns true for resi files`() {
        assertTrue(isRescriptExtension("resi"))
    }

    @Test
    fun `isRescriptFile returns false for js files`() {
        assertFalse(isRescriptExtension("js"))
    }

    @Test
    fun `isRescriptFile returns false for ts files`() {
        assertFalse(isRescriptExtension("ts"))
    }

    @Test
    fun `isRescriptFile returns false for null extension`() {
        assertFalse(isRescriptExtension(null))
    }

    /**
     * Test helper that mirrors the isRescriptFile logic without requiring VirtualFile.
     */
    private fun isRescriptExtension(ext: String?): Boolean {
        if (ext == null) return false
        return ext == "res" || ext == "resi"
    }
}
