package com.rescript.plugin.wizard.templates

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TemplateResourceLoaderTest {
    @Test
    fun `loads a resource verbatim when no placeholders are present`() {
        val content = TemplateResourceLoader.load("__test__/basic.txt")
        assertEquals("hello world\nline 2\n", content)
    }

    @Test
    fun `substitutes placeholders with supplied vars`() {
        val content =
            TemplateResourceLoader.load(
                "__test__/with-placeholder.txt",
                mapOf("projectName" to "demo", "installCmd" to "pnpm install"),
            )
        assertEquals("project: demo\ncmd: pnpm install\n", content)
    }

    @Test
    fun `strict mode throws when a placeholder is missing from vars`() {
        val ex =
            assertThrows(IllegalStateException::class.java) {
                TemplateResourceLoader.load(
                    "__test__/with-placeholder.txt",
                    mapOf("projectName" to "demo"),
                )
            }
        assertTrue(ex.message!!.contains("installCmd"))
        assertTrue(ex.message!!.contains("with-placeholder.txt"))
    }

    @Test
    fun `non-strict mode leaves unsubstituted placeholders verbatim`() {
        val content =
            TemplateResourceLoader.load(
                "__test__/with-placeholder.txt",
                mapOf("projectName" to "demo"),
                strict = false,
            )
        assertEquals("project: demo\ncmd: {{installCmd}}\n", content)
    }

    @Test
    fun `throws when the resource path does not exist`() {
        val ex =
            assertThrows(IllegalStateException::class.java) {
                TemplateResourceLoader.load("__test__/does-not-exist.txt")
            }
        assertTrue(ex.message!!.contains("does-not-exist"))
        assertTrue(ex.message!!.contains("Template resource not found"))
    }

    @Test
    fun `preserves UTF-8 multi-byte content`() {
        val content = TemplateResourceLoader.load("__test__/utf8.txt")
        assertTrue(content.contains("こんにちは世界"))
        assertTrue(content.contains("漢字テスト"))
        assertTrue(content.contains("𝐇𝐞𝐥𝐥𝐨"))
    }
}
