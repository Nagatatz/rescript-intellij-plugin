package com.rescript.plugin.highlight

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

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
        assertTrue(names.contains("Keyword"), "Should contain Keyword")
        assertTrue(names.contains("String"), "Should contain String")
        assertTrue(names.contains("Operator"), "Should contain Operator")
        assertTrue(names.contains("Semantic//Variable"), "Should contain Semantic//Variable")
    }

    @Test
    fun testAllDescriptorsHaveNonNullKey() {
        for (desc in page.attributeDescriptors) {
            assertNotNull(desc.key, "Descriptor '${desc.displayName}' should have non-null key")
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
