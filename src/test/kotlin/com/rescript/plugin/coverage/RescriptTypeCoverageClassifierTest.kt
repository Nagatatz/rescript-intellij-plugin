package com.rescript.plugin.coverage

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Unit tests for [RescriptTypeCoverageClassifier.classifyLet].
 *
 * Each case feeds a single self-contained `let` declaration to the
 * classifier and asserts the resulting tier. Annotation-bearing forms
 * cover top-level type ascriptions, function-type annotations and
 * destructuring patterns; inference forms exercise the depth-tracking
 * path so we don't false-positive on `:` tokens inside parameter lists,
 * record literals, or array literals.
 */
class RescriptTypeCoverageClassifierTest {
    private fun assertAnnotated(src: String) {
        assertEquals(
            LetCoverage.ANNOTATED,
            RescriptTypeCoverageClassifier.classifyLet(src),
            "Expected ANNOTATED: $src",
        )
    }

    private fun assertInferred(src: String) {
        assertEquals(
            LetCoverage.INFERRED,
            RescriptTypeCoverageClassifier.classifyLet(src),
            "Expected INFERRED: $src",
        )
    }

    // ── ANNOTATED: simple type ascription on the binding name ──

    @Test fun `int annotation`() = assertAnnotated("let x: int = 5")

    @Test fun `string annotation`() = assertAnnotated("let name: string = \"hi\"")

    @Test fun `bool annotation`() = assertAnnotated("let ok: bool = true")

    @Test fun `unit annotation`() = assertAnnotated("let nothing: unit = ()")

    @Test fun `polymorphic type annotation`() = assertAnnotated("let id: 'a => 'a = x => x")

    @Test fun `function type annotation`() = assertAnnotated("let add: (int, int) => int = (a, b) => a + b")

    @Test fun `nested generic type annotation`() = assertAnnotated("let opt: option<array<int>> = None")

    @Test fun `inline record type annotation`() =
        assertAnnotated("let user: {name: string, age: int} = {name: \"x\", age: 1}")

    @Test fun `tuple type annotation`() = assertAnnotated("let pair: (int, string) = (1, \"a\")")

    @Test fun `let rec with annotation`() = assertAnnotated("let rec loop: int => unit = n => loop(n - 1)")

    @Test fun `multiline annotation`() =
        assertAnnotated(
            """
            let total:
              int =
              1 + 2 + 3
            """.trimIndent(),
        )

    // ── INFERRED: no top-level type, possibly with parameter annotations ──

    @Test fun `simple inferred binding`() = assertInferred("let x = 5")

    @Test fun `simple string inferred`() = assertInferred("let s = \"hi\"")

    @Test fun `arrow function with no annotations`() = assertInferred("let f = x => x + 1")

    @Test fun `arrow function with parameter annotation only`() = assertInferred("let f = (x: int) => x + 1")

    @Test fun `arrow function with multiple parameter annotations`() =
        assertInferred(
            "let add = (x: int, y: int) => x + y",
        )

    @Test fun `arrow function with return annotation only`() = assertInferred("let f = (x): int => x + 1")

    @Test fun `arrow function with both param and return annotations`() =
        assertInferred(
            "let f = (x: int): int => x + 1",
        )

    @Test fun `let rec without annotation`() = assertInferred("let rec loop = n => loop(n - 1)")

    @Test fun `record literal without binding annotation`() = assertInferred("let user = {name: \"x\", age: 1}")

    @Test fun `array literal`() = assertInferred("let xs = [1, 2, 3]")

    @Test fun `pipe expression`() = assertInferred("let total = arr->Array.reduce(0, (acc, x) => acc + x)")

    @Test fun `tuple binding without annotation`() = assertInferred("let pair = (1, \"a\")")

    // ── Destructuring patterns ──

    @Test fun `tuple destructure with annotation`() = assertAnnotated("let (a, b): (int, string) = pair")

    @Test fun `tuple destructure without annotation`() = assertInferred("let (a, b) = pair")

    @Test fun `record destructure with annotation`() = assertAnnotated("let {x, y}: point = origin")

    @Test fun `record destructure without annotation`() = assertInferred("let {x, y} = origin")

    // ── Edge cases ──

    @Test fun `empty source defaults to inferred`() = assertInferred("")

    @Test fun `source without let keyword defaults to inferred`() = assertInferred("type t = int")

    @Test fun `colon inside record pattern but no top-level annotation`() = assertInferred("let {a: x, b: y} = obj")

    @Test fun `colon inside array of records but no annotation`() =
        assertInferred("let users = [{name: \"a\"}, {name: \"b\"}]")

    @Test fun `nested parens with internal colons stay inferred`() = assertInferred("let f = (x => (y: int) => y + x)")
}
