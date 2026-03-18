package com.rescript.plugin.lang

import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.rescript.plugin.IntelliJPlatformExtension
import com.rescript.plugin.lang.psi.RescriptElementTypes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Integration test for the ReScript parser using the full IDE platform.
 *
 * Verifies that the parser correctly builds PSI trees for various ReScript
 * constructs when processed through the IntelliJ infrastructure.
 */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptParserIntegrationTest {
    private lateinit var myFixture: CodeInsightTestFixture

    private fun collectElements(
        file: com.intellij.psi.PsiFile,
        type: com.intellij.psi.tree.IElementType,
    ): List<com.intellij.lang.ASTNode> {
        val result = mutableListOf<com.intellij.lang.ASTNode>()

        fun walk(node: com.intellij.lang.ASTNode) {
            if (node.elementType == type) result.add(node)
            var child = node.firstChildNode
            while (child != null) {
                walk(child)
                child = child.treeNext
            }
        }
        file.node?.let { walk(it) }
        return result
    }

    @Test
    fun testLetDeclarationsParsed() {
        val file =
            myFixture.configureByText(
                "LetTest.res",
                "let x = 1\nlet y = 2\n",
            )
        val lets = collectElements(file, RescriptElementTypes.LET_DECLARATION)
        assertEquals(2, lets.size, "Expected 2 let declarations")
    }

    @Test
    fun testTypeDeclarationParsed() {
        val file =
            myFixture.configureByText(
                "TypeTest.res",
                "type color = Red | Green | Blue\n",
            )
        val types = collectElements(file, RescriptElementTypes.TYPE_DECLARATION)
        assertEquals(1, types.size, "Expected 1 type declaration")
    }

    @Test
    fun testModuleDeclarationParsed() {
        val file =
            myFixture.configureByText(
                "ModuleTest.res",
                "module M = {\n  let x = 1\n}\n",
            )
        val modules = collectElements(file, RescriptElementTypes.MODULE_DECLARATION)
        assertEquals(1, modules.size, "Expected 1 module declaration")
    }

    @Test
    fun testExternalDeclarationParsed() {
        val file =
            myFixture.configureByText(
                "ExternalTest.res",
                "external log: string => unit = \"console.log\"\n",
            )
        val externals = collectElements(file, RescriptElementTypes.EXTERNAL_DECLARATION)
        assertEquals(1, externals.size, "Expected 1 external declaration")
    }

    @Test
    fun testExceptionDeclarationParsed() {
        val file =
            myFixture.configureByText(
                "ExceptionTest.res",
                "exception NotFound\n",
            )
        val exceptions = collectElements(file, RescriptElementTypes.EXCEPTION_DECLARATION)
        assertEquals(1, exceptions.size, "Expected 1 exception declaration")
    }

    @Test
    fun testOpenStatementParsed() {
        val file =
            myFixture.configureByText(
                "OpenTest.res",
                "open Belt\n",
            )
        val opens = collectElements(file, RescriptElementTypes.OPEN_STATEMENT)
        assertEquals(1, opens.size, "Expected 1 open statement")
    }

    @Test
    fun testJsxElementParsed() {
        val file =
            myFixture.configureByText(
                "JsxTest.res",
                "let make = () => <div> hello </div>\n",
            )
        val jsxElements = collectElements(file, RescriptElementTypes.JSX_ELEMENT)
        assertEquals(1, jsxElements.size, "Expected 1 JSX element")
    }

    @Test
    fun testJsxSelfClosingParsed() {
        val file =
            myFixture.configureByText(
                "JsxSelfTest.res",
                "let make = () => <br />\n",
            )
        val jsxSelfClosing = collectElements(file, RescriptElementTypes.JSX_SELF_CLOSING_ELEMENT)
        assertEquals(1, jsxSelfClosing.size, "Expected 1 JSX self-closing element")
    }

    @Test
    fun testMixedDeclarationsParsed() {
        val file =
            myFixture.configureByText(
                "MixedTest.res",
                """
                let x = 1
                type t = int
                module M = {
                  let y = 2
                }
                external log: string => unit = "console.log"
                exception NotFound
                """.trimIndent(),
            )
        assertEquals(2, collectElements(file, RescriptElementTypes.LET_DECLARATION).size)
        assertEquals(1, collectElements(file, RescriptElementTypes.TYPE_DECLARATION).size)
        assertEquals(1, collectElements(file, RescriptElementTypes.MODULE_DECLARATION).size)
        assertEquals(1, collectElements(file, RescriptElementTypes.EXTERNAL_DECLARATION).size)
        assertEquals(1, collectElements(file, RescriptElementTypes.EXCEPTION_DECLARATION).size)
    }
}
