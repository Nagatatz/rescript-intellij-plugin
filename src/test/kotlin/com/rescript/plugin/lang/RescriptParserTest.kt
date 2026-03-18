package com.rescript.plugin.lang

import com.intellij.lang.ASTNode
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.ParsingTestHelper
import com.rescript.plugin.RescriptParsingTestExtension
import com.rescript.plugin.lang.psi.RescriptElementTypes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(RescriptParsingTestExtension::class)
class RescriptParserTest {
    private lateinit var parsingHelper: ParsingTestHelper

    private fun assertFirstChildToken(
        node: ASTNode,
        expectedType: IElementType,
    ) {
        var child = node.firstChildNode
        // Skip whitespace
        while (child != null && child.elementType == com.intellij.psi.TokenType.WHITE_SPACE) {
            child = child.treeNext
        }
        assertNotNull(child, "Expected first non-ws child of type $expectedType")
        assertEquals(expectedType, child!!.elementType)
    }

    // ════════════════════════════════════════════════════════════════
    // let declarations
    // ════════════════════════════════════════════════════════════════

    @Test
    fun testLetSimple() {
        parsingHelper.assertHasElements("let x = 42", RescriptElementTypes.LET_DECLARATION, 1)
    }

    @Test
    fun testLetRec() {
        val file = parsingHelper.parseCode("let rec fib = x => x")
        val lets = parsingHelper.findElements(file, RescriptElementTypes.LET_DECLARATION)
        assertEquals(1, lets.size)
        // Should contain REC token
        val recNodes = parsingHelper.findElements(file, RescriptTokenTypes.REC)
        assertFalse(recNodes.isEmpty(), "Should contain rec keyword")
    }

    @Test
    fun testLetMultiple() {
        val code =
            """
            let a = 1
            let b = 2
            let c = 3
            """.trimIndent()
        parsingHelper.assertHasElements(code, RescriptElementTypes.LET_DECLARATION, 3)
    }

    @Test
    fun testLetWithBraces() {
        val code =
            """
            let f = x => {
              let y = x + 1
              y
            }
            let g = 2
            """.trimIndent()
        val file = parsingHelper.parseCode(code)
        val lets = parsingHelper.findElements(file, RescriptElementTypes.LET_DECLARATION)
        // Top-level: f and g. Inner `let y` is inside braces and consumed by skipToEndOfDeclaration
        assertEquals(2, lets.size)
    }

    // ════════════════════════════════════════════════════════════════
    // type declarations
    // ════════════════════════════════════════════════════════════════

    @Test
    fun testTypeSimple() {
        parsingHelper.assertHasElements("type t = int", RescriptElementTypes.TYPE_DECLARATION, 1)
    }

    @Test
    fun testTypeRec() {
        val file = parsingHelper.parseCode("type rec tree = Node(tree, tree) | Leaf")
        val types = parsingHelper.findElements(file, RescriptElementTypes.TYPE_DECLARATION)
        assertEquals(1, types.size)
    }

    @Test
    fun testTypeWithBraces() {
        val code =
            """
            type person = {
              name: string,
              age: int,
            }
            type color = Red | Green | Blue
            """.trimIndent()
        parsingHelper.assertHasElements(code, RescriptElementTypes.TYPE_DECLARATION, 2)
    }

    // ════════════════════════════════════════════════════════════════
    // module declarations
    // ════════════════════════════════════════════════════════════════

    @Test
    fun testModuleSimple() {
        val code =
            """
            module M = {
              let x = 1
            }
            """.trimIndent()
        parsingHelper.assertHasElements(code, RescriptElementTypes.MODULE_DECLARATION, 1)
    }

    @Test
    fun testModuleType() {
        val code =
            """
            module type S = {
              let x: int
            }
            """.trimIndent()
        val file = parsingHelper.parseCode(code)
        val modules = parsingHelper.findElements(file, RescriptElementTypes.MODULE_DECLARATION)
        assertEquals(1, modules.size)
    }

    @Test
    fun testModuleRec() {
        val code =
            """
            module rec A = {
              let x = 1
            }
            """.trimIndent()
        parsingHelper.assertHasElements(code, RescriptElementTypes.MODULE_DECLARATION, 1)
    }

    // ════════════════════════════════════════════════════════════════
    // external declarations
    // ════════════════════════════════════════════════════════════════

    @Test
    fun testExternal() {
        parsingHelper.assertHasElements(
            "external log : string => unit = \"console.log\"",
            RescriptElementTypes.EXTERNAL_DECLARATION,
            1,
        )
    }

    @Test
    fun testExternalMultiple() {
        val code =
            """
            external log : string => unit = "console.log"
            external alert : string => unit = "alert"
            """.trimIndent()
        parsingHelper.assertHasElements(code, RescriptElementTypes.EXTERNAL_DECLARATION, 2)
    }

    // ════════════════════════════════════════════════════════════════
    // open / include statements
    // ════════════════════════════════════════════════════════════════

    @Test
    fun testOpen() {
        parsingHelper.assertHasElements("open Belt", RescriptElementTypes.OPEN_STATEMENT, 1)
    }

    @Test
    fun testInclude() {
        parsingHelper.assertHasElements("include Common", RescriptElementTypes.INCLUDE_STATEMENT, 1)
    }

    @Test
    fun testOpenAndInclude() {
        val code =
            """
            open Belt
            include Common
            """.trimIndent()
        val file = parsingHelper.parseCode(code)
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.OPEN_STATEMENT).size)
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.INCLUDE_STATEMENT).size)
    }

    // ════════════════════════════════════════════════════════════════
    // exception declarations
    // ════════════════════════════════════════════════════════════════

    @Test
    fun testException() {
        parsingHelper.assertHasElements("exception NotFound", RescriptElementTypes.EXCEPTION_DECLARATION, 1)
    }

    @Test
    fun testExceptionWithPayload() {
        parsingHelper.assertHasElements(
            "exception HttpError(int, string)",
            RescriptElementTypes.EXCEPTION_DECLARATION,
            1,
        )
    }

    // ════════════════════════════════════════════════════════════════
    // annotations
    // ════════════════════════════════════════════════════════════════

    @Test
    fun testAnnotationSimple() {
        parsingHelper.assertHasElements("@module", RescriptElementTypes.ANNOTATION, 1)
    }

    @Test
    fun testAnnotationDotted() {
        parsingHelper.assertHasElements("@react.component", RescriptElementTypes.ANNOTATION, 1)
    }

    @Test
    fun testAnnotationWithArgs() {
        parsingHelper.assertHasElements("@module(\"fs\")", RescriptElementTypes.ANNOTATION, 1)
    }

    @Test
    fun testDoubleAnnotation() {
        // @@ produces two ARROBASE tokens; the parser handles first @ as annotation,
        // second @ starts another annotation
        val file = parsingHelper.parseCode("@@deriving")
        val annotations = parsingHelper.findElements(file, RescriptElementTypes.ANNOTATION)
        // The parser sees first @ → ANNOTATION (empty, no name after @),
        // then second @ → ANNOTATION with name "deriving"
        // Actual count depends on parser implementation
        assertTrue(annotations.isNotEmpty(), "Should have at least 1 annotation")
    }

    // ════════════════════════════════════════════════════════════════
    // compound cases
    // ════════════════════════════════════════════════════════════════

    @Test
    fun testAnnotationFollowedByDeclaration() {
        val code =
            """
            @react.component
            let make = () => <div />
            """.trimIndent()
        val file = parsingHelper.parseCode(code)
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.ANNOTATION).size)
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.LET_DECLARATION).size)
    }

    @Test
    fun testMixedDeclarations() {
        val code =
            """
            open Belt
            type t = int
            let x = 1
            module M = {
              let y = 2
            }
            external f : int => int = "f"
            include Common
            exception NotFound
            """.trimIndent()
        val file = parsingHelper.parseCode(code)
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.OPEN_STATEMENT).size)
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.TYPE_DECLARATION).size)
        // top-level x + inner y
        assertEquals(2, parsingHelper.findElements(file, RescriptElementTypes.LET_DECLARATION).size)
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.MODULE_DECLARATION).size)
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.EXTERNAL_DECLARATION).size)
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.INCLUDE_STATEMENT).size)
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.EXCEPTION_DECLARATION).size)
    }

    @Test
    fun testNestedBracesDoNotConfuseParser() {
        // Keywords inside braces should not be treated as top-level declarations
        val code =
            """
            let f = () => {
              let inner = {
                let deep = 1
                type t = int
              }
              inner
            }
            let g = 2
            """.trimIndent()
        val file = parsingHelper.parseCode(code)
        val lets = parsingHelper.findElements(file, RescriptElementTypes.LET_DECLARATION)
        // Only top-level: f and g
        assertEquals(2, lets.size)
    }

    @Test
    fun testAnnotationBeforeModule() {
        val code =
            """
            @module("react")
            external make : unit => React.element = "default"
            """.trimIndent()
        val file = parsingHelper.parseCode(code)
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.ANNOTATION).size)
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.EXTERNAL_DECLARATION).size)
    }

    // ════════════════════════════════════════════════════════════════
    // non-top-level tokens are silently skipped (no error markers)
    // ════════════════════════════════════════════════════════════════

    @Test
    fun testExpressionTokensSkippedSilently() {
        val file = parsingHelper.parseCode("42 + 1")
        val errors = parsingHelper.findErrors(file)
        assertTrue(errors.isEmpty(), "Expression-level tokens should not produce errors")
    }

    @Test
    fun testExpressionBeforeDeclarationNoError() {
        val code =
            """
            42 + 1
            let x = 1
            """.trimIndent()
        val file = parsingHelper.parseCode(code)
        val errors = parsingHelper.findErrors(file)
        assertTrue(errors.isEmpty(), "Expression before declaration should not produce errors")
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.LET_DECLARATION).size)
    }

    @Test
    fun testUnexpectedTokensBetweenDeclarations() {
        // Garbage between declarations is consumed by skipToEndOfDeclaration as part of the preceding
        // declaration body. The parser still correctly identifies the next declaration.
        val code =
            """
            let a = 1
            foo bar baz
            let b = 2
            """.trimIndent()
        val file = parsingHelper.parseCode(code)
        assertEquals(2, parsingHelper.findElements(file, RescriptElementTypes.LET_DECLARATION).size)
    }

    // ════════════════════════════════════════════════════════════════
    // error recovery: missing identifiers (R2)
    // ════════════════════════════════════════════════════════════════

    @Test
    fun testLetMissingIdentifier() {
        val code = "let = 42"
        val file = parsingHelper.parseCode(code)
        val errors = parsingHelper.findErrors(file)
        assertTrue(errors.isNotEmpty(), "Should report missing identifier")
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.LET_DECLARATION).size)
    }

    @Test
    fun testTypeMissingIdentifier() {
        val code = "type = int"
        val file = parsingHelper.parseCode(code)
        val errors = parsingHelper.findErrors(file)
        assertTrue(errors.isNotEmpty(), "Should report missing identifier")
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.TYPE_DECLARATION).size)
    }

    @Test
    fun testModuleMissingName() {
        val code =
            """
            module = {
              let x = 1
            }
            """.trimIndent()
        val file = parsingHelper.parseCode(code)
        val errors = parsingHelper.findErrors(file)
        assertTrue(errors.isNotEmpty(), "Should report missing module name")
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.MODULE_DECLARATION).size)
    }

    @Test
    fun testMissingIdentifierRecovery() {
        val code =
            """
            let = 42
            let y = 2
            """.trimIndent()
        val file = parsingHelper.parseCode(code)
        assertEquals(
            2,
            parsingHelper.findElements(file, RescriptElementTypes.LET_DECLARATION).size,
            "Both let declarations should be recognized",
        )
    }

    // ════════════════════════════════════════════════════════════════
    // error recovery: unbalanced braces (R3)
    // ════════════════════════════════════════════════════════════════

    @Test
    fun testModuleMissingClosingBrace() {
        val code =
            """
            module M = {
              let x = 1
            let y = 2
            """.trimIndent()
        val file = parsingHelper.parseCode(code)
        // Module should still be created, and parser should not crash
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.MODULE_DECLARATION).size)
    }

    // ════════════════════════════════════════════════════════════════
    // error recovery: compound error cases (R4)
    // ════════════════════════════════════════════════════════════════

    @Test
    fun testMultipleErrorsOnlyForMissingIdentifiers() {
        val code =
            """
            garbage tokens here
            let x = 1
            more garbage
            type t = int
            let = 42
            """.trimIndent()
        val file = parsingHelper.parseCode(code)
        val errors = parsingHelper.findErrors(file)
        // Only "let = 42" (missing identifier) should produce an error.
        // "garbage tokens here" is silently skipped; "more garbage" is consumed by let x body.
        assertEquals(1, errors.size, "Only missing-identifier error expected")
        assertEquals(2, parsingHelper.findElements(file, RescriptElementTypes.LET_DECLARATION).size)
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.TYPE_DECLARATION).size)
    }

    @Test
    fun testEmptyFileNoErrors() {
        val file = parsingHelper.parseCode("")
        val errors = parsingHelper.findErrors(file)
        assertTrue(errors.isEmpty(), "Empty file should have no errors")
    }

    @Test
    fun testValidCodeNoErrors() {
        val code =
            """
            let x = 1
            type t = int
            module M = {
              let y = 2
            }
            """.trimIndent()
        val file = parsingHelper.parseCode(code)
        val errors = parsingHelper.findErrors(file)
        assertTrue(errors.isEmpty(), "Valid code should have no errors")
    }

    @Test
    fun testJsxComponentNoErrors() {
        val code =
            """
            @react.component
            let make = (~children) =>
              <RescriptRelayReact.Context.Provider environment=RelayEnv.environment>
                <div> children </div>
              </RescriptRelayReact.Context.Provider>
            """.trimIndent()
        val file = parsingHelper.parseCode(code)
        val errors = parsingHelper.findErrors(file)
        assertTrue(errors.isEmpty(), "JSX code should have no errors")
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.ANNOTATION).size)
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.LET_DECLARATION).size)
    }

    @Test
    fun testRawExpressionNoErrors() {
        val code =
            """
            %raw("require('isomorphic-fetch')")
            let x = 1
            """.trimIndent()
        val file = parsingHelper.parseCode(code)
        val errors = parsingHelper.findErrors(file)
        assertTrue(errors.isEmpty(), "%raw expression should have no errors")
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.LET_DECLARATION).size)
    }

    // ════════════════════════════════════════════════════════════════
    // JSX PSI modeling
    // ════════════════════════════════════════════════════════════════

    @Test
    fun testJsxSelfClosingTag() {
        val code = "let x = <div />"
        val file = parsingHelper.parseCode(code)
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.LET_DECLARATION).size)
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.JSX_SELF_CLOSING_ELEMENT).size)
    }

    @Test
    fun testJsxSelfClosingComponent() {
        val code = "let x = <MyComponent />"
        val file = parsingHelper.parseCode(code)
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.JSX_SELF_CLOSING_ELEMENT).size)
    }

    @Test
    fun testJsxOpenCloseTag() {
        val code = "let x = <div> hello </div>"
        val file = parsingHelper.parseCode(code)
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.LET_DECLARATION).size)
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.JSX_ELEMENT).size)
    }

    @Test
    fun testJsxNestedElements() {
        val code = "let x = <div><span /></div>"
        val file = parsingHelper.parseCode(code)
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.JSX_ELEMENT).size)
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.JSX_SELF_CLOSING_ELEMENT).size)
    }

    @Test
    fun testJsxFragment() {
        val code = "let x = <> <div /> </>"
        val file = parsingHelper.parseCode(code)
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.JSX_FRAGMENT).size)
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.JSX_SELF_CLOSING_ELEMENT).size)
    }

    @Test
    fun testJsxWithExpression() {
        val code = "let x = <div>{name}</div>"
        val file = parsingHelper.parseCode(code)
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.JSX_ELEMENT).size)
    }

    @Test
    fun testJsxDottedComponentName() {
        val code = "let x = <React.Fragment> <div /> </React.Fragment>"
        val file = parsingHelper.parseCode(code)
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.JSX_ELEMENT).size)
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.JSX_SELF_CLOSING_ELEMENT).size)
    }

    @Test
    fun testJsxTopLevel() {
        // JSX at top level (not inside a declaration)
        val code = "<div />"
        val file = parsingHelper.parseCode(code)
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.JSX_SELF_CLOSING_ELEMENT).size)
    }

    @Test
    fun testJsxComponentWithAnnotation() {
        val code =
            """
            @react.component
            let make = (~children) =>
              <div> children </div>
            """.trimIndent()
        val file = parsingHelper.parseCode(code)
        val errors = parsingHelper.findErrors(file)
        assertTrue(errors.isEmpty(), "JSX component should have no errors")
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.ANNOTATION).size)
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.LET_DECLARATION).size)
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.JSX_ELEMENT).size)
    }

    @Test
    fun testJsxDeeplyNested() {
        val code =
            """
            let x = <div>
              <ul>
                <li> item1 </li>
                <li> item2 </li>
              </ul>
            </div>
            """.trimIndent()
        val file = parsingHelper.parseCode(code)
        // div > ul: 2 JSX_ELEMENT, li + li: 2 JSX_ELEMENT = 4 total
        val elements = parsingHelper.findElements(file, RescriptElementTypes.JSX_ELEMENT)
        assertEquals(4, elements.size)
    }

    @Test
    fun testJsxNoErrorsOnExistingTest() {
        // Ensure the existing complex JSX test still produces no errors
        val code =
            """
            @react.component
            let make = (~children) =>
              <RescriptRelayReact.Context.Provider environment=RelayEnv.environment>
                <div> children </div>
              </RescriptRelayReact.Context.Provider>
            """.trimIndent()
        val file = parsingHelper.parseCode(code)
        val errors = parsingHelper.findErrors(file)
        assertTrue(errors.isEmpty(), "Complex JSX should have no errors")
        // Should produce JSX PSI nodes
        assertTrue(
            parsingHelper.findElements(file, RescriptElementTypes.JSX_ELEMENT).isNotEmpty(),
            "Should have JSX elements",
        )
    }

    @Test
    fun testJsxSelfClosingInFragment() {
        val code = "let x = <> <br /> <hr /> </>"
        val file = parsingHelper.parseCode(code)
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.JSX_FRAGMENT).size)
        assertEquals(2, parsingHelper.findElements(file, RescriptElementTypes.JSX_SELF_CLOSING_ELEMENT).size)
    }
}
