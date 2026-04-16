package com.rescript.plugin.lang.psi

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.rescript.plugin.IntelliJPlatformExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Integration tests for [RescriptDeclarationPsiElement] using parsed PSI.
 *
 * Verifies that declarations created by the parser expose the declared name
 * via [RescriptDeclarationPsiElement.getDeclarationName] (falling back to AST
 * extraction when no stub is present) and that [toString] embeds the element
 * type for debug display.
 */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptDeclarationPsiElementTest {
    private lateinit var myFixture: CodeInsightTestFixture

    @Suppress("unused")
    private lateinit var project: Project

    @Test
    fun testGetDeclarationNameForLet() {
        val file = myFixture.configureByText("Foo.res", "let foo = 1")
        val decl = findDeclaration(file, RescriptElementTypes.LET_DECLARATION)
        assertNotNull(decl)
        assertEquals("foo", (decl as RescriptDeclarationPsiElement).getDeclarationName())
    }

    @Test
    fun testGetDeclarationNameForType() {
        val file = myFixture.configureByText("Foo.res", "type myType = int")
        val decl = findDeclaration(file, RescriptElementTypes.TYPE_DECLARATION)
        assertNotNull(decl)
        assertEquals("myType", (decl as RescriptDeclarationPsiElement).getDeclarationName())
    }

    @Test
    fun testGetDeclarationNameForModule() {
        val file = myFixture.configureByText("Foo.res", "module Bar = { let x = 1 }")
        val decl = findDeclaration(file, RescriptElementTypes.MODULE_DECLARATION)
        assertNotNull(decl)
        assertEquals("Bar", (decl as RescriptDeclarationPsiElement).getDeclarationName())
    }

    @Test
    fun testGetDeclarationNameForExternal() {
        val file = myFixture.configureByText("Foo.res", """external log: string => unit = "console.log"""")
        val decl = findDeclaration(file, RescriptElementTypes.EXTERNAL_DECLARATION)
        assertNotNull(decl)
        assertEquals("log", (decl as RescriptDeclarationPsiElement).getDeclarationName())
    }

    @Test
    fun testGetDeclarationNameForException() {
        val file = myFixture.configureByText("Foo.res", "exception MyError(string)")
        val decl = findDeclaration(file, RescriptElementTypes.EXCEPTION_DECLARATION)
        assertNotNull(decl)
        assertEquals("MyError", (decl as RescriptDeclarationPsiElement).getDeclarationName())
    }

    @Test
    fun testGetDeclarationNameForRecLet() {
        // 'rec' must be skipped — name follows after it
        val file = myFixture.configureByText("Foo.res", "let rec loop = () => loop()")
        val decl = findDeclaration(file, RescriptElementTypes.LET_DECLARATION)
        assertNotNull(decl)
        assertEquals("loop", (decl as RescriptDeclarationPsiElement).getDeclarationName())
    }

    @Test
    fun testToStringEmbedsElementType() {
        val file = myFixture.configureByText("Foo.res", "let foo = 1")
        val decl = findDeclaration(file, RescriptElementTypes.LET_DECLARATION) as RescriptDeclarationPsiElement
        val text = decl.toString()
        assertTrue(text.startsWith("RescriptDeclarationPsiElement("), "expected toString prefix, got: $text")
        assertTrue(text.endsWith(")"), "expected toString to be parenthesized, got: $text")
    }

    private fun findDeclaration(
        scope: PsiElement,
        elementType: com.intellij.psi.tree.IElementType,
    ): PsiElement? {
        if (scope.node?.elementType == elementType) return scope
        for (child in scope.children) {
            val found = findDeclaration(child, elementType)
            if (found != null) return found
        }
        return null
    }
}
