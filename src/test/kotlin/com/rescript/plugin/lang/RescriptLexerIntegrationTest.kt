package com.rescript.plugin.lang

import com.intellij.psi.tree.IElementType
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Integration test for the ReScript lexer using the full IDE platform.
 *
 * Verifies that the lexer correctly tokenizes ReScript source code when
 * processed through the IntelliJ infrastructure, including multi-line
 * constructs and template literals.
 */
class RescriptLexerIntegrationTest : BasePlatformTestCase() {
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

    fun testKeywordsTokenized() {
        val file =
            myFixture.configureByText(
                "Keywords.res",
                "let x = 1\ntype t = int\nmodule M = {}\nopen Belt\nswitch x { | _ => () }",
            )
        val tokens = collectLeafTokenTypeSet(file)
        assertTrue("Expected LET token", tokens.contains(RescriptTokenTypes.LET))
        assertTrue("Expected TYPE token", tokens.contains(RescriptTokenTypes.TYPE))
        assertTrue("Expected MODULE token", tokens.contains(RescriptTokenTypes.MODULE))
        assertTrue("Expected OPEN token", tokens.contains(RescriptTokenTypes.OPEN))
        assertTrue("Expected SWITCH token", tokens.contains(RescriptTokenTypes.SWITCH))
    }

    fun testStringLiteralTokenized() {
        val file = myFixture.configureByText("Strings.res", "let s = \"hello world\"")
        val tokens = collectLeafTokenTypeSet(file)
        assertTrue("Expected STRING_VALUE token", tokens.contains(RescriptTokenTypes.STRING_VALUE))
    }

    fun testNumericLiteralsTokenized() {
        val file = myFixture.configureByText("Numbers.res", "let n = 42\nlet f = 3.14")
        val tokens = collectLeafTokenTypeSet(file)
        assertTrue("Expected INT_VALUE token", tokens.contains(RescriptTokenTypes.INT_VALUE))
        assertTrue("Expected FLOAT_VALUE token", tokens.contains(RescriptTokenTypes.FLOAT_VALUE))
    }

    fun testCommentsTokenized() {
        val file =
            myFixture.configureByText(
                "Comments.res",
                "// single line\n/* block comment */\nlet x = 1",
            )
        val tokens = collectLeafTokenTypeSet(file)
        assertTrue("Expected SINGLE_COMMENT token", tokens.contains(RescriptTokenTypes.SINGLE_COMMENT))
        assertTrue("Expected MULTI_COMMENT token", tokens.contains(RescriptTokenTypes.MULTI_COMMENT))
    }

    fun testOperatorsTokenized() {
        val file =
            myFixture.configureByText(
                "Operators.res",
                "let x = 1 + 2\nlet f = a => a * 2\nlet y = x->Belt.Array.get(0)",
            )
        val tokens = collectLeafTokenTypeSet(file)
        assertTrue("Expected PLUS token", tokens.contains(RescriptTokenTypes.PLUS))
        assertTrue("Expected STAR token", tokens.contains(RescriptTokenTypes.STAR))
        assertTrue("Expected ARROW (=>) token", tokens.contains(RescriptTokenTypes.ARROW))
        assertTrue("Expected RIGHT_ARROW (->) token", tokens.contains(RescriptTokenTypes.RIGHT_ARROW))
    }

    fun testIdentifiersTokenized() {
        val file =
            myFixture.configureByText(
                "Identifiers.res",
                "let myVar = 1\nmodule MyModule = {}",
            )
        val tokens = collectLeafTokenTypeSet(file)
        assertTrue("Expected LIDENT token", tokens.contains(RescriptTokenTypes.LIDENT))
        assertTrue("Expected UIDENT token", tokens.contains(RescriptTokenTypes.UIDENT))
    }

    fun testPunctuationTokenized() {
        val file =
            myFixture.configureByText(
                "Punctuation.res",
                "let f = (a, b) => { a + b }",
            )
        val tokens = collectLeafTokenTypeSet(file)
        assertTrue("Expected LPAREN token", tokens.contains(RescriptTokenTypes.LPAREN))
        assertTrue("Expected RPAREN token", tokens.contains(RescriptTokenTypes.RPAREN))
        assertTrue("Expected LBRACE token", tokens.contains(RescriptTokenTypes.LBRACE))
        assertTrue("Expected RBRACE token", tokens.contains(RescriptTokenTypes.RBRACE))
        assertTrue("Expected COMMA token", tokens.contains(RescriptTokenTypes.COMMA))
    }

    fun testJsxTokensPresent() {
        val file =
            myFixture.configureByText(
                "Jsx.res",
                "let make = () => <div> {React.string(\"hi\")} </div>",
            )
        val tokens = collectLeafTokenTypeSet(file)
        assertTrue("Expected JSX_TAG_NAME token", tokens.contains(RescriptTokenTypes.JSX_TAG_NAME))
        assertTrue("Expected TAG_LT token", tokens.contains(RescriptTokenTypes.TAG_LT))
    }

    fun testTemplateLiteralTokenized() {
        val file =
            myFixture.configureByText(
                "Template.res",
                "let s = `hello \${name}`",
            )
        val tokens = collectLeafTokenTypeSet(file)
        assertTrue("Expected JS_STRING_OPEN token", tokens.contains(RescriptTokenTypes.JS_STRING_OPEN))
        assertTrue("Expected JS_STRING_CLOSE token", tokens.contains(RescriptTokenTypes.JS_STRING_CLOSE))
        assertTrue("Expected DOLLAR token", tokens.contains(RescriptTokenTypes.DOLLAR))
    }

    fun testAnnotationTokenized() {
        val file =
            myFixture.configureByText(
                "Annotation.res",
                "@module(\"fs\")\nexternal readFile: string => string = \"readFileSync\"",
            )
        val tokens = collectLeafTokenTypeSet(file)
        assertTrue("Expected ARROBASE token", tokens.contains(RescriptTokenTypes.ARROBASE))
        assertTrue("Expected EXTERNAL token", tokens.contains(RescriptTokenTypes.EXTERNAL))
    }

    fun testTokenOrderPreserved() {
        val file = myFixture.configureByText("Order.res", "let x = 1")
        val tokens = collectLeafTokenTypes(file)
        // Verify token order: LET, whitespace/ident, EQ, whitespace/ident
        val letIndex = tokens.indexOf(RescriptTokenTypes.LET)
        val eqIndex = tokens.indexOf(RescriptTokenTypes.EQ)
        val intIndex = tokens.indexOf(RescriptTokenTypes.INT_VALUE)
        assertTrue("LET should appear before EQ", letIndex < eqIndex)
        assertTrue("EQ should appear before INT_VALUE", eqIndex < intIndex)
    }
}
