package com.rescript.plugin.analysis

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptReanalyzeQuickFixTest {
    @Test
    fun `createQuickFixes returns prefix and remove for unusedVariable`() {
        val fixes = RescriptReanalyzeQuickFix.createQuickFixes("unusedVariable")
        assertEquals(2, fixes.size)
        assertEquals("Prefix with underscore", fixes[0].text)
        assertEquals("Remove unused code", fixes[1].text)
    }

    @Test
    fun `createQuickFixes returns prefix and remove for unusedArgument`() {
        val fixes = RescriptReanalyzeQuickFix.createQuickFixes("unusedArgument")
        assertEquals(2, fixes.size)
        assertEquals("Prefix with underscore", fixes[0].text)
    }

    @Test
    fun `createQuickFixes returns prefix and remove for unusedValue`() {
        val fixes = RescriptReanalyzeQuickFix.createQuickFixes("unusedValue")
        assertEquals(2, fixes.size)
    }

    @Test
    fun `createQuickFixes returns prefix and remove for unusedType`() {
        val fixes = RescriptReanalyzeQuickFix.createQuickFixes("unusedType")
        assertEquals(2, fixes.size)
    }

    @Test
    fun `createQuickFixes returns prefix and remove for unusedModule`() {
        val fixes = RescriptReanalyzeQuickFix.createQuickFixes("unusedModule")
        assertEquals(2, fixes.size)
    }

    @Test
    fun `createQuickFixes returns only remove for deadCode`() {
        val fixes = RescriptReanalyzeQuickFix.createQuickFixes("deadCode")
        assertEquals(1, fixes.size)
        assertEquals("Remove unused code", fixes[0].text)
    }

    @Test
    fun `createQuickFixes returns only remove for deadModule`() {
        val fixes = RescriptReanalyzeQuickFix.createQuickFixes("deadModule")
        assertEquals(1, fixes.size)
        assertEquals("Remove unused code", fixes[0].text)
    }

    @Test
    fun `createQuickFixes returns only remove for deadType`() {
        val fixes = RescriptReanalyzeQuickFix.createQuickFixes("deadType")
        assertEquals(1, fixes.size)
    }

    @Test
    fun `createQuickFixes returns only remove for deadValue`() {
        val fixes = RescriptReanalyzeQuickFix.createQuickFixes("deadValue")
        assertEquals(1, fixes.size)
    }

    @Test
    fun `createQuickFixes returns only remove for deadException`() {
        val fixes = RescriptReanalyzeQuickFix.createQuickFixes("deadException")
        assertEquals(1, fixes.size)
    }

    @Test
    fun `createQuickFixes returns empty for unknown diagnostic`() {
        val fixes = RescriptReanalyzeQuickFix.createQuickFixes("somethingElse")
        assertTrue(fixes.isEmpty())
    }

    @Test
    fun `findWordStart finds beginning of identifier`() {
        assertEquals(4, RescriptReanalyzeQuickFix.findWordStart("let myVar = 42", 6))
    }

    @Test
    fun `findWordStart handles start of text`() {
        assertEquals(0, RescriptReanalyzeQuickFix.findWordStart("myVar", 3))
    }

    @Test
    fun `findWordEnd finds end of identifier`() {
        assertEquals(9, RescriptReanalyzeQuickFix.findWordEnd("let myVar = 42", 4))
    }

    @Test
    fun `findWordEnd handles end of text`() {
        assertEquals(5, RescriptReanalyzeQuickFix.findWordEnd("myVar", 0))
    }

    @Test
    fun `findWordStart handles underscore in identifier`() {
        assertEquals(4, RescriptReanalyzeQuickFix.findWordStart("let my_var = 42", 7))
    }

    @Test
    fun `findWordEnd handles underscore in identifier`() {
        assertEquals(10, RescriptReanalyzeQuickFix.findWordEnd("let my_var = 42", 4))
    }

    @Test
    fun `family name is ReScript reanalyze`() {
        val fix = RescriptReanalyzeQuickFix.PrefixWithUnderscore()
        assertEquals("ReScript reanalyze", fix.familyName)
    }

    @Test
    fun `PrefixWithUnderscore startInWriteAction is true`() {
        val fix = RescriptReanalyzeQuickFix.PrefixWithUnderscore()
        assertTrue(fix.startInWriteAction())
    }

    @Test
    fun `RemoveUnused startInWriteAction is true`() {
        val fix = RescriptReanalyzeQuickFix.RemoveUnused()
        assertTrue(fix.startInWriteAction())
    }

    @Test
    fun `PrefixWithUnderscore isAvailable returns true`() {
        val fix = RescriptReanalyzeQuickFix.PrefixWithUnderscore()
        assertTrue(fix.isAvailable(stubProject(), null, null))
    }

    @Test
    fun `RemoveUnused isAvailable returns true`() {
        val fix = RescriptReanalyzeQuickFix.RemoveUnused()
        assertTrue(fix.isAvailable(stubProject(), null, null))
    }

    @Test
    fun `findWordStart handles empty string`() {
        assertEquals(0, RescriptReanalyzeQuickFix.findWordStart("", 0))
    }

    @Test
    fun `findWordEnd returns offset unchanged when beyond text length`() {
        // When offset exceeds text length, the while loop never enters
        assertEquals(10, RescriptReanalyzeQuickFix.findWordEnd("hello", 10))
    }

    @Test
    fun `findWordEnd handles identifier with apostrophe`() {
        // isIdentifierChar includes apostrophe, so x' is treated as identifier chars
        assertEquals(2, RescriptReanalyzeQuickFix.findWordEnd("x' ", 0))
    }

    @Test
    fun `findWordStart handles identifier with apostrophe`() {
        assertEquals(0, RescriptReanalyzeQuickFix.findWordStart("x' ", 2))
    }

    @Test
    fun `RemoveUnused familyName is ReScript reanalyze`() {
        val fix = RescriptReanalyzeQuickFix.RemoveUnused()
        assertEquals("ReScript reanalyze", fix.familyName)
    }

    @Test
    fun `createQuickFixes for empty string returns empty`() {
        assertTrue(RescriptReanalyzeQuickFix.createQuickFixes("").isEmpty())
    }

    @Test
    fun `findWordStart coerces offset past end`() {
        // offset exceeds text length, coerces to last index
        val result = RescriptReanalyzeQuickFix.findWordStart("hello", 100)
        assertEquals(0, result)
    }

    @Test
    fun `findWordEnd returns offset when beyond length`() {
        assertEquals(10, RescriptReanalyzeQuickFix.findWordEnd("hello", 10))
    }

    @Test
    fun `findWordStart at space looks back to previous word`() {
        // At offset 5 (space), text[i-1]='o' is identifier char, so walks back to 0
        assertEquals(0, RescriptReanalyzeQuickFix.findWordStart("hello world", 5))
    }

    @Test
    fun `findWordEnd at exact boundary`() {
        assertEquals(5, RescriptReanalyzeQuickFix.findWordEnd("hello world", 5))
    }

    @Test
    fun `findWordStart with digits`() {
        assertEquals(4, RescriptReanalyzeQuickFix.findWordStart("let x42 = 0", 6))
    }

    @Test
    fun `findWordEnd with digits`() {
        assertEquals(7, RescriptReanalyzeQuickFix.findWordEnd("let x42 = 0", 4))
    }

    @Test
    fun `PrefixWithUnderscore invoke with null editor does nothing`() {
        val fix = RescriptReanalyzeQuickFix.PrefixWithUnderscore()
        // Should not throw when editor is null
        fix.invoke(stubProject(), null, null)
    }

    @Test
    fun `RemoveUnused invoke with null editor does nothing`() {
        val fix = RescriptReanalyzeQuickFix.RemoveUnused()
        fix.invoke(stubProject(), null, null)
    }

    private fun stubProject(): com.intellij.openapi.project.Project =
        java.lang.reflect.Proxy.newProxyInstance(
            com.intellij.openapi.project.Project::class.java.classLoader,
            arrayOf(com.intellij.openapi.project.Project::class.java),
        ) { proxy, method, _ ->
            when (method.name) {
                "toString" -> "StubProject"
                "hashCode" -> System.identityHashCode(proxy)
                "equals" -> false
                else -> null
            }
        } as com.intellij.openapi.project.Project
}
