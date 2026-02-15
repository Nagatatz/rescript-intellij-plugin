package com.rescript.plugin.lang

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import com.intellij.testFramework.ParsingTestCase
import com.rescript.plugin.lang.psi.RescriptElementTypes

class RescriptParserTest :
    ParsingTestCase(
        "",
        "res",
        RescriptParserDefinition(),
    ) {
    override fun getTestDataPath(): String = "src/test/testData/parser"

    override fun skipSpaces(): Boolean = true

    override fun includeRanges(): Boolean = false

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

    private fun assertFirstChildToken(
        node: ASTNode,
        expectedType: IElementType,
    ) {
        var child = node.firstChildNode
        // Skip whitespace
        while (child != null && child.elementType == com.intellij.psi.TokenType.WHITE_SPACE) {
            child = child.treeNext
        }
        assertNotNull("Expected first non-ws child of type $expectedType", child)
        assertEquals(expectedType, child!!.elementType)
    }

    // ════════════════════════════════════════════════════════════════
    // let declarations
    // ════════════════════════════════════════════════════════════════

    fun testLetSimple() {
        assertHasElements("let x = 42", RescriptElementTypes.LET_DECLARATION, 1)
    }

    fun testLetRec() {
        val file = parseCode("let rec fib = x => x")
        val lets = findElements(file, RescriptElementTypes.LET_DECLARATION)
        assertEquals(1, lets.size)
        // Should contain REC token
        val recNodes = findElements(file, RescriptTokenTypes.REC)
        assertFalse("Should contain rec keyword", recNodes.isEmpty())
    }

    fun testLetMultiple() {
        val code =
            """
            let a = 1
            let b = 2
            let c = 3
            """.trimIndent()
        assertHasElements(code, RescriptElementTypes.LET_DECLARATION, 3)
    }

    fun testLetWithBraces() {
        val code =
            """
            let f = x => {
              let y = x + 1
              y
            }
            let g = 2
            """.trimIndent()
        val file = parseCode(code)
        val lets = findElements(file, RescriptElementTypes.LET_DECLARATION)
        // Top-level: f and g. Inner `let y` is inside braces and consumed by skipToEndOfDeclaration
        assertEquals(2, lets.size)
    }

    // ════════════════════════════════════════════════════════════════
    // type declarations
    // ════════════════════════════════════════════════════════════════

    fun testTypeSimple() {
        assertHasElements("type t = int", RescriptElementTypes.TYPE_DECLARATION, 1)
    }

    fun testTypeRec() {
        val file = parseCode("type rec tree = Node(tree, tree) | Leaf")
        val types = findElements(file, RescriptElementTypes.TYPE_DECLARATION)
        assertEquals(1, types.size)
    }

    fun testTypeWithBraces() {
        val code =
            """
            type person = {
              name: string,
              age: int,
            }
            type color = Red | Green | Blue
            """.trimIndent()
        assertHasElements(code, RescriptElementTypes.TYPE_DECLARATION, 2)
    }

    // ════════════════════════════════════════════════════════════════
    // module declarations
    // ════════════════════════════════════════════════════════════════

    fun testModuleSimple() {
        val code =
            """
            module M = {
              let x = 1
            }
            """.trimIndent()
        assertHasElements(code, RescriptElementTypes.MODULE_DECLARATION, 1)
    }

    fun testModuleType() {
        val code =
            """
            module type S = {
              let x: int
            }
            """.trimIndent()
        val file = parseCode(code)
        val modules = findElements(file, RescriptElementTypes.MODULE_DECLARATION)
        assertEquals(1, modules.size)
    }

    fun testModuleRec() {
        val code =
            """
            module rec A = {
              let x = 1
            }
            """.trimIndent()
        assertHasElements(code, RescriptElementTypes.MODULE_DECLARATION, 1)
    }

    // ════════════════════════════════════════════════════════════════
    // external declarations
    // ════════════════════════════════════════════════════════════════

    fun testExternal() {
        assertHasElements(
            "external log : string => unit = \"console.log\"",
            RescriptElementTypes.EXTERNAL_DECLARATION,
            1,
        )
    }

    fun testExternalMultiple() {
        val code =
            """
            external log : string => unit = "console.log"
            external alert : string => unit = "alert"
            """.trimIndent()
        assertHasElements(code, RescriptElementTypes.EXTERNAL_DECLARATION, 2)
    }

    // ════════════════════════════════════════════════════════════════
    // open / include statements
    // ════════════════════════════════════════════════════════════════

    fun testOpen() {
        assertHasElements("open Belt", RescriptElementTypes.OPEN_STATEMENT, 1)
    }

    fun testInclude() {
        assertHasElements("include Common", RescriptElementTypes.INCLUDE_STATEMENT, 1)
    }

    fun testOpenAndInclude() {
        val code =
            """
            open Belt
            include Common
            """.trimIndent()
        val file = parseCode(code)
        assertEquals(1, findElements(file, RescriptElementTypes.OPEN_STATEMENT).size)
        assertEquals(1, findElements(file, RescriptElementTypes.INCLUDE_STATEMENT).size)
    }

    // ════════════════════════════════════════════════════════════════
    // exception declarations
    // ════════════════════════════════════════════════════════════════

    fun testException() {
        assertHasElements("exception NotFound", RescriptElementTypes.EXCEPTION_DECLARATION, 1)
    }

    fun testExceptionWithPayload() {
        assertHasElements(
            "exception HttpError(int, string)",
            RescriptElementTypes.EXCEPTION_DECLARATION,
            1,
        )
    }

    // ════════════════════════════════════════════════════════════════
    // annotations
    // ════════════════════════════════════════════════════════════════

    fun testAnnotationSimple() {
        assertHasElements("@module", RescriptElementTypes.ANNOTATION, 1)
    }

    fun testAnnotationDotted() {
        assertHasElements("@react.component", RescriptElementTypes.ANNOTATION, 1)
    }

    fun testAnnotationWithArgs() {
        assertHasElements("@module(\"fs\")", RescriptElementTypes.ANNOTATION, 1)
    }

    fun testDoubleAnnotation() {
        // @@ produces two ARROBASE tokens; the parser handles first @ as annotation,
        // second @ starts another annotation
        val file = parseCode("@@deriving")
        val annotations = findElements(file, RescriptElementTypes.ANNOTATION)
        // The parser sees first @ → ANNOTATION (empty, no name after @),
        // then second @ → ANNOTATION with name "deriving"
        // Actual count depends on parser implementation
        assertTrue("Should have at least 1 annotation", annotations.isNotEmpty())
    }

    // ════════════════════════════════════════════════════════════════
    // compound cases
    // ════════════════════════════════════════════════════════════════

    fun testAnnotationFollowedByDeclaration() {
        val code =
            """
            @react.component
            let make = () => <div />
            """.trimIndent()
        val file = parseCode(code)
        assertEquals(1, findElements(file, RescriptElementTypes.ANNOTATION).size)
        assertEquals(1, findElements(file, RescriptElementTypes.LET_DECLARATION).size)
    }

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
        val file = parseCode(code)
        assertEquals(1, findElements(file, RescriptElementTypes.OPEN_STATEMENT).size)
        assertEquals(1, findElements(file, RescriptElementTypes.TYPE_DECLARATION).size)
        assertEquals(2, findElements(file, RescriptElementTypes.LET_DECLARATION).size) // top-level x + inner y
        assertEquals(1, findElements(file, RescriptElementTypes.MODULE_DECLARATION).size)
        assertEquals(1, findElements(file, RescriptElementTypes.EXTERNAL_DECLARATION).size)
        assertEquals(1, findElements(file, RescriptElementTypes.INCLUDE_STATEMENT).size)
        assertEquals(1, findElements(file, RescriptElementTypes.EXCEPTION_DECLARATION).size)
    }

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
        val file = parseCode(code)
        val lets = findElements(file, RescriptElementTypes.LET_DECLARATION)
        // Only top-level: f and g
        assertEquals(2, lets.size)
    }

    fun testAnnotationBeforeModule() {
        val code =
            """
            @module("react")
            external make : unit => React.element = "default"
            """.trimIndent()
        val file = parseCode(code)
        assertEquals(1, findElements(file, RescriptElementTypes.ANNOTATION).size)
        assertEquals(1, findElements(file, RescriptElementTypes.EXTERNAL_DECLARATION).size)
    }

    // ════════════════════════════════════════════════════════════════
    // error recovery: unexpected tokens (R1)
    // ════════════════════════════════════════════════════════════════

    fun testUnexpectedTokensCreateErrorNode() {
        val file = parseCode("42 + 1")
        val errors = findErrors(file)
        assertTrue("Should have error nodes for unexpected tokens", errors.isNotEmpty())
    }

    fun testUnexpectedTokensBeforeDeclaration() {
        val code =
            """
            42 + 1
            let x = 1
            """.trimIndent()
        val file = parseCode(code)
        val errors = findErrors(file)
        assertTrue("Should have error node for '42 + 1'", errors.isNotEmpty())
        assertEquals(1, findElements(file, RescriptElementTypes.LET_DECLARATION).size)
    }

    fun testUnexpectedTokensBetweenDeclarations() {
        // Garbage between declarations is consumed by skipToEndOfDeclaration as part of the preceding
        // declaration body. The parser still correctly identifies the next declaration.
        val code =
            """
            let a = 1
            foo bar baz
            let b = 2
            """.trimIndent()
        val file = parseCode(code)
        assertEquals(2, findElements(file, RescriptElementTypes.LET_DECLARATION).size)
    }

    // ════════════════════════════════════════════════════════════════
    // error recovery: missing identifiers (R2)
    // ════════════════════════════════════════════════════════════════

    fun testLetMissingIdentifier() {
        val code = "let = 42"
        val file = parseCode(code)
        val errors = findErrors(file)
        assertTrue("Should report missing identifier", errors.isNotEmpty())
        assertEquals(1, findElements(file, RescriptElementTypes.LET_DECLARATION).size)
    }

    fun testTypeMissingIdentifier() {
        val code = "type = int"
        val file = parseCode(code)
        val errors = findErrors(file)
        assertTrue("Should report missing identifier", errors.isNotEmpty())
        assertEquals(1, findElements(file, RescriptElementTypes.TYPE_DECLARATION).size)
    }

    fun testModuleMissingName() {
        val code =
            """
            module = {
              let x = 1
            }
            """.trimIndent()
        val file = parseCode(code)
        val errors = findErrors(file)
        assertTrue("Should report missing module name", errors.isNotEmpty())
        assertEquals(1, findElements(file, RescriptElementTypes.MODULE_DECLARATION).size)
    }

    fun testMissingIdentifierRecovery() {
        val code =
            """
            let = 42
            let y = 2
            """.trimIndent()
        val file = parseCode(code)
        assertEquals(
            "Both let declarations should be recognized",
            2,
            findElements(file, RescriptElementTypes.LET_DECLARATION).size,
        )
    }

    // ════════════════════════════════════════════════════════════════
    // error recovery: unbalanced braces (R3)
    // ════════════════════════════════════════════════════════════════

    fun testModuleMissingClosingBrace() {
        val code =
            """
            module M = {
              let x = 1
            let y = 2
            """.trimIndent()
        val file = parseCode(code)
        // Module should still be created, and parser should not crash
        assertEquals(1, findElements(file, RescriptElementTypes.MODULE_DECLARATION).size)
    }

    // ════════════════════════════════════════════════════════════════
    // error recovery: compound error cases (R4)
    // ════════════════════════════════════════════════════════════════

    fun testMultipleErrors() {
        val code =
            """
            garbage tokens here
            let x = 1
            more garbage
            type t = int
            let = 42
            """.trimIndent()
        val file = parseCode(code)
        val errors = findErrors(file)
        // "garbage tokens here" → error node; "more garbage" consumed by let x body; "let = 42" → missing identifier error
        assertTrue("Should have error nodes", errors.isNotEmpty())
        assertEquals(2, findElements(file, RescriptElementTypes.LET_DECLARATION).size)
        assertEquals(1, findElements(file, RescriptElementTypes.TYPE_DECLARATION).size)
    }

    fun testEmptyFileNoErrors() {
        val file = parseCode("")
        val errors = findErrors(file)
        assertTrue("Empty file should have no errors", errors.isEmpty())
    }

    fun testValidCodeNoErrors() {
        val code =
            """
            let x = 1
            type t = int
            module M = {
              let y = 2
            }
            """.trimIndent()
        val file = parseCode(code)
        val errors = findErrors(file)
        assertTrue("Valid code should have no errors", errors.isEmpty())
    }

    // ── helper for error recovery tests ─────────────────────────────

    private fun findErrors(file: PsiFile): List<ASTNode> {
        val result = mutableListOf<ASTNode>()

        fun walk(node: ASTNode) {
            if (node.elementType == com.intellij.psi.TokenType.ERROR_ELEMENT) result.add(node)
            var child = node.firstChildNode
            while (child != null) {
                walk(child)
                child = child.treeNext
            }
        }
        walk(file.node)
        return result
    }
}
