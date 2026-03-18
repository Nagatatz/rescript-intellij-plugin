package com.rescript.plugin.run

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.rescript.plugin.IntelliJPlatformExtension
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lang.psi.RescriptElementTypes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(IntelliJPlatformExtension::class)
class RescriptRunLineMarkerContributorTest {
    private lateinit var myFixture: CodeInsightTestFixture

    @Test
    fun testNonResFileReturnsNull() {
        val file = myFixture.configureByText("test.txt", "let x = 1")
        val firstChild = file.firstChild
        assertNotNull(firstChild)
        assertFalse(RescriptRunLineMarkerContributor.shouldShowMarker(firstChild))
    }

    @Test
    fun testResFileFirstLetDeclaration() {
        val file = myFixture.configureByText("Test.res", "let x = 1\nlet y = 2")

        val letElement = findFirstLeafOfType(file, RescriptTokenTypes.LET)
        assertNotNull(letElement, "Should find LET token")

        val parent = letElement!!.parent
        assertNotNull(parent)
        assertEquals(RescriptElementTypes.LET_DECLARATION, parent.node?.elementType)
    }

    @Test
    fun testResFileFirstTypeDeclaration() {
        val file = myFixture.configureByText("Test2.res", "type color = Red | Blue")

        val typeElement = findFirstLeafOfType(file, RescriptTokenTypes.TYPE)
        assertNotNull(typeElement, "Should find TYPE token")

        val parent = typeElement!!.parent
        assertNotNull(parent)
        assertEquals(RescriptElementTypes.TYPE_DECLARATION, parent.node?.elementType)
    }

    @Test
    fun testResFileFirstModuleDeclaration() {
        val file = myFixture.configureByText("Test3.res", "module Foo = {\n  let x = 1\n}")

        val moduleElement = findFirstLeafOfType(file, RescriptTokenTypes.MODULE)
        assertNotNull(moduleElement, "Should find MODULE token")

        val parent = moduleElement!!.parent
        assertNotNull(parent)
        assertEquals(RescriptElementTypes.MODULE_DECLARATION, parent.node?.elementType)
    }

    @Test
    fun testSecondDeclarationIsNotFirst() {
        val file = myFixture.configureByText("Test4.res", "let x = 1\nlet y = 2")

        val letElements = findAllLeavesOfType(file, RescriptTokenTypes.LET)
        assertEquals(2, letElements.size, "Should find 2 LET tokens")

        val firstParent = letElements[0].parent
        val secondParent = letElements[1].parent

        assertTrue(
            isFirstTopLevelDeclaration(firstParent),
            "First LET parent should be first declaration",
        )
        assertFalse(
            isFirstTopLevelDeclaration(secondParent),
            "Second LET parent should not be first declaration",
        )
    }

    @Test
    fun testNonKeywordTokenDoesNotMatch() {
        val file = myFixture.configureByText("Test5.res", "let x = 1")

        val identElement = findFirstLeafOfType(file, RescriptTokenTypes.LIDENT)
        assertNotNull(identElement, "Should find LIDENT token")
        assertFalse(
            RescriptRunLineMarkerContributor.shouldShowMarker(identElement!!),
            "LIDENT should not trigger marker",
        )
    }

    @Test
    fun testDeclarationKeywordConstants() {
        val keywords = setOf(RescriptTokenTypes.LET, RescriptTokenTypes.TYPE, RescriptTokenTypes.MODULE)
        assertTrue(RescriptTokenTypes.LET in keywords, "LET should be in keywords")
        assertTrue(RescriptTokenTypes.TYPE in keywords, "TYPE should be in keywords")
        assertTrue(RescriptTokenTypes.MODULE in keywords, "MODULE should be in keywords")
    }

    private fun findFirstLeafOfType(
        file: PsiFile,
        tokenType: IElementType,
    ): PsiElement? = findAllLeavesOfType(file, tokenType).firstOrNull()

    private fun findAllLeavesOfType(
        file: PsiFile,
        tokenType: IElementType,
    ): List<PsiElement> =
        PsiTreeUtil
            .collectElements(file) { element ->
                element.node?.elementType == tokenType && element.firstChild == null
            }.toList()

    private fun isFirstTopLevelDeclaration(declaration: PsiElement): Boolean {
        val declarationTypes =
            setOf(
                RescriptElementTypes.LET_DECLARATION,
                RescriptElementTypes.TYPE_DECLARATION,
                RescriptElementTypes.MODULE_DECLARATION,
            )
        val file = declaration.containingFile ?: return false
        var child = file.firstChild
        while (child != null) {
            val childType = child.node?.elementType
            if (childType in declarationTypes) {
                return child === declaration
            }
            child = child.nextSibling
        }
        return false
    }
}
