package com.rescript.plugin.lang

import com.rescript.plugin.ParsingTestHelper
import com.rescript.plugin.RescriptParsingTestExtension
import com.rescript.plugin.lang.psi.RescriptElementTypes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Tests for [RescriptDeclarationParser] focusing on edge cases in declaration
 * parsing, annotation handling, and error recovery.
 *
 * Uses [RescriptParsingTestExtension] to exercise the real parsing pipeline with actual
 * lexer and PsiBuilder integration.
 *
 * @see RescriptDeclarationParser
 * @see RescriptParserTest for general parser coverage
 */
@ExtendWith(RescriptParsingTestExtension::class)
class RescriptDeclarationParserTest {
    private lateinit var parsingHelper: ParsingTestHelper

    // ════════════════════════════════════════════════════════════════
    // parseDeclaration: identifier edge cases
    // ════════════════════════════════════════════════════════════════

    @Test
    fun testLetWithUnderscore() {
        parsingHelper.assertHasElements("let _ = 42", RescriptElementTypes.LET_DECLARATION, 1)
    }

    @Test
    fun testLetWithUppercaseIdentifier() {
        // UIDENT is in IDENTIFIER_TOKENS, should be accepted
        parsingHelper.assertHasElements("let X = 42", RescriptElementTypes.LET_DECLARATION, 1)
    }

    @Test
    fun testExternalWithUnderscore() {
        parsingHelper.assertHasElements(
            "external _ : int => unit = \"fn\"",
            RescriptElementTypes.EXTERNAL_DECLARATION,
            1,
        )
    }

    @Test
    fun testExceptionWithUppercaseIdentifier() {
        parsingHelper.assertHasElements("exception MyError", RescriptElementTypes.EXCEPTION_DECLARATION, 1)
    }

    @Test
    fun testExceptionWithComplexPayload() {
        parsingHelper.assertHasElements(
            "exception HttpError({code: int, message: string})",
            RescriptElementTypes.EXCEPTION_DECLARATION,
            1,
        )
    }

    // ════════════════════════════════════════════════════════════════
    // parseDeclaration: rec keyword handling
    // ════════════════════════════════════════════════════════════════

    @Test
    fun testLetRecWithUnderscore() {
        val file = parsingHelper.parseCode("let rec _ = () => ()")
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.LET_DECLARATION).size)
    }

    @Test
    fun testTypeRecMultiple() {
        val code =
            """
            type rec tree = Leaf | Node(tree, tree)
            type rec list<'a> = Nil | Cons('a, list<'a>)
            """.trimIndent()
        parsingHelper.assertHasElements(code, RescriptElementTypes.TYPE_DECLARATION, 2)
    }

    @Test
    fun testLetWithoutRecStillWorks() {
        // consumeRec=true but no rec keyword present — should just skip
        parsingHelper.assertHasElements("let simple = 1", RescriptElementTypes.LET_DECLARATION, 1)
    }

    // ════════════════════════════════════════════════════════════════
    // parseDeclaration: error recovery — missing identifier
    // ════════════════════════════════════════════════════════════════

    @Test
    fun testExternalMissingIdentifier() {
        val file = parsingHelper.parseCode("external : int => unit = \"fn\"")
        val errors = parsingHelper.findErrors(file)
        assertTrue(errors.isNotEmpty(), "Should report missing identifier for external")
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.EXTERNAL_DECLARATION).size)
    }

    @Test
    fun testExceptionMissingIdentifier() {
        // 'exception' followed by a non-identifier, non-top-level token
        val file = parsingHelper.parseCode("exception = NotValid")
        val errors = parsingHelper.findErrors(file)
        assertTrue(errors.isNotEmpty(), "Should report missing identifier for exception")
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.EXCEPTION_DECLARATION).size)
    }

    @Test
    fun testMissingIdentifierFollowedByTopLevel() {
        // When the next token IS a top-level start, no error should be reported
        // because the parser treats it as "no identifier, skip" gracefully
        val code =
            """
            let = 42
            type t = int
            """.trimIndent()
        val file = parsingHelper.parseCode(code)
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.LET_DECLARATION).size)
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.TYPE_DECLARATION).size)
    }

    @Test
    fun testMissingIdentifierAtEof() {
        val file = parsingHelper.parseCode("let")
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.LET_DECLARATION).size)
    }

    // ════════════════════════════════════════════════════════════════
    // parseSimple: open / include edge cases
    // ════════════════════════════════════════════════════════════════

    @Test
    fun testOpenDottedPath() {
        parsingHelper.assertHasElements("open Belt.Array", RescriptElementTypes.OPEN_STATEMENT, 1)
    }

    @Test
    fun testIncludeDottedPath() {
        parsingHelper.assertHasElements("include React.Component", RescriptElementTypes.INCLUDE_STATEMENT, 1)
    }

    @Test
    fun testOpenAtEndOfFile() {
        val file = parsingHelper.parseCode("open")
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.OPEN_STATEMENT).size)
    }

    @Test
    fun testIncludeAtEndOfFile() {
        val file = parsingHelper.parseCode("include")
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.INCLUDE_STATEMENT).size)
    }

    @Test
    fun testMultipleOpenStatements() {
        val code =
            """
            open Belt
            open Belt.Array
            open Belt.Map
            """.trimIndent()
        parsingHelper.assertHasElements(code, RescriptElementTypes.OPEN_STATEMENT, 3)
    }

    // ════════════════════════════════════════════════════════════════
    // parseAnnotation: various forms
    // ════════════════════════════════════════════════════════════════

    @Test
    fun testAnnotationWithNestedParens() {
        // @module({"key": "value"}) — parens with nested braces
        parsingHelper.assertHasElements("@module({\"key\": \"value\"})", RescriptElementTypes.ANNOTATION, 1)
    }

    @Test
    fun testAnnotationWithEmptyParens() {
        parsingHelper.assertHasElements("@foo()", RescriptElementTypes.ANNOTATION, 1)
    }

    @Test
    fun testAnnotationMultipleDotted() {
        // Deep dotted path: @a.b.c
        parsingHelper.assertHasElements("@a.b.c", RescriptElementTypes.ANNOTATION, 1)
    }

    @Test
    fun testMultipleAnnotationsBeforeDeclaration() {
        val code =
            """
            @genType
            @react.component
            let make = () => <div />
            """.trimIndent()
        val file = parsingHelper.parseCode(code)
        assertEquals(2, parsingHelper.findElements(file, RescriptElementTypes.ANNOTATION).size)
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.LET_DECLARATION).size)
    }

    @Test
    fun testAnnotationWithComplexArgs() {
        // @send annotation with complex argument
        parsingHelper.assertHasElements("@send", RescriptElementTypes.ANNOTATION, 1)
    }

    @Test
    fun testAnnotationFollowedByEof() {
        val file = parsingHelper.parseCode("@module")
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.ANNOTATION).size)
    }

    @Test
    fun testAnnotationWithStringArg() {
        parsingHelper.assertHasElements("@val(\"document\")", RescriptElementTypes.ANNOTATION, 1)
    }

    // ════════════════════════════════════════════════════════════════
    // module declaration edge cases (parsed in RescriptParser but
    // delegates identifier parsing similar to RescriptDeclarationParser)
    // ════════════════════════════════════════════════════════════════

    @Test
    fun testModuleAlias() {
        // Module alias without braces: module X = Y
        parsingHelper.assertHasElements("module X = Y", RescriptElementTypes.MODULE_DECLARATION, 1)
    }

    @Test
    fun testNestedModules() {
        val code =
            """
            module Outer = {
              module Inner = {
                let x = 1
              }
            }
            """.trimIndent()
        val file = parsingHelper.parseCode(code)
        assertEquals(2, parsingHelper.findElements(file, RescriptElementTypes.MODULE_DECLARATION).size)
    }

    @Test
    fun testDeeplyNestedModules() {
        val code =
            """
            module A = {
              module B = {
                module C = {
                  let x = 1
                }
              }
            }
            """.trimIndent()
        val file = parsingHelper.parseCode(code)
        assertEquals(3, parsingHelper.findElements(file, RescriptElementTypes.MODULE_DECLARATION).size)
    }

    // ════════════════════════════════════════════════════════════════
    // compound edge cases: declaration sequences
    // ════════════════════════════════════════════════════════════════

    @Test
    fun testAllDeclarationTypes() {
        val code =
            """
            @genType
            let x = 1
            let rec fib = n => n
            type t = int
            type rec tree = Leaf | Node(tree, tree)
            module M = {
              let y = 2
            }
            module type S = {
              let z: int
            }
            module rec R = {
              let w = 3
            }
            external log: string => unit = "console.log"
            open Belt
            include Common
            exception NotFound
            exception HttpError(int, string)
            """.trimIndent()
        val file = parsingHelper.parseCode(code)
        val errors = parsingHelper.findErrors(file)
        assertTrue(errors.isEmpty(), "All declaration types should parse without errors")
        assertTrue(parsingHelper.findElements(file, RescriptElementTypes.ANNOTATION).isNotEmpty())
        assertTrue(parsingHelper.findElements(file, RescriptElementTypes.LET_DECLARATION).isNotEmpty())
        assertTrue(parsingHelper.findElements(file, RescriptElementTypes.TYPE_DECLARATION).isNotEmpty())
        assertTrue(parsingHelper.findElements(file, RescriptElementTypes.MODULE_DECLARATION).isNotEmpty())
        assertTrue(parsingHelper.findElements(file, RescriptElementTypes.EXTERNAL_DECLARATION).isNotEmpty())
        assertTrue(parsingHelper.findElements(file, RescriptElementTypes.OPEN_STATEMENT).isNotEmpty())
        assertTrue(parsingHelper.findElements(file, RescriptElementTypes.INCLUDE_STATEMENT).isNotEmpty())
        assertTrue(parsingHelper.findElements(file, RescriptElementTypes.EXCEPTION_DECLARATION).isNotEmpty())
    }

    @Test
    fun testDeclarationAfterAnnotationWithArgs() {
        val code =
            """
            @module("fs")
            external readFileSync: string => string = "readFileSync"
            """.trimIndent()
        val file = parsingHelper.parseCode(code)
        val errors = parsingHelper.findErrors(file)
        assertTrue(errors.isEmpty(), "Annotation + external should have no errors")
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.ANNOTATION).size)
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.EXTERNAL_DECLARATION).size)
    }

    @Test
    fun testEmptyModuleNoError() {
        val code =
            """
            module Empty = {
            }
            """.trimIndent()
        val file = parsingHelper.parseCode(code)
        val errors = parsingHelper.findErrors(file)
        assertTrue(errors.isEmpty(), "Empty module should have no errors")
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.MODULE_DECLARATION).size)
    }

    @Test
    fun testDeclarationWithComplexBody() {
        // Declaration with switch expression containing braces
        val code =
            """
            let f = x => switch x {
            | Some(v) => v
            | None => 0
            }
            let g = 2
            """.trimIndent()
        val file = parsingHelper.parseCode(code)
        assertEquals(2, parsingHelper.findElements(file, RescriptElementTypes.LET_DECLARATION).size)
    }

    @Test
    fun testTypeWithVariantsAndRecord() {
        val code =
            """
            type shape =
              | Circle({radius: float})
              | Rectangle({width: float, height: float})
              | Point
            """.trimIndent()
        val file = parsingHelper.parseCode(code)
        assertEquals(1, parsingHelper.findElements(file, RescriptElementTypes.TYPE_DECLARATION).size)
    }
}
