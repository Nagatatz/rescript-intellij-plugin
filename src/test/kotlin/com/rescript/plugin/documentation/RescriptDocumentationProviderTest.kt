package com.rescript.plugin.documentation

import com.intellij.lang.documentation.AbstractDocumentationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptDocumentationProviderTest {
    @Test
    fun `instance can be created`() {
        val provider = RescriptDocumentationProvider()
        assertNotNull(provider)
    }

    @Test
    fun `is an AbstractDocumentationProvider`() {
        val provider = RescriptDocumentationProvider()
        assertTrue(provider is AbstractDocumentationProvider)
    }

    // -- MODULE_URL_MAP tests --

    @Test
    fun `MODULE_URL_MAP contains Belt_Array mapping`() {
        assertEquals("belt/array", RescriptDocumentationProvider.MODULE_URL_MAP["Belt.Array"])
    }

    @Test
    fun `MODULE_URL_MAP contains Js_String2 mapping`() {
        assertEquals("js/string-2", RescriptDocumentationProvider.MODULE_URL_MAP["Js.String2"])
    }

    @Test
    fun `MODULE_URL_MAP contains Belt root`() {
        assertEquals("belt", RescriptDocumentationProvider.MODULE_URL_MAP["Belt"])
    }

    @Test
    fun `MODULE_URL_MAP contains Js root`() {
        assertEquals("js", RescriptDocumentationProvider.MODULE_URL_MAP["Js"])
    }

    @Test
    fun `MODULE_URL_MAP contains Belt_List`() {
        assertEquals("belt/list", RescriptDocumentationProvider.MODULE_URL_MAP["Belt.List"])
    }

    @Test
    fun `MODULE_URL_MAP contains Belt_Map`() {
        assertEquals("belt/map", RescriptDocumentationProvider.MODULE_URL_MAP["Belt.Map"])
    }

    @Test
    fun `MODULE_URL_MAP contains Belt_Option`() {
        assertEquals("belt/option", RescriptDocumentationProvider.MODULE_URL_MAP["Belt.Option"])
    }

    @Test
    fun `MODULE_URL_MAP contains Js_Promise`() {
        assertEquals("js/promise", RescriptDocumentationProvider.MODULE_URL_MAP["Js.Promise"])
    }

    @Test
    fun `MODULE_URL_MAP contains Js_Json`() {
        assertEquals("js/json", RescriptDocumentationProvider.MODULE_URL_MAP["Js.Json"])
    }

    @Test
    fun `MODULE_URL_MAP contains Js_Dict`() {
        assertEquals("js/dict", RescriptDocumentationProvider.MODULE_URL_MAP["Js.Dict"])
    }

    @Test
    fun `MODULE_URL_MAP does not contain unknown module`() {
        assertNull(RescriptDocumentationProvider.MODULE_URL_MAP["NonExistent.Module"])
    }

    @Test
    fun `MODULE_URL_MAP is not empty`() {
        assertTrue(RescriptDocumentationProvider.MODULE_URL_MAP.isNotEmpty())
    }

    @Test
    fun `MODULE_URL_MAP contains Belt submodules`() {
        val beltKeys = RescriptDocumentationProvider.MODULE_URL_MAP.keys.filter { it.startsWith("Belt") }
        assertTrue("Expected multiple Belt entries", beltKeys.size > 10)
    }

    @Test
    fun `MODULE_URL_MAP contains Js submodules`() {
        val jsKeys = RescriptDocumentationProvider.MODULE_URL_MAP.keys.filter { it.startsWith("Js") }
        assertTrue("Expected multiple Js entries", jsKeys.size > 10)
    }

    @Test
    fun `getUrlFor returns null for null element`() {
        val provider = RescriptDocumentationProvider()
        val result = provider.getUrlFor(null, null)
        assertNull(result)
    }
}
