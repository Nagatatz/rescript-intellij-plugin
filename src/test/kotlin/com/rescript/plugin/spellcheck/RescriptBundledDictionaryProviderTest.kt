package com.rescript.plugin.spellcheck

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptBundledDictionaryProviderTest {
    private val provider = RescriptBundledDictionaryProvider()

    @Test
    fun `getBundledDictionaries returns non-empty array`() {
        val dictionaries = provider.bundledDictionaries
        assertTrue(dictionaries.isNotEmpty())
    }

    @Test
    fun `dictionary path points to rescript dic`() {
        val dictionaries = provider.bundledDictionaries
        assertTrue(dictionaries.any { it.contains("rescript.dic") })
    }

    @Test
    fun `dictionary resource file exists on classpath`() {
        val path = provider.bundledDictionaries.first()
        val resource = javaClass.getResource(path)
        assertNotNull("Dictionary resource file should exist at $path", resource)
    }

    @Test
    fun `dictionary contains common ReScript terms`() {
        val path = provider.bundledDictionaries.first()
        val content = javaClass.getResource(path)!!.readText()
        val words = content.lines().map { it.trim() }.filter { it.isNotEmpty() }

        val expectedTerms = listOf("rescript", "genType", "Belt", "Js", "functor", "polyvariant")
        for (term in expectedTerms) {
            assertTrue(
                "Dictionary should contain '$term'",
                words.any { it.equals(term, ignoreCase = false) },
            )
        }
    }
}
