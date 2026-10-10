package com.rescript.plugin.refactor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptInlineHandlerTest {
    @Test
    fun `constant plan excludes strings comments qualified fields and partial names`() {
        val text = "let x = 42\nlet xy = 1\nConsole.log(\"x\") // x\nConsole.log(A.x)\nConsole.log(x)"
        val plan = RescriptInlinePlan.create(text, 4)!!
        assertEquals(listOf(text.lastIndexOf("x")), plan.usages)
        assertFalse(plan.removeDeclaration)
    }

    @Test
    fun `local plan stays within its declaring block`() {
        val text = "let result = {\n  let x = 1 + 2\n  Console.log(x * 3)\n}\nConsole.log(A.x)"
        val plan = RescriptInlinePlan.create(text, text.indexOf("let x"))!!
        assertEquals(listOf(text.indexOf("x *")), plan.usages)
        assertTrue(plan.removeDeclaration)
        assertEquals("1 + 2", plan.value)
    }

    @Test
    fun `rebinding and shadowing are unavailable`() {
        for (text in listOf(
            "let x = 1\nlet x = 2\nConsole.log(x)",
            "let x = 1\nlet y = {\nlet x = 2\nConsole.log(x)\n}",
            "let x = 1\nlet f = x => x + 1",
            "let x = 1\nlet f = (other, x) => x + other",
            "let x = 1\nlet (x, y) = pair\nConsole.log(x)",
        )) {
            assertNull(RescriptInlinePlan.create(text, 4), text)
        }
    }

    @Test
    fun `effectful variable dependent and throwing expressions are unavailable`() {
        for (value in listOf("run()", "other + 1", "ref(1)", "1 / 0", "1 % 0", "[1, 2]")) {
            assertNull(RescriptInlinePlan.create("let x = $value\nConsole.log(x)", 4), value)
        }
    }

    @Test
    fun `multiline continuations and incomplete expressions are unavailable`() {
        for (text in listOf(
            "let x = 1 +\n2\nConsole.log(x)",
            "let x = 1\n + 2\nConsole.log(x)",
            "let x = 1\n -> run\nConsole.log(x)",
            "let x = (1 + 2\nConsole.log(x)",
            "let x = \"hello\nworld\"\nConsole.log(x)",
        )) {
            assertNull(RescriptInlinePlan.create(text, 4), text)
        }
    }

    @Test
    fun `unknown record shorthand labels templates and opens are unavailable`() {
        for (text in listOf(
            "let x = 1\nlet r = {x}",
            "let x = 1\ncall(~x)",
            "let x = 1\nlet t = `\${x}`",
            "open A\nlet x = 1\nConsole.log(x)",
            "let x = 1\n%%raw(\"console.log(x)\")\nConsole.log(x)",
            "let x = 1\nJs.Global.eval(\"x\")\nConsole.log(x)",
        )) {
            assertNull(RescriptInlinePlan.create(text, text.indexOf("let x")), text)
        }
    }

    @Test
    fun `function parameters with other names do not hide constant references`() {
        val text = "let compute = (other) => {\nlet x = 1 + 2\nother + x * 3\n}"
        val plan = RescriptInlinePlan.create(text, text.indexOf("let x"))!!
        assertEquals(listOf(text.indexOf("x *")), plan.usages)
        assertTrue(plan.removeDeclaration)
        assertNotNull(RescriptInlinePlan.create("let x = 1\nlet f = (other) => {other + x}", 4))
    }

    @Test
    fun `declaration comments are not moved but comment markers inside literals are safe`() {
        assertNull(RescriptInlinePlan.create("let x = 1 // keep this comment\nConsole.log(x)", 4))
        assertNull(RescriptInlinePlan.create("let x = 1 /* keep this comment */\nConsole.log(x)", 4))
        assertNotNull(RescriptInlinePlan.create("let x = \"// literal\"\nConsole.log(x)", 4))
    }

    @Test
    fun `parenthesized unary literal arithmetic remains available`() {
        val plan = RescriptInlinePlan.create("let x = -(1 + 2) * 3\nConsole.log(x)", 4)
        assertNotNull(plan)
        assertEquals("-(1 + 2) * 3", plan!!.value)
    }
}
