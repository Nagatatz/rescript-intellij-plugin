package com.rescript.plugin.lang

import com.rescript.plugin.ParsingTestHelper
import com.rescript.plugin.RescriptParsingTestExtension
import com.rescript.plugin.lang.psi.RescriptElementTypes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Tests for [RescriptJsxParser] focusing on JSX-specific edge cases:
 * attributes, dotted component paths, nested structures, fragments,
 * expression containers, and error tolerance.
 *
 * Uses [RescriptParsingTestExtension] to exercise the real lexer+parser pipeline.
 * General JSX coverage is in [RescriptParserTest]; this class targets
 * the internal methods of [RescriptJsxParser].
 *
 * @see RescriptJsxParser
 * @see RescriptParserTest for general JSX coverage
 */
@ExtendWith(RescriptParsingTestExtension::class)
class RescriptJsxParserTest {
    private lateinit var parsingHelper: ParsingTestHelper

    // ════════════════════════════════════════════════════════════════
    // tryParseJsx: self-closing elements with attributes
    // ════════════════════════════════════════════════════════════════

    @Test
    fun testSelfClosingWithStringAttribute() {
        parsingHelper.assertHasElements(
            "let x = <input type=\"text\" />",
            RescriptElementTypes.JSX_SELF_CLOSING_ELEMENT,
            1,
        )
    }

    @Test
    fun testSelfClosingWithMultipleAttributes() {
        parsingHelper.assertHasElements(
            "let x = <input type=\"text\" value=\"hello\" disabled=true />",
            RescriptElementTypes.JSX_SELF_CLOSING_ELEMENT,
            1,
        )
    }

    @Test
    fun testSelfClosingWithExpressionAttribute() {
        // Attribute value with braces: className={styles.container}
        parsingHelper.assertHasElements(
            "let x = <div className={styles.container} />",
            RescriptElementTypes.JSX_SELF_CLOSING_ELEMENT,
            1,
        )
    }

    @Test
    fun testSelfClosingWithSpreadAttribute() {
        // Spread props: {...props}
        parsingHelper.assertHasElements(
            "let x = <MyComponent {...props} />",
            RescriptElementTypes.JSX_SELF_CLOSING_ELEMENT,
            1,
        )
    }

    @Test
    fun testSelfClosingWithPunnedAttribute() {
        // ReScript-style punned label: <Comp ~label />
        val file = parsingHelper.parseCode("let x = <MyComponent name />")
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.JSX_SELF_CLOSING_ELEMENT).size)
    }

    // ════════════════════════════════════════════════════════════════
    // tryParseJsx: dotted component paths
    // ════════════════════════════════════════════════════════════════

    @Test
    fun testDottedComponentSelfClosing() {
        parsingHelper.assertHasElements(
            "let x = <Module.Component />",
            RescriptElementTypes.JSX_SELF_CLOSING_ELEMENT,
            1,
        )
    }

    @Test
    fun testDeepDottedComponentPath() {
        parsingHelper.assertHasElements(
            "let x = <A.B.C.D />",
            RescriptElementTypes.JSX_SELF_CLOSING_ELEMENT,
            1,
        )
    }

    @Test
    fun testDottedComponentOpenClose() {
        val file = parsingHelper.parseCode("let x = <Module.Comp> child </Module.Comp>")
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.JSX_ELEMENT).size)
    }

    @Test
    fun testDottedComponentWithAttributes() {
        val file = parsingHelper.parseCode("let x = <Ui.Button size=\"large\" onClick={handler} />")
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.JSX_SELF_CLOSING_ELEMENT).size)
    }

    // ════════════════════════════════════════════════════════════════
    // tryParseJsx: open/close elements with children
    // ════════════════════════════════════════════════════════════════

    @Test
    fun testEmptyElement() {
        // Element with no children: <div></div>
        val file = parsingHelper.parseCode("let x = <div></div>")
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.JSX_ELEMENT).size)
    }

    @Test
    fun testElementWithTextChild() {
        val file = parsingHelper.parseCode("let x = <span> hello world </span>")
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.JSX_ELEMENT).size)
    }

    @Test
    fun testElementWithMultipleExpressionChildren() {
        val file = parsingHelper.parseCode("let x = <div>{a}{b}{c}</div>")
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.JSX_ELEMENT).size)
    }

    @Test
    fun testElementWithMixedChildren() {
        // Text + expression + nested element
        val file = parsingHelper.parseCode("let x = <div> text {expr} <span /> </div>")
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.JSX_ELEMENT).size)
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.JSX_SELF_CLOSING_ELEMENT).size)
    }

    @Test
    fun testElementWithNestedBracesInExpression() {
        // Expression container with nested braces: {switch x { | A => 1 }}
        val file = parsingHelper.parseCode("let x = <div>{switch x { | A => 1 | B => 2 }}</div>")
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.JSX_ELEMENT).size)
    }

    // ════════════════════════════════════════════════════════════════
    // tryParseJsxFragment: fragment edge cases
    // ════════════════════════════════════════════════════════════════

    @Test
    fun testEmptyFragment() {
        parsingHelper.assertHasElements("let x = <> </>", RescriptElementTypes.JSX_FRAGMENT, 1)
    }

    @Test
    fun testFragmentWithMultipleChildren() {
        val file = parsingHelper.parseCode("let x = <> <div /> <span /> <br /> </>")
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.JSX_FRAGMENT).size)
        assertEquals(3, parsingHelper.findElements(file, RescriptElementTypes.JSX_SELF_CLOSING_ELEMENT).size)
    }

    @Test
    fun testFragmentWithNestedElement() {
        val file = parsingHelper.parseCode("let x = <> <div> inner </div> </>")
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.JSX_FRAGMENT).size)
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.JSX_ELEMENT).size)
    }

    @Test
    fun testFragmentWithExpression() {
        val file = parsingHelper.parseCode("let x = <> {name} </>")
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.JSX_FRAGMENT).size)
    }

    @Test
    fun testNestedFragments() {
        val file = parsingHelper.parseCode("let x = <> <> inner </> </>")
        assertEquals(2, parsingHelper.findElements(file, RescriptElementTypes.JSX_FRAGMENT).size)
    }

    // ════════════════════════════════════════════════════════════════
    // parseJsxChildren: deeply nested structures
    // ════════════════════════════════════════════════════════════════

    @Test
    fun testThreeLevelNesting() {
        val code =
            """
            let x = <div>
              <section>
                <p> text </p>
              </section>
            </div>
            """.trimIndent()
        val file = parsingHelper.parseCode(code)
        // div, section, p = 3 JSX_ELEMENT
        assertEquals(3, parsingHelper.findElements(file, RescriptElementTypes.JSX_ELEMENT).size)
    }

    @Test
    fun testSiblingElements() {
        val code =
            """
            let x = <div>
              <span> a </span>
              <span> b </span>
              <span> c </span>
            </div>
            """.trimIndent()
        val file = parsingHelper.parseCode(code)
        // div + 3 spans = 4 JSX_ELEMENT
        assertEquals(4, parsingHelper.findElements(file, RescriptElementTypes.JSX_ELEMENT).size)
    }

    @Test
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
        val file = parsingHelper.parseCode(code)
        assertEquals(2, parsingHelper.findElements(file, RescriptElementTypes.JSX_ELEMENT).size) // div + span
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.JSX_SELF_CLOSING_ELEMENT).size) // br
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.JSX_FRAGMENT).size) // fragment
    }

    // ════════════════════════════════════════════════════════════════
    // consumeClosingTag: closing tag edge cases
    // ════════════════════════════════════════════════════════════════

    @Test
    fun testClosingTagWithDottedPath() {
        val file = parsingHelper.parseCode("let x = <A.B.C> child </A.B.C>")
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.JSX_ELEMENT).size)
    }

    // ════════════════════════════════════════════════════════════════
    // JSX inside declaration bodies
    // ════════════════════════════════════════════════════════════════

    @Test
    fun testJsxInsideLetBody() {
        val code =
            """
            let render = () => {
              <div> <span /> </div>
            }
            """.trimIndent()
        val file = parsingHelper.parseCode(code)
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.LET_DECLARATION).size)
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.JSX_ELEMENT).size)
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.JSX_SELF_CLOSING_ELEMENT).size)
    }

    @Test
    fun testJsxInsideModuleBody() {
        val code =
            """
            module App = {
              let make = () => <div />
            }
            """.trimIndent()
        val file = parsingHelper.parseCode(code)
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.MODULE_DECLARATION).size)
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.JSX_SELF_CLOSING_ELEMENT).size)
    }

    @Test
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
        val file = parsingHelper.parseCode(code)
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.LET_DECLARATION).size)
        assertEquals(2, parsingHelper.findElements(file, RescriptElementTypes.JSX_ELEMENT).size)
    }

    // ════════════════════════════════════════════════════════════════
    // error tolerance: no errors for valid JSX
    // ════════════════════════════════════════════════════════════════

    @Test
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
        val file = parsingHelper.parseCode(code)
        val errors = parsingHelper.findErrors(file)
        assertTrue(errors.isEmpty(), "Complex JSX should have no errors")
        assertTrue(parsingHelper.findElements(file, RescriptElementTypes.JSX_ELEMENT).size >= 4)
    }

    @Test
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
        val file = parsingHelper.parseCode(code)
        val errors = parsingHelper.findErrors(file)
        assertTrue(errors.isEmpty(), "Fragment with components should have no errors")
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.JSX_FRAGMENT).size)
    }

    // ════════════════════════════════════════════════════════════════
    // JSX at top level (outside declarations)
    // ════════════════════════════════════════════════════════════════

    @Test
    fun testTopLevelSelfClosing() {
        val file = parsingHelper.parseCode("<br />")
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.JSX_SELF_CLOSING_ELEMENT).size)
    }

    @Test
    fun testTopLevelOpenClose() {
        val file = parsingHelper.parseCode("<div> content </div>")
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.JSX_ELEMENT).size)
    }

    @Test
    fun testTopLevelFragment() {
        val file = parsingHelper.parseCode("<> <div /> </>")
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.JSX_FRAGMENT).size)
    }

    // ════════════════════════════════════════════════════════════════
    // multiple JSX elements across declarations
    // ════════════════════════════════════════════════════════════════

    @Test
    fun testMultipleDeclarationsWithJsx() {
        val code =
            """
            let header = <Header />
            let content = <div> body </div>
            let footer = <> <span /> </>
            """.trimIndent()
        val file = parsingHelper.parseCode(code)
        assertEquals(3, parsingHelper.findElements(file, RescriptElementTypes.LET_DECLARATION).size)
        // Header + span inside fragment = 2 self-closing elements
        assertEquals(2, parsingHelper.findElements(file, RescriptElementTypes.JSX_SELF_CLOSING_ELEMENT).size)
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.JSX_ELEMENT).size) // div
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.JSX_FRAGMENT).size) // fragment
    }
}
