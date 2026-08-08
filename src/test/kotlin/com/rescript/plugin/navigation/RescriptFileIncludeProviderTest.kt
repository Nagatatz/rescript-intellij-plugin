package com.rescript.plugin.navigation

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.indexing.IndexedFile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class RescriptFileIncludeProviderTest {
    private val provider = RescriptFileIncludeProvider()

    /** Builds an [IndexedFile] whose backing [VirtualFile] reports the given extension. */
    private fun indexedFileWithExtension(extension: String?): IndexedFile {
        val virtualFile = mock(VirtualFile::class.java)
        `when`(virtualFile.extension).thenReturn(extension)
        val indexedFile = mock(IndexedFile::class.java)
        `when`(indexedFile.file).thenReturn(virtualFile)
        return indexedFile
    }

    @Test
    fun testProviderCanBeInstantiated() {
        assertNotNull(provider)
    }

    @Test
    fun testGetId() {
        assertEquals("rescript", provider.id)
    }

    // -- acceptFile(IndexedFile) tests --
    //
    // 2026.2 deprecated acceptFile(VirtualFile) in favour of the IndexedFile overload;
    // these cover the replacement so the file-type filter cannot regress silently.

    @Test
    fun testAcceptFileAcceptsResExtension() {
        assertTrue(provider.acceptFile(indexedFileWithExtension("res")))
    }

    @Test
    fun testAcceptFileAcceptsResiExtension() {
        assertTrue(provider.acceptFile(indexedFileWithExtension("resi")))
    }

    @Test
    fun testAcceptFileRejectsOtherExtension() {
        assertFalse(provider.acceptFile(indexedFileWithExtension("kt")))
    }

    @Test
    fun testAcceptFileRejectsMissingExtension() {
        assertFalse(provider.acceptFile(indexedFileWithExtension(null)))
    }

    // -- extractModuleNames tests --

    @Test
    fun testExtractSingleOpenStatement() {
        val text = "open Belt"
        val result = RescriptFileIncludeProvider.extractModuleNames(text)
        assertEquals(listOf("Belt"), result)
    }

    @Test
    fun testExtractDottedModulePath() {
        val text = "open Belt.Array"
        val result = RescriptFileIncludeProvider.extractModuleNames(text)
        assertEquals(listOf("Belt.Array"), result)
    }

    @Test
    fun testExtractMultipleOpenStatements() {
        val text =
            """
            open Belt
            open Js.Array2
            open ReactDOM
            """.trimIndent()
        val result = RescriptFileIncludeProvider.extractModuleNames(text)
        assertEquals(3, result.size)
        assertEquals("Belt", result[0])
        assertEquals("Js.Array2", result[1])
        assertEquals("ReactDOM", result[2])
    }

    @Test
    fun testExtractWithLeadingWhitespace() {
        val text = "  open Belt"
        val result = RescriptFileIncludeProvider.extractModuleNames(text)
        assertEquals(listOf("Belt"), result)
    }

    @Test
    fun testNoMatchForLowercaseModule() {
        val text = "open belt"
        val result = RescriptFileIncludeProvider.extractModuleNames(text)
        assertTrue(result.isEmpty())
    }

    @Test
    fun testNoMatchForNonOpenStatement() {
        val text = "let x = 1"
        val result = RescriptFileIncludeProvider.extractModuleNames(text)
        assertTrue(result.isEmpty())
    }

    @Test
    fun testNoMatchForEmptyText() {
        val result = RescriptFileIncludeProvider.extractModuleNames("")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testExtractIgnoresCommentedOpen() {
        val text =
            """
            open Belt
            // open Commented
            let x = 1
            """.trimIndent()
        val result = RescriptFileIncludeProvider.extractModuleNames(text)
        // The regex matches lines starting with optional whitespace + "open",
        // so "// open Commented" won't match (starts with "//")
        assertEquals(1, result.size)
        assertEquals("Belt", result[0])
    }

    // -- moduleNameToFileName tests --

    @Test
    fun testSimpleModuleToFileName() {
        assertEquals("Belt.res", RescriptFileIncludeProvider.moduleNameToFileName("Belt"))
    }

    @Test
    fun testDottedModuleToFileName() {
        assertEquals("Array.res", RescriptFileIncludeProvider.moduleNameToFileName("Belt.Array"))
    }

    @Test
    fun testDeepModulePathToFileName() {
        assertEquals("Map.res", RescriptFileIncludeProvider.moduleNameToFileName("Belt.Map.String.Map"))
    }
}
