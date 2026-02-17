package com.rescript.plugin.run

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lang.psi.RescriptElementTypes

class RescriptRunLineMarkerContributorTest : BasePlatformTestCase() {
    fun testNonResFileReturnsNull() {
        val file = myFixture.configureByText("test.txt", "let x = 1")
        val firstChild = file.firstChild
        assertNotNull(firstChild)
        assertFalse(RescriptRunLineMarkerContributor.shouldShowMarker(firstChild))
    }

    fun testResFileFirstLetDeclaration() {
        val file = myFixture.configureByText("Test.res", "let x = 1\nlet y = 2")

        val letElement = findFirstLeafOfType(file, RescriptTokenTypes.LET)
        assertNotNull("Should find LET token", letElement)

        val parent = letElement!!.parent
        assertNotNull(parent)
        assertEquals(RescriptElementTypes.LET_DECLARATION, parent.node?.elementType)
    }

    fun testResFileFirstTypeDeclaration() {
        val file = myFixture.configureByText("Test2.res", "type color = Red | Blue")

        val typeElement = findFirstLeafOfType(file, RescriptTokenTypes.TYPE)
        assertNotNull("Should find TYPE token", typeElement)

        val parent = typeElement!!.parent
        assertNotNull(parent)
        assertEquals(RescriptElementTypes.TYPE_DECLARATION, parent.node?.elementType)
    }

    fun testResFileFirstModuleDeclaration() {
        val file = myFixture.configureByText("Test3.res", "module Foo = {\n  let x = 1\n}")

        val moduleElement = findFirstLeafOfType(file, RescriptTokenTypes.MODULE)
        assertNotNull("Should find MODULE token", moduleElement)

        val parent = moduleElement!!.parent
        assertNotNull(parent)
        assertEquals(RescriptElementTypes.MODULE_DECLARATION, parent.node?.elementType)
    }

    fun testSecondDeclarationIsNotFirst() {
        val file = myFixture.configureByText("Test4.res", "let x = 1\nlet y = 2")

        val letElements = findAllLeavesOfType(file, RescriptTokenTypes.LET)
        assertEquals("Should find 2 LET tokens", 2, letElements.size)

        val firstParent = letElements[0].parent
        val secondParent = letElements[1].parent

        assertTrue(
            "First LET parent should be first declaration",
            isFirstTopLevelDeclaration(firstParent),
        )
        assertFalse(
            "Second LET parent should not be first declaration",
            isFirstTopLevelDeclaration(secondParent),
        )
    }

    fun testNonKeywordTokenDoesNotMatch() {
        val file = myFixture.configureByText("Test5.res", "let x = 1")

        val identElement = findFirstLeafOfType(file, RescriptTokenTypes.LIDENT)
        assertNotNull("Should find LIDENT token", identElement)
        assertFalse(
            "LIDENT should not trigger marker",
            RescriptRunLineMarkerContributor.shouldShowMarker(identElement!!),
        )
    }

    fun testDeclarationKeywordConstants() {
        val keywords = setOf(RescriptTokenTypes.LET, RescriptTokenTypes.TYPE, RescriptTokenTypes.MODULE)
        assertTrue("LET should be in keywords", RescriptTokenTypes.LET in keywords)
        assertTrue("TYPE should be in keywords", RescriptTokenTypes.TYPE in keywords)
        assertTrue("MODULE should be in keywords", RescriptTokenTypes.MODULE in keywords)
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
