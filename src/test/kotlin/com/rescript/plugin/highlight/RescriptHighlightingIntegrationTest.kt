package com.rescript.plugin.highlight

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.rescript.plugin.lang.RescriptTokenTypes

/**
 * Integration test for ReScript syntax highlighting using the full IDE platform.
 *
 * Verifies that the lexer and syntax highlighter work correctly end-to-end
 * when processing real ReScript files through the IntelliJ infrastructure.
 */
class RescriptHighlightingIntegrationTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String = "src/test/testData/highlighting"

    fun testFileParsesPsiTreeSuccessfully() {
        val file =
            myFixture.configureByText(
                "BasicHL.res",
                "let x = 1\ntype t = int\nmodule M = {}\n",
            )
        assertNotNull("Expected non-null PSI file", file)
        assertNotNull("Expected non-null AST node", file.node)
        val tokens = collectTokenTypes(file)
        assertTrue("Expected at least one token", tokens.isNotEmpty())
    }

    fun testKeywordsAreTokenized() {
        val file =
            myFixture.configureByText(
                "Keywords.res",
                "let x = 1\ntype t = int\nmodule M = {}\nopen Belt",
            )
        val tokens = collectTokenTypes(file)
        assertTrue("Expected LET token", tokens.contains(RescriptTokenTypes.LET))
        assertTrue("Expected TYPE token", tokens.contains(RescriptTokenTypes.TYPE))
        assertTrue("Expected MODULE token", tokens.contains(RescriptTokenTypes.MODULE))
        assertTrue("Expected OPEN token", tokens.contains(RescriptTokenTypes.OPEN))
    }

    fun testStringLiteralsAreTokenized() {
        val file = myFixture.configureByText("Strings.res", "let s = \"hello world\"")
        val tokens = collectTokenTypes(file)
        assertTrue("Expected STRING_VALUE token", tokens.contains(RescriptTokenTypes.STRING_VALUE))
    }

    fun testNumericLiteralsAreTokenized() {
        val file = myFixture.configureByText("Numbers.res", "let n = 42\nlet f = 3.14")
        val tokens = collectTokenTypes(file)
        assertTrue("Expected INT_VALUE token", tokens.contains(RescriptTokenTypes.INT_VALUE))
        assertTrue("Expected FLOAT_VALUE token", tokens.contains(RescriptTokenTypes.FLOAT_VALUE))
    }

    fun testCommentsAreTokenized() {
        val file =
            myFixture.configureByText(
                "Comments.res",
                "// single line\n/* block comment */\nlet x = 1",
            )
        val tokens = collectTokenTypes(file)
        assertTrue("Expected SINGLE_COMMENT token", tokens.contains(RescriptTokenTypes.SINGLE_COMMENT))
        assertTrue("Expected MULTI_COMMENT token", tokens.contains(RescriptTokenTypes.MULTI_COMMENT))
    }

    fun testJsxTokensArePresent() {
        val file =
            myFixture.configureByText(
                "Jsx.res",
                "let make = () => <div> {React.string(\"hi\")} </div>",
            )
        val tokens = collectTokenTypes(file)
        assertTrue("Expected JSX_TAG_NAME token", tokens.contains(RescriptTokenTypes.JSX_TAG_NAME))
        assertTrue("Expected TAG_LT token", tokens.contains(RescriptTokenTypes.TAG_LT))
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
