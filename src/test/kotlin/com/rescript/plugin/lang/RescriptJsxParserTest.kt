package com.rescript.plugin.lang

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.intellij.testFramework.ParsingTestCase
import com.rescript.plugin.lang.psi.RescriptElementTypes

/**
 * Tests for [RescriptJsxParser] focusing on JSX-specific edge cases:
 * attributes, dotted component paths, nested structures, fragments,
 * expression containers, and error tolerance.
 *
 * Uses [ParsingTestCase] to exercise the real lexer+parser pipeline.
 * General JSX coverage is in [RescriptParserTest]; this class targets
 * the internal methods of [RescriptJsxParser].
 *
 * @see RescriptJsxParser
 * @see RescriptParserTest for general JSX coverage
 */
class RescriptJsxParserTest :
    ParsingTestCase(
        "",
        "res",
        RescriptParserDefinition(),
    ) {
    override fun getTestDataPath(): String = "src/test/testData/parser"

    override fun skipSpaces(): Boolean = true

    override fun includeRanges(): Boolean = false

    // ── helpers ─────────────────────────────────────────────────────

    private fun parseCode(code: String): PsiFile {
        val file = createPsiFile("test", code)
        ensureParsed(file)
        return file
    }

    private fun findElements(
        file: PsiFile,
        type: IElementType,
    ): List<ASTNode> {
        val result = mutableListOf<ASTNode>()

        fun walk(node: ASTNode) {
            if (node.elementType == type) result.add(node)
            var child = node.firstChildNode
            while (child != null) {
                walk(child)
                child = child.treeNext
            }
        }
        walk(file.node)
        return result
    }

    private fun findErrors(file: PsiFile): List<ASTNode> {
        val result = mutableListOf<ASTNode>()

        fun walk(node: ASTNode) {
            if (node.elementType == TokenType.ERROR_ELEMENT) result.add(node)
            var child = node.firstChildNode
            while (child != null) {
                walk(child)
                child = child.treeNext
            }
        }
        walk(file.node)
        return result
    }

    private fun assertHasElements(
        code: String,
        type: IElementType,
        expectedCount: Int,
    ) {
        val file = parseCode(code)
        val elements = findElements(file, type)
        assertEquals(
            "Expected $expectedCount $type in: $code",
            expectedCount,
            elements.size,
        )
    }

    // ════════════════════════════════════════════════════════════════
    // tryParseJsx: self-closing elements with attributes
    // ════════════════════════════════════════════════════════════════

    fun testSelfClosingWithStringAttribute() {
        assertHasElements(
            "let x = <input type=\"text\" />",
            RescriptElementTypes.JSX_SELF_CLOSING_ELEMENT,
            1,
        )
    }

    fun testSelfClosingWithMultipleAttributes() {
        assertHasElements(
            "let x = <input type=\"text\" value=\"hello\" disabled=true />",
            RescriptElementTypes.JSX_SELF_CLOSING_ELEMENT,
            1,
        )
    }

    fun testSelfClosingWithExpressionAttribute() {
        // Attribute value with braces: className={styles.container}
        assertHasElements(
            "let x = <div className={styles.container} />",
            RescriptElementTypes.JSX_SELF_CLOSING_ELEMENT,
            1,
        )
    }

    fun testSelfClosingWithSpreadAttribute() {
        // Spread props: {...props}
        assertHasElements(
            "let x = <MyComponent {...props} />",
            RescriptElementTypes.JSX_SELF_CLOSING_ELEMENT,
            1,
        )
    }

    fun testSelfClosingWithPunnedAttribute() {
        // ReScript-style punned label: <Comp ~label />
        val file = parseCode("let x = <MyComponent name />")
        assertEquals(1, findElements(file, RescriptElementTypes.JSX_SELF_CLOSING_ELEMENT).size)
    }

    // ════════════════════════════════════════════════════════════════
    // tryParseJsx: dotted component paths
    // ════════════════════════════════════════════════════════════════

    fun testDottedComponentSelfClosing() {
        assertHasElements(
            "let x = <Module.Component />",
            RescriptElementTypes.JSX_SELF_CLOSING_ELEMENT,
            1,
        )
    }

    fun testDeepDottedComponentPath() {
        assertHasElements(
            "let x = <A.B.C.D />",
            RescriptElementTypes.JSX_SELF_CLOSING_ELEMENT,
            1,
        )
    }

    fun testDottedComponentOpenClose() {
        val file = parseCode("let x = <Module.Comp> child </Module.Comp>")
        assertEquals(1, findElements(file, RescriptElementTypes.JSX_ELEMENT).size)
    }

    fun testDottedComponentWithAttributes() {
        val file = parseCode("let x = <Ui.Button size=\"large\" onClick={handler} />")
        assertEquals(1, findElements(file, RescriptElementTypes.JSX_SELF_CLOSING_ELEMENT).size)
    }

    // ════════════════════════════════════════════════════════════════
    // tryParseJsx: open/close elements with children
    // ════════════════════════════════════════════════════════════════

    fun testEmptyElement() {
        // Element with no children: <div></div>
        val file = parseCode("let x = <div></div>")
        assertEquals(1, findElements(file, RescriptElementTypes.JSX_ELEMENT).size)
    }

    fun testElementWithTextChild() {
        val file = parseCode("let x = <span> hello world </span>")
        assertEquals(1, findElements(file, RescriptElementTypes.JSX_ELEMENT).size)
    }

    fun testElementWithMultipleExpressionChildren() {
        val file = parseCode("let x = <div>{a}{b}{c}</div>")
        assertEquals(1, findElements(file, RescriptElementTypes.JSX_ELEMENT).size)
    }

    fun testElementWithMixedChildren() {
        // Text + expression + nested element
        val file = parseCode("let x = <div> text {expr} <span /> </div>")
        assertEquals(1, findElements(file, RescriptElementTypes.JSX_ELEMENT).size)
        assertEquals(1, findElements(file, RescriptElementTypes.JSX_SELF_CLOSING_ELEMENT).size)
    }

    fun testElementWithNestedBracesInExpression() {
        // Expression container with nested braces: {switch x { | A => 1 }}
        val file = parseCode("let x = <div>{switch x { | A => 1 | B => 2 }}</div>")
        assertEquals(1, findElements(file, RescriptElementTypes.JSX_ELEMENT).size)
    }

    // ════════════════════════════════════════════════════════════════
    // tryParseJsxFragment: fragment edge cases
    // ════════════════════════════════════════════════════════════════

    fun testEmptyFragment() {
        assertHasElements("let x = <> </>", RescriptElementTypes.JSX_FRAGMENT, 1)
    }

    fun testFragmentWithMultipleChildren() {
        val file = parseCode("let x = <> <div /> <span /> <br /> </>")
        assertEquals(1, findElements(file, RescriptElementTypes.JSX_FRAGMENT).size)
        assertEquals(3, findElements(file, RescriptElementTypes.JSX_SELF_CLOSING_ELEMENT).size)
    }

    fun testFragmentWithNestedElement() {
        val file = parseCode("let x = <> <div> inner </div> </>")
        assertEquals(1, findElements(file, RescriptElementTypes.JSX_FRAGMENT).size)
        assertEquals(1, findElements(file, RescriptElementTypes.JSX_ELEMENT).size)
    }

    fun testFragmentWithExpression() {
        val file = parseCode("let x = <> {name} </>")
        assertEquals(1, findElements(file, RescriptElementTypes.JSX_FRAGMENT).size)
    }

    fun testNestedFragments() {
        val file = parseCode("let x = <> <> inner </> </>")
        assertEquals(2, findElements(file, RescriptElementTypes.JSX_FRAGMENT).size)
    }

    // ════════════════════════════════════════════════════════════════
    // parseJsxChildren: deeply nested structures
    // ════════════════════════════════════════════════════════════════

    fun testThreeLevelNesting() {
        val code =
            """
            let x = <div>
              <section>
                <p> text </p>
              </section>
            </div>
            """.trimIndent()
        val file = parseCode(code)
        // div, section, p = 3 JSX_ELEMENT
        assertEquals(3, findElements(file, RescriptElementTypes.JSX_ELEMENT).size)
    }

    fun testSiblingElements() {
        val code =
            """
            let x = <div>
              <span> a </span>
              <span> b </span>
              <span> c </span>
            </div>
            """.trimIndent()
        val file = parseCode(code)
        // div + 3 spans = 4 JSX_ELEMENT
        assertEquals(4, findElements(file, RescriptElementTypes.JSX_ELEMENT).size)
    }

    fun testMixedElementTypes() {
        // Self-closing + open/close + fragment as siblings
        val code =
            """
            let x = <div>
              <br />
              <span> text </span>
              <> fragment child </>
            </div>
            """.trimIndent()
        val file = parseCode(code)
        assertEquals(2, findElements(file, RescriptElementTypes.JSX_ELEMENT).size) // div + span
        assertEquals(1, findElements(file, RescriptElementTypes.JSX_SELF_CLOSING_ELEMENT).size) // br
        assertEquals(1, findElements(file, RescriptElementTypes.JSX_FRAGMENT).size) // fragment
    }

    // ════════════════════════════════════════════════════════════════
    // consumeClosingTag: closing tag edge cases
    // ════════════════════════════════════════════════════════════════

    fun testClosingTagWithDottedPath() {
        val file = parseCode("let x = <A.B.C> child </A.B.C>")
        assertEquals(1, findElements(file, RescriptElementTypes.JSX_ELEMENT).size)
    }

    // ════════════════════════════════════════════════════════════════
    // JSX inside declaration bodies
    // ════════════════════════════════════════════════════════════════

    fun testJsxInsideLetBody() {
        val code =
            """
            let render = () => {
              <div> <span /> </div>
            }
            """.trimIndent()
        val file = parseCode(code)
        assertEquals(1, findElements(file, RescriptElementTypes.LET_DECLARATION).size)
        assertEquals(1, findElements(file, RescriptElementTypes.JSX_ELEMENT).size)
        assertEquals(1, findElements(file, RescriptElementTypes.JSX_SELF_CLOSING_ELEMENT).size)
    }

    fun testJsxInsideModuleBody() {
        val code =
            """
            module App = {
              let make = () => <div />
            }
            """.trimIndent()
        val file = parseCode(code)
        assertEquals(1, findElements(file, RescriptElementTypes.MODULE_DECLARATION).size)
        assertEquals(1, findElements(file, RescriptElementTypes.JSX_SELF_CLOSING_ELEMENT).size)
    }

    fun testMultipleJsxInSameDeclaration() {
        val code =
            """
            let make = (show) => {
              if show {
                <div> visible </div>
              } else {
                <span> hidden </span>
              }
            }
            """.trimIndent()
        val file = parseCode(code)
        assertEquals(1, findElements(file, RescriptElementTypes.LET_DECLARATION).size)
        assertEquals(2, findElements(file, RescriptElementTypes.JSX_ELEMENT).size)
    }

    // ════════════════════════════════════════════════════════════════
    // error tolerance: no errors for valid JSX
    // ════════════════════════════════════════════════════════════════

    fun testComplexJsxNoErrors() {
        val code =
            """
            @react.component
            let make = (~name, ~children) =>
              <div className="wrapper">
                <h1> {React.string(name)} </h1>
                <main> children </main>
                <footer>
                  <p> {React.string("footer")} </p>
                </footer>
              </div>
            """.trimIndent()
        val file = parseCode(code)
        val errors = findErrors(file)
        assertTrue("Complex JSX should have no errors", errors.isEmpty())
        assertTrue(findElements(file, RescriptElementTypes.JSX_ELEMENT).size >= 4)
    }

    fun testFragmentWithComponentsNoErrors() {
        val code =
            """
            let make = () =>
              <>
                <Header />
                <Main.Content>
                  <Sidebar />
                  <Article title="hello" />
                </Main.Content>
                <Footer />
              </>
            """.trimIndent()
        val file = parseCode(code)
        val errors = findErrors(file)
        assertTrue("Fragment with components should have no errors", errors.isEmpty())
        assertEquals(1, findElements(file, RescriptElementTypes.JSX_FRAGMENT).size)
    }

    // ════════════════════════════════════════════════════════════════
    // JSX at top level (outside declarations)
    // ════════════════════════════════════════════════════════════════

    fun testTopLevelSelfClosing() {
        val file = parseCode("<br />")
        assertEquals(1, findElements(file, RescriptElementTypes.JSX_SELF_CLOSING_ELEMENT).size)
    }

    fun testTopLevelOpenClose() {
        val file = parseCode("<div> content </div>")
        assertEquals(1, findElements(file, RescriptElementTypes.JSX_ELEMENT).size)
    }

    fun testTopLevelFragment() {
        val file = parseCode("<> <div /> </>")
        assertEquals(1, findElements(file, RescriptElementTypes.JSX_FRAGMENT).size)
    }

    // ════════════════════════════════════════════════════════════════
    // multiple JSX elements across declarations
    // ════════════════════════════════════════════════════════════════

    fun testMultipleDeclarationsWithJsx() {
        val code =
            """
            let header = <Header />
            let content = <div> body </div>
            let footer = <> <span /> </>
            """.trimIndent()
        val file = parseCode(code)
        assertEquals(3, findElements(file, RescriptElementTypes.LET_DECLARATION).size)
        // Header + span inside fragment = 2 self-closing elements
        assertEquals(2, findElements(file, RescriptElementTypes.JSX_SELF_CLOSING_ELEMENT).size)
        assertEquals(1, findElements(file, RescriptElementTypes.JSX_ELEMENT).size) // div
        assertEquals(1, findElements(file, RescriptElementTypes.JSX_FRAGMENT).size) // fragment
    }
}
