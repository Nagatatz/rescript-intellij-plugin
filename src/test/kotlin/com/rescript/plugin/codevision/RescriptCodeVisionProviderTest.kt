package com.rescript.plugin.codevision

import com.intellij.codeInsight.codeVision.CodeVisionAnchorKind
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.rescript.plugin.IntelliJPlatformExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(IntelliJPlatformExtension::class)
class RescriptCodeVisionProviderTest {
    private lateinit var myFixture: CodeInsightTestFixture

    private val provider = RescriptCodeVisionProvider()

    @Test
    fun testProviderProperties() {
        assertEquals("rescript.codeLens", provider.id)
        assertEquals("ReScript Type Annotations", provider.name)
        assertEquals(CodeVisionAnchorKind.Top, provider.defaultAnchor)
        assertTrue(provider.relativeOrderings.isEmpty())
    }

    @Test
    fun testResiFileReturnsEmpty() {
        val resiFile = myFixture.addFileToProject("Test.resi", "let x: int")
        myFixture.openFileInEditor(resiFile.virtualFile)

        val result = provider.computeForEditor(myFixture.editor, resiFile)

        assertTrue(result.isEmpty())
    }

    @Test
    fun testResFileWithoutLspReturnsEmpty() {
        val resFile = myFixture.addFileToProject("Test.res", "let x = 1")
        myFixture.openFileInEditor(resFile.virtualFile)

        val result = provider.computeForEditor(myFixture.editor, resFile)

        assertTrue(result.isEmpty())
    }
}
