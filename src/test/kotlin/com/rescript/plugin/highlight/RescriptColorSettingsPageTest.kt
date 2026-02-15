package com.rescript.plugin.highlight

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptColorSettingsPageTest {
    private val page = RescriptColorSettingsPage()

    @Test
    fun testDisplayName() {
        assertEquals("ReScript", page.displayName)
    }

    @Test
    fun testIconNotNull() {
        assertNotNull(page.icon)
    }

    @Test
    fun testHighlighterType() {
        assertEquals(RescriptSyntaxHighlighter::class, page.highlighter::class)
    }

    @Test
    fun testAttributeDescriptorsNotEmpty() {
        assertTrue(page.attributeDescriptors.isNotEmpty())
    }

    @Test
    fun testAttributeDescriptorsContainLexerAndSemanticEntries() {
        val names = page.attributeDescriptors.map { it.displayName }
        assertTrue("Should contain Keyword", names.contains("Keyword"))
        assertTrue("Should contain String", names.contains("String"))
        assertTrue("Should contain Operator", names.contains("Operator"))
        assertTrue("Should contain Semantic//Variable", names.contains("Semantic//Variable"))
    }

    @Test
    fun testAllDescriptorsHaveNonNullKey() {
        for (desc in page.attributeDescriptors) {
            assertNotNull("Descriptor '${desc.displayName}' should have non-null key", desc.key)
        }
    }

    @Test
    fun testColorDescriptorsEmpty() {
        assertEquals(0, page.colorDescriptors.size)
    }

    @Test
    fun testDemoTextContainsKeywords() {
        val demo = page.demoText
        assertTrue(demo.contains("let"))
        assertTrue(demo.contains("type"))
        assertTrue(demo.contains("module"))
        assertTrue(demo.contains("external"))
        assertTrue(demo.contains("switch"))
    }

    @Test
    fun testDemoTextContainsComments() {
        val demo = page.demoText
        assertTrue(demo.contains("//"))
        assertTrue(demo.contains("/*"))
    }

    @Test
    fun testAdditionalTagMapNotEmpty() {
        val tagMap = page.additionalHighlightingTagToDescriptorMap
        assertNotNull(tagMap)
        assertTrue(tagMap.isNotEmpty())
    }

    @Test
    fun testAdditionalTagMapContainsExpectedTags() {
        val tagMap = page.additionalHighlightingTagToDescriptorMap
        assertTrue(tagMap.containsKey("var"))
        assertTrue(tagMap.containsKey("typ"))
        assertTrue(tagMap.containsKey("ns"))
        assertTrue(tagMap.containsKey("enum"))
        assertTrue(tagMap.containsKey("prop"))
    }
}
