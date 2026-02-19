package com.rescript.plugin.folding

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptCustomFoldingProviderTest {
    private val provider = RescriptCustomFoldingProvider()

    @Test
    fun `isCustomRegionStart detects region without space`() {
        assertTrue(provider.isCustomRegionStart("//#region My Region"))
    }

    @Test
    fun `isCustomRegionStart detects region with space`() {
        assertTrue(provider.isCustomRegionStart("// #region My Region"))
    }

    @Test
    fun `isCustomRegionStart handles leading whitespace`() {
        assertTrue(provider.isCustomRegionStart("  //#region Indented"))
    }

    @Test
    fun `isCustomRegionStart rejects normal comments`() {
        assertFalse(provider.isCustomRegionStart("// regular comment"))
    }

    @Test
    fun `isCustomRegionEnd detects endregion without space`() {
        assertTrue(provider.isCustomRegionEnd("//#endregion"))
    }

    @Test
    fun `isCustomRegionEnd detects endregion with space`() {
        assertTrue(provider.isCustomRegionEnd("// #endregion"))
    }

    @Test
    fun `isCustomRegionEnd rejects normal comments`() {
        assertFalse(provider.isCustomRegionEnd("// not an endregion"))
    }

    @Test
    fun `getPlaceholderText extracts region name`() {
        assertEquals("My Region", provider.getPlaceholderText("//#region My Region"))
    }

    @Test
    fun `getPlaceholderText returns ellipsis for unnamed region`() {
        assertEquals("...", provider.getPlaceholderText("//#region"))
    }

    @Test
    fun `getPlaceholderText handles space variant`() {
        assertEquals("Utils", provider.getPlaceholderText("// #region Utils"))
    }

    @Test
    fun `getDescription returns expected text`() {
        assertEquals("//#region ... //#endregion", provider.getDescription())
    }

    @Test
    fun `getStartString returns region marker`() {
        assertEquals("//#region", provider.getStartString())
    }

    @Test
    fun `getEndString returns endregion marker`() {
        assertEquals("//#endregion", provider.getEndString())
    }

    // -- Additional edge cases --

    @Test
    fun `isCustomRegionEnd handles leading whitespace`() {
        assertTrue(provider.isCustomRegionEnd("  //#endregion"))
    }

    @Test
    fun `isCustomRegionEnd handles tab leading whitespace`() {
        assertTrue(provider.isCustomRegionEnd("\t//#endregion"))
    }

    @Test
    fun `isCustomRegionEnd with space variant and leading whitespace`() {
        assertTrue(provider.isCustomRegionEnd("    // #endregion"))
    }

    @Test
    fun `getPlaceholderText returns ellipsis for region with only spaces after prefix`() {
        assertEquals("...", provider.getPlaceholderText("//#region   "))
    }

    @Test
    fun `getPlaceholderText returns ellipsis for space variant unnamed region`() {
        assertEquals("...", provider.getPlaceholderText("// #region"))
    }

    @Test
    fun `getPlaceholderText returns ellipsis for space variant with trailing spaces`() {
        assertEquals("...", provider.getPlaceholderText("// #region   "))
    }

    @Test
    fun `getPlaceholderText with leading whitespace`() {
        assertEquals("Section", provider.getPlaceholderText("  //#region Section"))
    }

    @Test
    fun `isCustomRegionStart rejects endregion`() {
        assertFalse(provider.isCustomRegionStart("//#endregion"))
    }

    @Test
    fun `isCustomRegionEnd rejects region start`() {
        assertFalse(provider.isCustomRegionEnd("//#region Foo"))
    }
}
