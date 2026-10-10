package com.rescript.plugin.refactor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class RescriptExtractFunctionHandlerTest {
    private fun plan(marked: String): RescriptExtractFunctionPlan? {
        val start = marked.indexOf("<selection>")
        val end = marked.indexOf("</selection>") - "<selection>".length
        val text = marked.replace("<selection>", "").replace("</selection>", "")
        return RescriptExtractFunctionPlan.create(text, start, end)
    }

    @Test
    fun `qualified members strings and comments are not free variables`() {
        val result = plan("let x = 42\n<selection>Console.log(x) // log hello</selection>")!!
        assertEquals(listOf("x"), result.parameters)
        assertEquals(emptyList<String>(), plan("<selection>Console.log(\"hello\")</selection>")!!.parameters)
        assertEquals(
            listOf("r"),
            plan("let r = ref(1)\n<selection>Console.log(r.contents)</selection>")!!.parameters,
        )
    }

    @Test
    fun `only resolved external bindings become parameters`() {
        assertEquals(listOf("a", "z"), plan("let z = 1\nlet a = 2\n<selection>z + a</selection>")!!.parameters)
        assertNull(plan("<selection>unknown + 1</selection>"))
        assertNull(plan("<selection>x + 1</selection>\nlet x = 2"))
        assertNull(RescriptExtractFunctionHandler.findFreeVariables("x", "let x = 1", 0))
    }

    @Test
    fun `local let binding is resolved inside selection and cannot escape`() {
        assertEquals(
            listOf("x"),
            plan("let x = 1\nlet result = {\n<selection>let temp = x + 1\ntemp * 2</selection>\n}")!!.parameters,
        )
        assertNull(plan("let x = 1\nlet result = {\n<selection>let temp = x + 1</selection>\ntemp * 2\n}"))
        assertNull(plan("let result = {\n<selection>let temp = 1</selection>\nA.call(~temp)\n}"))
    }

    @Test
    fun `block lambda parameters are local while captured bindings are external`() {
        assertEquals(
            listOf("base", "item"),
            plan("let base = 1\nlet compute = (item) => {\n<selection>item + base</selection>\n}")!!.parameters,
        )
        assertEquals(
            listOf("y"),
            plan(
                "let y = 1\nlet empty = (y) => {}\nlet compute = () => {\n<selection>y + 1</selection>\n}",
            )!!.parameters,
        )
        assertEquals(listOf("item"), plan("let compute = (item) => {\n<selection>item + 1</selection>\n}")!!.parameters)
        assertNull(plan("let compute = (<selection>item</selection>) => {item + 1}"))
        assertEquals(
            listOf("x"),
            plan("let x = 1\nlet compute = (x) => {\n<selection>x + 1</selection>\n}")!!.parameters,
        )
    }

    @Test
    fun `generated name avoids existing declarations and reference spellings`() {
        val result =
            plan(
                "let extractedFunction = 1\nlet extractedFunction2 = 2\n<selection>extractedFunction + extractedFunction2</selection>",
            )!!
        assertEquals("extractedFunction3", result.name)
    }

    @Test
    fun `private helpers and parameter shadowing preserve lexical binding identities`() {
        val result =
            plan(
                "let x = 1\n@private\nlet extractedFunction = (x) => {x + 1}\n" +
                    "let exported = extractedFunction(x)\n<selection>x + 2</selection>",
            )!!
        assertEquals(listOf("x"), result.parameters)
        assertEquals("extractedFunction2", result.name)
    }

    @Test
    fun `ambiguous selection boundaries patterns opens and shorthand labels fail closed`() {
        for (text in listOf(
            "let x = 1\n<selection>x +</selection> 2",
            "let x = 1\n<selection>Console</selection>.log(x)",
            "let x = 1\nConsole.<selection>log</selection>(x)",
            "let (x, y) = pair\n<selection>x + y</selection>",
            "open A\nlet x = 1\n<selection>x + 1</selection>",
            "let x = 1\n<selection>call(~x)</selection>",
            "let x = 1\n<selection>x => x + 1</selection>",
            "let identity = <selection>(x) => {x}</selection>",
            "let identity = <selection>A.identity</selection>",
            "let value = <selection>A.Any</selection>",
            "<selection>A.compute(1, ...)</selection>",
            "let compute = (record) => {\n<selection>record.identity</selection>\n}",
            "let r = ref(1)\n<selection>r.contents.identity</selection>",
            "let compute = (record) => {\n<selection>(record).identity</selection>\n}",
            "let compute = (record) => {\n<selection>record[\"identity\"]</selection>\n}",
            "let compute = (record) => {\nrecord[<selection>\"identity\"</selection>]\n}",
            "let empty = (y) => {}\nlet compute = () => {\n<selection>y + 1</selection>\n}",
            "let compute = () =>\n<selection>{Console.log(1)}</selection>",
            "let value =\n<selection>1</selection>",
            "let value = 1 +\n<selection>2</selection>",
            "Console.log(\n<selection>1</selection>\n)",
            "<selection>A.identity</selection>(1)",
            "<selection>ref</selection>(1)",
            "let identity = <selection>A.identity(_)</selection>",
            "<selection>@private</selection>\nlet value = 1",
            "<selection>Js.Global.eval(\"1\")</selection>",
            "A.consume(<selection>{field: 1}</selection>)",
            "A.consume({\n field: <selection>1</selection>\n})",
            "let t = (x) => {x}\nlet compute = (): A.t => {\n<selection>t</selection>\n}",
            "let compute = (_) => {\n<selection>A.identity(_)</selection>\n}",
            "let identity = (item) => {item}\n<selection>{let n = identity(1)\nidentity(\"hello\")}</selection>",
            "let values = List.empty()\n<selection>Console.log(values)</selection>",
            "let value = 1->A.make()\n<selection>Console.log(value)</selection>",
            "let value = 1\n->A.make()\n<selection>Console.log(value)</selection>",
            "<selection>let exported = 1</selection>",
            "let x = 1\n<selection>\"part</selection>ial\"",
            "<selection>\"unfinished</selection>",
            "let x = 1\n<selection>x + 1 /* unfinished</selection>",
        )) {
            assertNull(plan(text), text)
        }
    }

    @Test
    fun `generated functions always use block bodies and calls evaluate once`() {
        assertEquals(
            "let add = (a, b) => {\n  a + b\n}",
            RescriptExtractFunctionHandler.generateFunction("add", "a + b", listOf("a", "b")),
        )
        assertEquals(
            "let value = () => {\n  42\n}",
            RescriptExtractFunctionHandler.generateFunction("value", "42", emptyList()),
        )
        assertEquals("add(a, b)", RescriptExtractFunctionHandler.generateCallSite("add", listOf("a", "b")))
        assertEquals("value()", RescriptExtractFunctionHandler.generateCallSite("value", emptyList()))
    }
}
