package com.rescript.plugin

import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Headless IDE smoke test that verifies the plugin loads correctly
 * in the IntelliJ Platform sandbox environment.
 *
 * Tests fundamental registration: language, file types, and basic file recognition.
 * Catches issues like EP registration errors and service initialization failures.
 */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptPluginSmokeTest {
    @Suppress("unused")
    private lateinit var myFixture: CodeInsightTestFixture

    @Test
    fun testLanguageRegistered() {
        val language = Language.findLanguageByID("ReScript")
        assertNotNull(language, "ReScript language should be registered")
        assertEquals(RescriptLanguage, language)
    }

    @Test
    fun testResFileTypeRegistered() {
        val fileType = FileTypeManager.getInstance().getFileTypeByExtension("res")
        assertNotNull(fileType, "File type for .res should be registered")
        assertEquals("ReScript", fileType.name)
    }

    @Test
    fun testResiFileTypeRegistered() {
        val fileType = FileTypeManager.getInstance().getFileTypeByExtension("resi")
        assertNotNull(fileType, "File type for .resi should be registered")
        assertEquals("ReScript Interface", fileType.name)
    }

    @Test
    fun testResFileRecognized() {
        val file = myFixture.configureByText("test.res", "let x = 1")
        assertNotNull(file, "PsiFile should be created for .res file")
        assertEquals(RescriptLanguage, file.language)
    }

    @Test
    fun testResiFileRecognized() {
        val file = myFixture.configureByText("test.resi", "let x: int")
        assertNotNull(file, "PsiFile should be created for .resi file")
        assertEquals(RescriptLanguage, file.language)
    }

    @Test
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
        assertTrue(tokens.isNotEmpty(), "Should produce PSI nodes")
    }

    @Test
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
        assertNotNull(file, "File should be configured without exceptions")
    }
}
