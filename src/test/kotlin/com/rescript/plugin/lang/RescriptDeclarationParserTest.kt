package com.rescript.plugin.lang

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.intellij.testFramework.ParsingTestCase
import com.rescript.plugin.lang.psi.RescriptElementTypes

/**
 * Tests for [RescriptDeclarationParser] focusing on edge cases in declaration
 * parsing, annotation handling, and error recovery.
 *
 * Uses [ParsingTestCase] to exercise the real parsing pipeline with actual
 * lexer and PsiBuilder integration.
 *
 * @see RescriptDeclarationParser
 * @see RescriptParserTest for general parser coverage
 */
class RescriptDeclarationParserTest :
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
    // parseDeclaration: identifier edge cases
    // ════════════════════════════════════════════════════════════════

    fun testLetWithUnderscore() {
        assertHasElements("let _ = 42", RescriptElementTypes.LET_DECLARATION, 1)
    }

    fun testLetWithUppercaseIdentifier() {
        // UIDENT is in IDENTIFIER_TOKENS, should be accepted
        assertHasElements("let X = 42", RescriptElementTypes.LET_DECLARATION, 1)
    }

    fun testExternalWithUnderscore() {
        assertHasElements(
            "external _ : int => unit = \"fn\"",
            RescriptElementTypes.EXTERNAL_DECLARATION,
            1,
        )
    }

    fun testExceptionWithUppercaseIdentifier() {
        assertHasElements("exception MyError", RescriptElementTypes.EXCEPTION_DECLARATION, 1)
    }

    fun testExceptionWithComplexPayload() {
        assertHasElements(
            "exception HttpError({code: int, message: string})",
            RescriptElementTypes.EXCEPTION_DECLARATION,
            1,
        )
    }

    // ════════════════════════════════════════════════════════════════
    // parseDeclaration: rec keyword handling
    // ════════════════════════════════════════════════════════════════

    fun testLetRecWithUnderscore() {
        val file = parseCode("let rec _ = () => ()")
        assertEquals(1, findElements(file, RescriptElementTypes.LET_DECLARATION).size)
    }

    fun testTypeRecMultiple() {
        val code =
            """
            type rec tree = Leaf | Node(tree, tree)
            type rec list<'a> = Nil | Cons('a, list<'a>)
            """.trimIndent()
        assertHasElements(code, RescriptElementTypes.TYPE_DECLARATION, 2)
    }

    fun testLetWithoutRecStillWorks() {
        // consumeRec=true but no rec keyword present — should just skip
        assertHasElements("let simple = 1", RescriptElementTypes.LET_DECLARATION, 1)
    }

    // ════════════════════════════════════════════════════════════════
    // parseDeclaration: error recovery — missing identifier
    // ════════════════════════════════════════════════════════════════

    fun testExternalMissingIdentifier() {
        val file = parseCode("external : int => unit = \"fn\"")
        val errors = findErrors(file)
        assertTrue("Should report missing identifier for external", errors.isNotEmpty())
        assertEquals(1, findElements(file, RescriptElementTypes.EXTERNAL_DECLARATION).size)
    }

    fun testExceptionMissingIdentifier() {
        // 'exception' followed by a non-identifier, non-top-level token
        val file = parseCode("exception = NotValid")
        val errors = findErrors(file)
        assertTrue("Should report missing identifier for exception", errors.isNotEmpty())
        assertEquals(1, findElements(file, RescriptElementTypes.EXCEPTION_DECLARATION).size)
    }

    fun testMissingIdentifierFollowedByTopLevel() {
        // When the next token IS a top-level start, no error should be reported
        // because the parser treats it as "no identifier, skip" gracefully
        val code =
            """
            let = 42
            type t = int
            """.trimIndent()
        val file = parseCode(code)
        assertEquals(1, findElements(file, RescriptElementTypes.LET_DECLARATION).size)
        assertEquals(1, findElements(file, RescriptElementTypes.TYPE_DECLARATION).size)
    }

    fun testMissingIdentifierAtEof() {
        val file = parseCode("let")
        assertEquals(1, findElements(file, RescriptElementTypes.LET_DECLARATION).size)
    }

    // ════════════════════════════════════════════════════════════════
    // parseSimple: open / include edge cases
    // ════════════════════════════════════════════════════════════════

    fun testOpenDottedPath() {
        assertHasElements("open Belt.Array", RescriptElementTypes.OPEN_STATEMENT, 1)
    }

    fun testIncludeDottedPath() {
        assertHasElements("include React.Component", RescriptElementTypes.INCLUDE_STATEMENT, 1)
    }

    fun testOpenAtEndOfFile() {
        val file = parseCode("open")
        assertEquals(1, findElements(file, RescriptElementTypes.OPEN_STATEMENT).size)
    }

    fun testIncludeAtEndOfFile() {
        val file = parseCode("include")
        assertEquals(1, findElements(file, RescriptElementTypes.INCLUDE_STATEMENT).size)
    }

    fun testMultipleOpenStatements() {
        val code =
            """
            open Belt
            open Belt.Array
            open Belt.Map
            """.trimIndent()
        assertHasElements(code, RescriptElementTypes.OPEN_STATEMENT, 3)
    }

    // ════════════════════════════════════════════════════════════════
    // parseAnnotation: various forms
    // ════════════════════════════════════════════════════════════════

    fun testAnnotationWithNestedParens() {
        // @module({"key": "value"}) — parens with nested braces
        assertHasElements("@module({\"key\": \"value\"})", RescriptElementTypes.ANNOTATION, 1)
    }

    fun testAnnotationWithEmptyParens() {
        assertHasElements("@foo()", RescriptElementTypes.ANNOTATION, 1)
    }

    fun testAnnotationMultipleDotted() {
        // Deep dotted path: @a.b.c
        assertHasElements("@a.b.c", RescriptElementTypes.ANNOTATION, 1)
    }

    fun testMultipleAnnotationsBeforeDeclaration() {
        val code =
            """
            @genType
            @react.component
            let make = () => <div />
            """.trimIndent()
        val file = parseCode(code)
        assertEquals(2, findElements(file, RescriptElementTypes.ANNOTATION).size)
        assertEquals(1, findElements(file, RescriptElementTypes.LET_DECLARATION).size)
    }

    fun testAnnotationWithComplexArgs() {
        // @send annotation with complex argument
        assertHasElements("@send", RescriptElementTypes.ANNOTATION, 1)
    }

    fun testAnnotationFollowedByEof() {
        val file = parseCode("@module")
        assertEquals(1, findElements(file, RescriptElementTypes.ANNOTATION).size)
    }

    fun testAnnotationWithStringArg() {
        assertHasElements("@val(\"document\")", RescriptElementTypes.ANNOTATION, 1)
    }

    // ════════════════════════════════════════════════════════════════
    // module declaration edge cases (parsed in RescriptParser but
    // delegates identifier parsing similar to RescriptDeclarationParser)
    // ════════════════════════════════════════════════════════════════

    fun testModuleAlias() {
        // Module alias without braces: module X = Y
        assertHasElements("module X = Y", RescriptElementTypes.MODULE_DECLARATION, 1)
    }

    fun testNestedModules() {
        val code =
            """
            module Outer = {
              module Inner = {
                let x = 1
              }
            }
            """.trimIndent()
        val file = parseCode(code)
        assertEquals(2, findElements(file, RescriptElementTypes.MODULE_DECLARATION).size)
    }

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
        val file = parseCode(code)
        assertEquals(3, findElements(file, RescriptElementTypes.MODULE_DECLARATION).size)
    }

    // ════════════════════════════════════════════════════════════════
    // compound edge cases: declaration sequences
    // ════════════════════════════════════════════════════════════════

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
        val file = parseCode(code)
        val errors = findErrors(file)
        assertTrue("All declaration types should parse without errors", errors.isEmpty())
        assertTrue(findElements(file, RescriptElementTypes.ANNOTATION).isNotEmpty())
        assertTrue(findElements(file, RescriptElementTypes.LET_DECLARATION).isNotEmpty())
        assertTrue(findElements(file, RescriptElementTypes.TYPE_DECLARATION).isNotEmpty())
        assertTrue(findElements(file, RescriptElementTypes.MODULE_DECLARATION).isNotEmpty())
        assertTrue(findElements(file, RescriptElementTypes.EXTERNAL_DECLARATION).isNotEmpty())
        assertTrue(findElements(file, RescriptElementTypes.OPEN_STATEMENT).isNotEmpty())
        assertTrue(findElements(file, RescriptElementTypes.INCLUDE_STATEMENT).isNotEmpty())
        assertTrue(findElements(file, RescriptElementTypes.EXCEPTION_DECLARATION).isNotEmpty())
    }

    fun testDeclarationAfterAnnotationWithArgs() {
        val code =
            """
            @module("fs")
            external readFileSync: string => string = "readFileSync"
            """.trimIndent()
        val file = parseCode(code)
        val errors = findErrors(file)
        assertTrue("Annotation + external should have no errors", errors.isEmpty())
        assertEquals(1, findElements(file, RescriptElementTypes.ANNOTATION).size)
        assertEquals(1, findElements(file, RescriptElementTypes.EXTERNAL_DECLARATION).size)
    }

    fun testEmptyModuleNoError() {
        val code =
            """
            module Empty = {
            }
            """.trimIndent()
        val file = parseCode(code)
        val errors = findErrors(file)
        assertTrue("Empty module should have no errors", errors.isEmpty())
        assertEquals(1, findElements(file, RescriptElementTypes.MODULE_DECLARATION).size)
    }

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
        val file = parseCode(code)
        assertEquals(2, findElements(file, RescriptElementTypes.LET_DECLARATION).size)
    }

    fun testTypeWithVariantsAndRecord() {
        val code =
            """
            type shape =
              | Circle({radius: float})
              | Rectangle({width: float, height: float})
              | Point
            """.trimIndent()
        val file = parseCode(code)
        assertEquals(1, findElements(file, RescriptElementTypes.TYPE_DECLARATION).size)
    }
}
