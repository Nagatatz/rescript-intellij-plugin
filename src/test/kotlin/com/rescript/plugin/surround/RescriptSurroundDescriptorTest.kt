package com.rescript.plugin.surround

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptSurroundDescriptorTest {
    private val descriptor = RescriptSurroundDescriptor()

    @Test
    fun testGetSurroundersReturnsFourSurrounders() {
        val surrounders = descriptor.surrounders
        assertEquals(4, surrounders.size)
    }

    @Test
    fun testSurroundersOrder() {
        val surrounders = descriptor.surrounders
        assertTrue(surrounders[0] is RescriptIfSurrounder)
        assertTrue(surrounders[1] is RescriptSwitchSurrounder)
        assertTrue(surrounders[2] is RescriptTrySurrounder)
        assertTrue(surrounders[3] is RescriptBlockSurrounder)
    }

    @Test
    fun testIsExclusiveReturnsFalse() {
        assertFalse(descriptor.isExclusive)
    }

    @Test
    fun testIfSurrounderDescription() {
        assertEquals("if (...)", RescriptIfSurrounder().templateDescription)
    }

    @Test
    fun testSwitchSurrounderDescription() {
        assertEquals("switch ...", RescriptSwitchSurrounder().templateDescription)
    }

    @Test
    fun testTrySurrounderDescription() {
        assertEquals("try ... catch", RescriptTrySurrounder().templateDescription)
    }

    @Test
    fun testBlockSurrounderDescription() {
        assertEquals("{ }", RescriptBlockSurrounder().templateDescription)
    }

    @Test
    fun testIfSurrounderGeneratesCorrectTemplate() {
        val surrounder = RescriptIfSurrounder()
        val result = surrounder.generateTemplate("x + 1")
        assertEquals("if (condition) {\n  x + 1\n}", result)
    }

    @Test
    fun testSwitchSurrounderGeneratesCorrectTemplate() {
        val surrounder = RescriptSwitchSurrounder()
        val result = surrounder.generateTemplate("x + 1")
        assertEquals("switch expr {\n| _ => x + 1\n}", result)
    }

    @Test
    fun testTrySurrounderGeneratesCorrectTemplate() {
        val surrounder = RescriptTrySurrounder()
        val result = surrounder.generateTemplate("dangerousCall()")
        assertEquals("try {\n  dangerousCall()\n} catch {\n| exn => ()\n}", result)
    }

    @Test
    fun testBlockSurrounderGeneratesCorrectTemplate() {
        val surrounder = RescriptBlockSurrounder()
        val result = surrounder.generateTemplate("x + 1")
        assertEquals("{\n  x + 1\n}", result)
    }

    @Test
    fun testIfSurrounderCursorOnCondition() {
        val surrounder = RescriptIfSurrounder()
        val template = surrounder.generateTemplate("x + 1")
        val cursorRange = surrounder.getCursorRange(template)
        assertEquals("condition", template.substring(cursorRange.startOffset, cursorRange.endOffset))
    }

    @Test
    fun testSwitchSurrounderCursorOnExpr() {
        val surrounder = RescriptSwitchSurrounder()
        val template = surrounder.generateTemplate("x + 1")
        val cursorRange = surrounder.getCursorRange(template)
        assertEquals("expr", template.substring(cursorRange.startOffset, cursorRange.endOffset))
    }

    @Test
    fun testTrySurrounderCursorOnUnit() {
        val surrounder = RescriptTrySurrounder()
        val template = surrounder.generateTemplate("dangerousCall()")
        val cursorRange = surrounder.getCursorRange(template)
        assertEquals("()", template.substring(cursorRange.startOffset, cursorRange.endOffset))
    }

    @Test
    fun testBlockSurrounderCursorAtEnd() {
        val surrounder = RescriptBlockSurrounder()
        val template = surrounder.generateTemplate("x + 1")
        val cursorRange = surrounder.getCursorRange(template)
        // Cursor should be at the closing brace position (collapsed range)
        assertEquals(cursorRange.startOffset, cursorRange.endOffset)
        assertEquals("}", template.substring(cursorRange.startOffset, cursorRange.startOffset + 1))
    }

    @Test
    fun testIfSurrounderWithMultilineSelection() {
        val surrounder = RescriptIfSurrounder()
        val result = surrounder.generateTemplate("let x = 1\nlet y = 2")
        assertEquals("if (condition) {\n  let x = 1\nlet y = 2\n}", result)
    }

    @Test
    fun testTrySurrounderWithMultilineSelection() {
        val surrounder = RescriptTrySurrounder()
        val result = surrounder.generateTemplate("let x = parse(s)\nlet y = process(x)")
        assertEquals("try {\n  let x = parse(s)\nlet y = process(x)\n} catch {\n| exn => ()\n}", result)
    }

    // -- isApplicable tests --

    @Test
    fun testIsApplicableReturnsTrueForNonEmptyArray() {
        val surrounder = RescriptIfSurrounder()
        val element =
            java.lang.reflect.Proxy.newProxyInstance(
                com.intellij.psi.PsiElement::class.java.classLoader,
                arrayOf(com.intellij.psi.PsiElement::class.java),
            ) { _, method, _ ->
                when (method.name) {
                    "toString" -> "StubElement"
                    "hashCode" -> 0
                    "equals" -> false
                    else -> null
                }
            } as com.intellij.psi.PsiElement
        assertTrue(surrounder.isApplicable(arrayOf(element)))
    }

    @Test
    fun testIsApplicableReturnsFalseForEmptyArray() {
        val surrounder = RescriptIfSurrounder()
        assertFalse(surrounder.isApplicable(emptyArray()))
    }

    @Test
    fun testSwitchSurrounderIsApplicableNonEmpty() {
        val surrounder = RescriptSwitchSurrounder()
        val element =
            java.lang.reflect.Proxy.newProxyInstance(
                com.intellij.psi.PsiElement::class.java.classLoader,
                arrayOf(com.intellij.psi.PsiElement::class.java),
            ) { _, method, _ ->
                when (method.name) {
                    "toString" -> "StubElement"
                    "hashCode" -> 0
                    "equals" -> false
                    else -> null
                }
            } as com.intellij.psi.PsiElement
        assertTrue(surrounder.isApplicable(arrayOf(element)))
    }

    @Test
    fun testTrySurrounderIsApplicableEmpty() {
        val surrounder = RescriptTrySurrounder()
        assertFalse(surrounder.isApplicable(emptyArray()))
    }

    @Test
    fun testBlockSurrounderIsApplicableNonEmpty() {
        val surrounder = RescriptBlockSurrounder()
        val element =
            java.lang.reflect.Proxy.newProxyInstance(
                com.intellij.psi.PsiElement::class.java.classLoader,
                arrayOf(com.intellij.psi.PsiElement::class.java),
            ) { _, method, _ ->
                when (method.name) {
                    "toString" -> "StubElement"
                    "hashCode" -> 0
                    "equals" -> false
                    else -> null
                }
            } as com.intellij.psi.PsiElement
        assertTrue(surrounder.isApplicable(arrayOf(element)))
    }

    @Test
    fun testBlockSurrounderIsApplicableEmpty() {
        val surrounder = RescriptBlockSurrounder()
        assertFalse(surrounder.isApplicable(emptyArray()))
    }

    @Test
    fun testSurroundElementsReturnsNullForEmptyArray() {
        val surrounder = RescriptIfSurrounder()
        val result = surrounder.surroundElements(stubProject(), stubEditor(), emptyArray())
        assertEquals(null, result)
    }

    @Test
    fun testBlockSurrounderWithEmptyContent() {
        val surrounder = RescriptBlockSurrounder()
        val result = surrounder.generateTemplate("")
        assertEquals("{\n  \n}", result)
    }

    @Test
    fun testSwitchSurrounderWithEmptyContent() {
        val surrounder = RescriptSwitchSurrounder()
        val result = surrounder.generateTemplate("")
        assertEquals("switch expr {\n| _ => \n}", result)
    }

    @Test
    fun testTrySurrounderWithEmptyContent() {
        val surrounder = RescriptTrySurrounder()
        val result = surrounder.generateTemplate("")
        assertEquals("try {\n  \n} catch {\n| exn => ()\n}", result)
    }

    @Test
    fun testIfSurrounderWithEmptyContent() {
        val surrounder = RescriptIfSurrounder()
        val result = surrounder.generateTemplate("")
        assertEquals("if (condition) {\n  \n}", result)
    }

    private fun stubProject(): com.intellij.openapi.project.Project =
        java.lang.reflect.Proxy.newProxyInstance(
            com.intellij.openapi.project.Project::class.java.classLoader,
            arrayOf(com.intellij.openapi.project.Project::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "toString" -> "StubProject"
                "hashCode" -> 0
                "equals" -> false
                else -> null
            }
        } as com.intellij.openapi.project.Project

    private fun stubEditor(): com.intellij.openapi.editor.Editor =
        java.lang.reflect.Proxy.newProxyInstance(
            com.intellij.openapi.editor.Editor::class.java.classLoader,
            arrayOf(com.intellij.openapi.editor.Editor::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "toString" -> "StubEditor"
                "hashCode" -> 0
                "equals" -> false
                else -> null
            }
        } as com.intellij.openapi.editor.Editor
}
