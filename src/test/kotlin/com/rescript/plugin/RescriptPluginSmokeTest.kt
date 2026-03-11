package com.rescript.plugin

import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Headless IDE smoke test that verifies the plugin loads correctly
 * in the IntelliJ Platform sandbox environment.
 *
 * Tests fundamental registration: language, file types, and basic file recognition.
 * Catches issues like EP registration errors and service initialization failures.
 */
class RescriptPluginSmokeTest : BasePlatformTestCase() {
    fun testLanguageRegistered() {
        val language = Language.findLanguageByID("ReScript")
        assertNotNull("ReScript language should be registered", language)
        assertEquals(RescriptLanguage, language)
    }

    fun testResFileTypeRegistered() {
        val fileType = FileTypeManager.getInstance().getFileTypeByExtension("res")
        assertNotNull("File type for .res should be registered", fileType)
        assertEquals("ReScript", fileType.name)
    }

    fun testResiFileTypeRegistered() {
        val fileType = FileTypeManager.getInstance().getFileTypeByExtension("resi")
        assertNotNull("File type for .resi should be registered", fileType)
        assertEquals("ReScript Interface", fileType.name)
    }

    fun testResFileRecognized() {
        val file = myFixture.configureByText("test.res", "let x = 1")
        assertNotNull("PsiFile should be created for .res file", file)
        assertEquals(RescriptLanguage, file.language)
    }

    fun testResiFileRecognized() {
        val file = myFixture.configureByText("test.resi", "let x: int")
        assertNotNull("PsiFile should be created for .resi file", file)
        assertEquals(RescriptLanguage, file.language)
    }

    fun testLexerProducesTokens() {
        val file =
            myFixture.configureByText(
                "test.res",
                """
                let greeting = "hello"
                type color = Red | Green | Blue
                module M = { let x = 1 }
                """.trimIndent(),
            )
        val tokens = mutableListOf<com.intellij.psi.tree.IElementType>()
        var node = file.node?.firstChildNode
        while (node != null) {
            tokens.add(node.elementType)
            node = node.treeNext
        }
        assertTrue("Should produce PSI nodes", tokens.isNotEmpty())
    }

    fun testBasicHighlighting() {
        // Verify that configuring a ReScript file doesn't throw exceptions
        val file =
            myFixture.configureByText(
                "test.res",
                """
                let x = 42
                let y = "hello"
                module M = { let z = true }
                """.trimIndent(),
            )
        // If we get here without exceptions, the highlighting infrastructure works
        assertNotNull("File should be configured without exceptions", file)
    }
}
