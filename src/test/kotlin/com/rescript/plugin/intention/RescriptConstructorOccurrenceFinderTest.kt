package com.rescript.plugin.intention

import com.intellij.mock.MockVirtualFile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Unit tests for [RescriptConstructorOccurrenceFinder.findInText].
 *
 * The IO entry point ([RescriptConstructorOccurrenceFinder.findAll])
 * piggybacks on `PsiSearchHelper`, which can't run without an
 * IntelliJ Platform fixture. The pure helper drives the same classifier
 * the IO entry point uses, so the rename intention's correctness rests
 * on these cases — anything covered here will behave identically once
 * the IO path produces the same text.
 */
class RescriptConstructorOccurrenceFinderTest {
    private fun mockFile(name: String) = MockVirtualFile(name)

    @Test
    fun `multiple occurrences in a single file are all collected`() {
        val text =
            """
            type t = | Foo | Bar
            let v = Foo
            switch v {
            | Foo => 1
            | Bar => 0
            }
            """.trimIndent()
        val result =
            RescriptConstructorOccurrenceFinder.findInText(
                mockFile("F.res"),
                text,
                "Foo",
            )
        assertEquals(3, result.size)
        // type arm + expression-position constructor + switch arm
        val kinds = result.map { it.kind }.toSet()
        assertTrue(kinds.contains(ConstructorOccurrenceKind.PATTERN))
        assertTrue(kinds.contains(ConstructorOccurrenceKind.CONSTRUCTOR))
    }

    @Test
    fun `module-qualified call is collected as MODULE_QUALIFIED_TAIL`() {
        val text = "let x = Result.Foo(1)"
        val result =
            RescriptConstructorOccurrenceFinder.findInText(
                mockFile("M.res"),
                text,
                "Foo",
            )
        assertEquals(1, result.size)
        assertEquals(ConstructorOccurrenceKind.MODULE_QUALIFIED_TAIL, result.first().kind)
    }

    @Test
    fun `JSX element name is filtered out`() {
        val text = "let el = <Foo />"
        val result =
            RescriptConstructorOccurrenceFinder.findInText(
                mockFile("J.res"),
                text,
                "Foo",
            )
        assertEquals(0, result.size)
    }

    @Test
    fun `string literal contents are filtered out`() {
        val text = "let s = \"Foo bar\""
        val result =
            RescriptConstructorOccurrenceFinder.findInText(
                mockFile("S.res"),
                text,
                "Foo",
            )
        assertEquals(0, result.size)
    }

    @Test
    fun `comment contents are filtered out`() {
        val text =
            """
            // Foo is a constructor
            /* Foo bar */
            let x = 1
            """.trimIndent()
        val result =
            RescriptConstructorOccurrenceFinder.findInText(
                mockFile("C.res"),
                text,
                "Foo",
            )
        assertEquals(0, result.size)
    }

    @Test
    fun `name embedded in a longer identifier is not matched`() {
        // `FooBar` and `xFoo` must not register as `Foo` matches.
        val text =
            """
            type t = | FooBar
            let xFoo = 1
            let yFoo_ = 2
            """.trimIndent()
        val result =
            RescriptConstructorOccurrenceFinder.findInText(
                mockFile("L.res"),
                text,
                "Foo",
            )
        assertEquals(0, result.size)
    }

    @Test
    fun `range covers exactly the UIDENT for each hit`() {
        val text = "let v = Foo"
        val result =
            RescriptConstructorOccurrenceFinder.findInText(
                mockFile("R.res"),
                text,
                "Foo",
            )
        assertEquals(1, result.size)
        val occ = result.first()
        assertEquals("Foo", text.substring(occ.range.startOffset, occ.range.endOffset))
    }

    @Test
    fun `mixed valid and skipped contexts are partitioned correctly`() {
        val text =
            """
            type t = | Foo | Bar
            let s = "Foo"
            // Foo
            let el = <Foo />
            let v = Foo
            """.trimIndent()
        val result =
            RescriptConstructorOccurrenceFinder.findInText(
                mockFile("Mix.res"),
                text,
                "Foo",
            )
        // Only the `type` arm and the `let v = Foo` should match.
        assertEquals(2, result.size)
    }
}
