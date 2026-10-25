package com.rescript.plugin.intention

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/** Balanced-token regressions shared by both public pipe intentions. */
class RescriptPipeConversionTest {
    private fun pipe(
        text: String,
        caret: Int = text.indexOf("->Array") + 1,
    ): String? =
        RescriptPipeConversion.pipe(text, caret)?.let {
            RescriptConvertPipeToFunctionCallIntention.convertPipeToFunctionCall(it)
        }

    private fun call(
        text: String,
        caret: Int = text.indexOf("Array.") + 2,
    ): String? =
        RescriptPipeConversion.call(text, caret)?.let {
            RescriptConvertFunctionCallToPipeIntention.convertFunctionCallToPipe(it)
        }

    @Test
    fun `unit call is retained in both directions`() {
        assertEquals("Array.length(getItems())", pipe("getItems()->Array.length"))
        assertEquals("getItems()->Array.length", call("Array.length(getItems())"))
    }

    @Test
    fun `nested calls strings and arrays retain commas and parentheses`() {
        val lhs = "getItems([1, 2], \"a,) -> x\")"
        val args = "f(\"),\", [3, 4]), {value: 2}"
        assertEquals("Array.reduce($lhs, $args)", pipe("$lhs->Array.reduce($args)"))
        assertEquals("$lhs->Array.reduce($args)", call("Array.reduce($lhs, $args)"))
    }

    @Test
    fun `multiline expressions retain complete ranges`() {
        val text = "let result = getItems(\n  [1, 2]\n)\n  ->Array.map(\n    f(1, 2)\n  )\nlet other = 1"
        val match = RescriptPipeConversion.pipe(text, text.indexOf("->") + 1)!!
        assertEquals("getItems(\n  [1, 2]\n)", match.lhs)
        assertEquals("\n    f(1, 2)\n  ", match.args)
        assertEquals(
            "getItems(\n  [1, 2]\n)\n  ->Array.map(\n    f(1, 2)\n  )",
            text.substring(match.fullStart, match.fullEnd),
        )
        assertEquals("getItems(\n  [1, 2]\n)->Array.length", call("Array.length(\ngetItems(\n  [1, 2]\n)\n)"))
    }

    @Test
    fun `comments do not split arguments and all comments survive`() {
        val text = "Array.map(getItems(/* ), -> */), /* , ) */ f)"
        assertEquals("getItems(/* ), -> */)->Array.map(/* , ) */ f)", call(text))
        assertEquals(
            "Array.length(/* right */ getItems() /* left */)",
            pipe("getItems() /* left */ -> /* right */ Array.length"),
        )
        assertEquals(
            "getItems()->Array /* dot */ . /* name */ length /* call */ ",
            call("Array /* dot */ . /* name */ length /* call */ (getItems())", 2),
        )
    }

    @Test
    fun `line comments remain terminated after movement`() {
        assertEquals("getItems() // first\n->Array.map(f)", call("Array.map(getItems() // first\n, f)"))
        assertEquals("Array.length(// right\ngetItems())", pipe("getItems()-> // right\nArray.length"))
    }

    @Test
    fun `pipe chain includes earlier stages without dropping the unit call`() {
        val text = "getItems()->Array.map(f)->Array.length"
        assertEquals("Array.length(getItems()->Array.map(f))", pipe(text, text.lastIndexOf("->") + 1))
    }

    @Test
    fun `caret in string or comment does not offer a rewrite`() {
        val string = "Array.map(\"x -> Array.length\", f)"
        assertNull(call(string, string.indexOf("x ->")))
        val comment = "/* getItems()->Array.length */"
        assertNull(pipe(comment))
        assertNull(pipe("let text = \"getItems()->Array.length\""))
    }

    @Test
    fun `unsupported and incomplete input is refused`() {
        listOf(
            "a + b->Array.length",
            "a|>Array.length",
            "getItems(->Array.length",
            "getItems()->Array.length()()",
            "getItems()->Array.length /* unfinished",
            "getItems()->Array.length(\"unfinished)",
            "getItems()->Array.length(/* only */)",
        ).forEach {
            assertNull(pipe(it), it)
        }
        listOf(
            "Array.map(~items=arr, f)",
            "Array.map(a + b, f)",
            "Array.map(arr,, f)",
            "Array.map(arr, f)",
            "Array.map(arr, f)()",
        ).forEach {
            val text = if (it == "Array.map(arr, f)") "$it + unknown" else it
            assertNull(call(text), text)
        }
    }

    @Test
    fun `unknown argument shapes are refused recursively in both directions`() {
        val arguments =
            listOf(
                "a b",
                "getItems(a b)",
                "[a b]",
                "{value: a b}",
                "f([a b], {value: 1})",
                "x => a b",
                "(x, x) => x",
                "(x y) => x",
                "f(x => getItems(a b))",
                "a ? b",
                "a +",
                "let x = 1",
            )
        arguments.forEach { argument ->
            assertNull(call("Array.map(arr, $argument)"), argument)
            assertNull(pipe("arr->Array.map($argument)"), argument)
        }
        listOf("getItems(a b)", "[a b]", "({value: a b})").forEach { lhs ->
            assertNull(call("Array.length($lhs)"), lhs)
            assertNull(pipe("$lhs->Array.length"), lhs)
        }
    }

    @Test
    fun `lambda body is a complete expression boundary in both directions`() {
        val before = "let sizes = Array.map([[1], [2, 3]], items => items->Array.length)"
        val match = RescriptPipeConversion.pipe(before, before.indexOf("->") + 1)!!
        assertEquals("items", match.lhs)
        assertEquals("Array.length(items)", pipe(before))
        val after = "let sizes = Array.map([[1], [2, 3]], items => Array.length(items))"
        val reverse = RescriptPipeConversion.call(after, after.lastIndexOf("Array.length") + 2)!!
        assertEquals(
            "items->Array.length",
            RescriptConvertFunctionCallToPipeIntention.convertFunctionCallToPipe(reverse),
        )
        assertEquals("Array.length(getItems())", pipe("let f = () => getItems()->Array.length"))
        assertEquals("Array.length(items)", pipe("let f = (items, ignored) => items->Array.length"))
    }

    @Test
    fun `lambda boundary requires a supported parameter list`() {
        listOf(
            "a b => items->Array.length",
            "(a b) => items->Array.length",
            "(x, x) => items->Array.length",
            "(x: int) => items->Array.length",
        ).forEach {
            assertNull(pipe("let f = $it"), it)
        }
        assertNull(call("let f = a b => Array.length(items)"))
    }

    @Test
    fun `known nested expression shapes are retained without reprinting`() {
        val args = """(x, y) => f(x + y, {value: [1, 2]}), g("a b,)->")"""
        assertEquals("Array.reduce(getItems(), $args)", pipe("getItems()->Array.reduce($args)"))
        assertEquals("getItems()->Array.reduce($args)", call("Array.reduce(getItems(), $args)"))
        assertEquals("Array.map(arr, x => {f(x, 2)})", pipe("arr->Array.map(x => {f(x, 2)})"))
    }

    @Test
    fun `balanced argument splitting ignores lexical delimiters`() {
        assertEquals(
            listOf("foo([1, 2])", " \"a,b)\"", " /* , */ {a: 1, b: 2}"),
            RescriptPipeConversion.splitArguments("foo([1, 2]), \"a,b)\", /* , */ {a: 1, b: 2}"),
        )
        assertEquals(emptyList<String>(), RescriptPipeConversion.splitArguments("foo([1, 2), f"))
    }

    @Test
    fun `parenthesized expressions and record arguments remain grouped`() {
        assertEquals("Array.length((a + b))", pipe("(a + b)->Array.length"))
        assertEquals("({items: [1, 2]})->Array.length", call("Array.length(({items: [1, 2]}))"))
        assertNotNull(RescriptPipeConversion.call("Array.map(arr, x => f(x, 2))", 2))
    }
}
