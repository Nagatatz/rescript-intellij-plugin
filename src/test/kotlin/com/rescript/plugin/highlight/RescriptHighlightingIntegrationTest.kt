package com.rescript.plugin.highlight

import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.rescript.plugin.IntelliJPlatformExtension
import com.rescript.plugin.lang.RescriptTokenTypes
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Integration test for ReScript syntax highlighting using the full IDE platform.
 *
 * Verifies that the lexer and syntax highlighter work correctly end-to-end
 * when processing real ReScript files through the IntelliJ infrastructure.
 */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptHighlightingIntegrationTest {
    private lateinit var myFixture: CodeInsightTestFixture

    @Suppress("unused")
    private val testDataPath: String = "src/test/testData/highlighting"

    @Test
    fun testFileParsesPsiTreeSuccessfully() {
        val file =
            myFixture.configureByText(
                "BasicHL.res",
                "let x = 1\ntype t = int\nmodule M = {}\n",
            )
        assertNotNull(file, "Expected non-null PSI file")
        assertNotNull(file.node, "Expected non-null AST node")
        val tokens = collectTokenTypes(file)
        assertTrue(tokens.isNotEmpty(), "Expected at least one token")
    }

    @Test
    fun testKeywordsAreTokenized() {
        val file =
            myFixture.configureByText(
                "Keywords.res",
                "let x = 1\ntype t = int\nmodule M = {}\nopen Belt",
            )
        val tokens = collectTokenTypes(file)
        assertTrue(tokens.contains(RescriptTokenTypes.LET), "Expected LET token")
        assertTrue(tokens.contains(RescriptTokenTypes.TYPE), "Expected TYPE token")
        assertTrue(tokens.contains(RescriptTokenTypes.MODULE), "Expected MODULE token")
        assertTrue(tokens.contains(RescriptTokenTypes.OPEN), "Expected OPEN token")
    }

    @Test
    fun testStringLiteralsAreTokenized() {
        val file = myFixture.configureByText("Strings.res", "let s = \"hello world\"")
        val tokens = collectTokenTypes(file)
        assertTrue(tokens.contains(RescriptTokenTypes.STRING_VALUE), "Expected STRING_VALUE token")
    }

    @Test
    fun testNumericLiteralsAreTokenized() {
        val file = myFixture.configureByText("Numbers.res", "let n = 42\nlet f = 3.14")
        val tokens = collectTokenTypes(file)
        assertTrue(tokens.contains(RescriptTokenTypes.INT_VALUE), "Expected INT_VALUE token")
        assertTrue(tokens.contains(RescriptTokenTypes.FLOAT_VALUE), "Expected FLOAT_VALUE token")
    }

    @Test
    fun testCommentsAreTokenized() {
        val file =
            myFixture.configureByText(
                "Comments.res",
                "// single line\n/* block comment */\nlet x = 1",
            )
        val tokens = collectTokenTypes(file)
        assertTrue(tokens.contains(RescriptTokenTypes.SINGLE_COMMENT), "Expected SINGLE_COMMENT token")
        assertTrue(tokens.contains(RescriptTokenTypes.MULTI_COMMENT), "Expected MULTI_COMMENT token")
    }

    @Test
    fun testJsxTokensArePresent() {
        val file =
            myFixture.configureByText(
                "Jsx.res",
                "let make = () => <div> {React.string(\"hi\")} </div>",
            )
        val tokens = collectTokenTypes(file)
        assertTrue(tokens.contains(RescriptTokenTypes.JSX_TAG_NAME), "Expected JSX_TAG_NAME token")
        assertTrue(tokens.contains(RescriptTokenTypes.TAG_LT), "Expected TAG_LT token")
    }

    private fun collectTokenTypes(file: com.intellij.psi.PsiFile): Set<com.intellij.psi.tree.IElementType> {
        val types = mutableSetOf<com.intellij.psi.tree.IElementType>()

        fun walk(node: com.intellij.lang.ASTNode) {
            if (node.firstChildNode == null) {
                types.add(node.elementType)
            }
            var child = node.firstChildNode
            while (child != null) {
                walk(child)
                child = child.treeNext
            }
        }
        file.node?.let { walk(it) }
        return types
    }
}
