package com.rescript.plugin.lang

import com.intellij.psi.tree.IElementType
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.rescript.plugin.IntelliJPlatformExtension
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Integration test for the ReScript lexer using the full IDE platform.
 *
 * Verifies that the lexer correctly tokenizes ReScript source code when
 * processed through the IntelliJ infrastructure, including multi-line
 * constructs and template literals.
 */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptLexerIntegrationTest {
    private lateinit var myFixture: CodeInsightTestFixture

    private fun collectLeafTokenTypes(file: com.intellij.psi.PsiFile): List<IElementType> {
        val types = mutableListOf<IElementType>()

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

    private fun collectLeafTokenTypeSet(file: com.intellij.psi.PsiFile): Set<IElementType> =
        collectLeafTokenTypes(file).toSet()

    @Test
    fun testKeywordsTokenized() {
        val file =
            myFixture.configureByText(
                "Keywords.res",
                "let x = 1\ntype t = int\nmodule M = {}\nopen Belt\nswitch x { | _ => () }",
            )
        val tokens = collectLeafTokenTypeSet(file)
        assertTrue(tokens.contains(RescriptTokenTypes.LET), "Expected LET token")
        assertTrue(tokens.contains(RescriptTokenTypes.TYPE), "Expected TYPE token")
        assertTrue(tokens.contains(RescriptTokenTypes.MODULE), "Expected MODULE token")
        assertTrue(tokens.contains(RescriptTokenTypes.OPEN), "Expected OPEN token")
        assertTrue(tokens.contains(RescriptTokenTypes.SWITCH), "Expected SWITCH token")
    }

    @Test
    fun testStringLiteralTokenized() {
        val file = myFixture.configureByText("Strings.res", "let s = \"hello world\"")
        val tokens = collectLeafTokenTypeSet(file)
        assertTrue(tokens.contains(RescriptTokenTypes.STRING_VALUE), "Expected STRING_VALUE token")
    }

    @Test
    fun testNumericLiteralsTokenized() {
        val file = myFixture.configureByText("Numbers.res", "let n = 42\nlet f = 3.14")
        val tokens = collectLeafTokenTypeSet(file)
        assertTrue(tokens.contains(RescriptTokenTypes.INT_VALUE), "Expected INT_VALUE token")
        assertTrue(tokens.contains(RescriptTokenTypes.FLOAT_VALUE), "Expected FLOAT_VALUE token")
    }

    @Test
    fun testCommentsTokenized() {
        val file =
            myFixture.configureByText(
                "Comments.res",
                "// single line\n/* block comment */\nlet x = 1",
            )
        val tokens = collectLeafTokenTypeSet(file)
        assertTrue(tokens.contains(RescriptTokenTypes.SINGLE_COMMENT), "Expected SINGLE_COMMENT token")
        assertTrue(tokens.contains(RescriptTokenTypes.MULTI_COMMENT), "Expected MULTI_COMMENT token")
    }

    @Test
    fun testOperatorsTokenized() {
        val file =
            myFixture.configureByText(
                "Operators.res",
                "let x = 1 + 2\nlet f = a => a * 2\nlet y = x->Belt.Array.get(0)",
            )
        val tokens = collectLeafTokenTypeSet(file)
        assertTrue(tokens.contains(RescriptTokenTypes.PLUS), "Expected PLUS token")
        assertTrue(tokens.contains(RescriptTokenTypes.STAR), "Expected STAR token")
        assertTrue(tokens.contains(RescriptTokenTypes.ARROW), "Expected ARROW (=>) token")
        assertTrue(tokens.contains(RescriptTokenTypes.RIGHT_ARROW), "Expected RIGHT_ARROW (->) token")
    }

    @Test
    fun testIdentifiersTokenized() {
        val file =
            myFixture.configureByText(
                "Identifiers.res",
                "let myVar = 1\nmodule MyModule = {}",
            )
        val tokens = collectLeafTokenTypeSet(file)
        assertTrue(tokens.contains(RescriptTokenTypes.LIDENT), "Expected LIDENT token")
        assertTrue(tokens.contains(RescriptTokenTypes.UIDENT), "Expected UIDENT token")
    }

    @Test
    fun testPunctuationTokenized() {
        val file =
            myFixture.configureByText(
                "Punctuation.res",
                "let f = (a, b) => { a + b }",
            )
        val tokens = collectLeafTokenTypeSet(file)
        assertTrue(tokens.contains(RescriptTokenTypes.LPAREN), "Expected LPAREN token")
        assertTrue(tokens.contains(RescriptTokenTypes.RPAREN), "Expected RPAREN token")
        assertTrue(tokens.contains(RescriptTokenTypes.LBRACE), "Expected LBRACE token")
        assertTrue(tokens.contains(RescriptTokenTypes.RBRACE), "Expected RBRACE token")
        assertTrue(tokens.contains(RescriptTokenTypes.COMMA), "Expected COMMA token")
    }

    @Test
    fun testJsxTokensPresent() {
        val file =
            myFixture.configureByText(
                "Jsx.res",
                "let make = () => <div> {React.string(\"hi\")} </div>",
            )
        val tokens = collectLeafTokenTypeSet(file)
        assertTrue(tokens.contains(RescriptTokenTypes.JSX_TAG_NAME), "Expected JSX_TAG_NAME token")
        assertTrue(tokens.contains(RescriptTokenTypes.TAG_LT), "Expected TAG_LT token")
    }

    @Test
    fun testTemplateLiteralTokenized() {
        val file =
            myFixture.configureByText(
                "Template.res",
                "let s = `hello \${name}`",
            )
        val tokens = collectLeafTokenTypeSet(file)
        assertTrue(tokens.contains(RescriptTokenTypes.JS_STRING_OPEN), "Expected JS_STRING_OPEN token")
        assertTrue(tokens.contains(RescriptTokenTypes.JS_STRING_CLOSE), "Expected JS_STRING_CLOSE token")
        assertTrue(tokens.contains(RescriptTokenTypes.DOLLAR), "Expected DOLLAR token")
    }

    @Test
    fun testAnnotationTokenized() {
        val file =
            myFixture.configureByText(
                "Annotation.res",
                "@module(\"fs\")\nexternal readFile: string => string = \"readFileSync\"",
            )
        val tokens = collectLeafTokenTypeSet(file)
        assertTrue(tokens.contains(RescriptTokenTypes.ARROBASE), "Expected ARROBASE token")
        assertTrue(tokens.contains(RescriptTokenTypes.EXTERNAL), "Expected EXTERNAL token")
    }

    @Test
    fun testTokenOrderPreserved() {
        val file = myFixture.configureByText("Order.res", "let x = 1")
        val tokens = collectLeafTokenTypes(file)
        // Verify token order: LET, whitespace/ident, EQ, whitespace/ident
        val letIndex = tokens.indexOf(RescriptTokenTypes.LET)
        val eqIndex = tokens.indexOf(RescriptTokenTypes.EQ)
        val intIndex = tokens.indexOf(RescriptTokenTypes.INT_VALUE)
        assertTrue(letIndex < eqIndex, "LET should appear before EQ")
        assertTrue(eqIndex < intIndex, "EQ should appear before INT_VALUE")
    }
}
