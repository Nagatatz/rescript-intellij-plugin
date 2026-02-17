package com.rescript.plugin.codevision

import com.intellij.codeInsight.codeVision.CodeVisionAnchorKind
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class RescriptCodeVisionProviderTest : BasePlatformTestCase() {
    private val provider = RescriptCodeVisionProvider()

    fun testProviderProperties() {
        assertEquals("rescript.codeLens", provider.id)
        assertEquals("ReScript Type Annotations", provider.name)
        assertEquals(CodeVisionAnchorKind.Top, provider.defaultAnchor)
        assertTrue(provider.relativeOrderings.isEmpty())
    }

    fun testResiFileReturnsEmpty() {
        val resiFile = myFixture.addFileToProject("Test.resi", "let x: int")
        myFixture.openFileInEditor(resiFile.virtualFile)

        val result = provider.computeForEditor(myFixture.editor, resiFile)

        assertTrue(result.isEmpty())
    }

    fun testResFileWithoutLspReturnsEmpty() {
        val resFile = myFixture.addFileToProject("Test.res", "let x = 1")
        myFixture.openFileInEditor(resFile.virtualFile)

        val result = provider.computeForEditor(myFixture.editor, resFile)

        assertTrue(result.isEmpty())
    }
}
