package com.rescript.plugin.inspection

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptRefRemovalPlannerTest {
    @Test
    fun `local scalar initializer and all contents reads change together`() {
        val before = "let run = () => {\nlet count = ref(0)\nlet value = count.contents + 1\ncount.contents + value\n}"
        val after = "let run = () => {\nlet count = 0\nlet value = count + 1\ncount + value\n}"
        assertEquals(after, replacement(before))
    }

    @Test
    fun `scalar value arguments preserve the value rather than passing a box`() {
        val before = "let run = () => {\nlet count = ref(2)\nInt.toString(count.contents)\n}"
        val after = "let run = () => {\nlet count = 2\nInt.toString(count)\n}"
        assertEquals(after, replacement(before))
    }

    @Test
    fun `scalar literals retain their source spelling`() {
        listOf("false", "-2", "1.5", "\"hello\"", "'a'", "(())", "((0))").forEach { literal ->
            val before = "let run = () => {\nlet value = ref($literal)\nvalue.contents\n}"
            val after = "let run = () => {\nlet value = $literal\nvalue\n}"
            assertEquals(after, replacement(before), literal)
        }
    }

    @Test
    fun `multiline comments and string contents are retained as source`() {
        val before =
            """
            let run = () => {
              let name = ref(/* outer /* nested */ */
                "name.contents := ref(0)"
              )
              name /* read */ . /* field */ contents
            }
            """.trimIndent()
        val result = replacement(before)!!
        assertTrue(result.contains("\"name.contents := ref(0)\""))
        assertTrue(result.contains("let name = /* outer /* nested */ */"))
        assertTrue(result.contains("name /* read */  /* field */ "))
    }

    @Test
    fun `snapshot changes anywhere reject the prepared edit`() {
        val before = "let run = () => {\nlet count = ref(0)\ncount.contents\n}"
        val plan = RescriptRefRemovalPlanner.plans(before).single()
        assertNull(plan.replacementFor(before + "\n// changed elsewhere"))
        assertNull(plan.replacementFor(before.replace("0", "1")))
        val after = "let run = () => {\nlet count = 0\ncount\n}"
        assertEquals(after, plan.replacementFor(before))
    }

    @Test
    fun `exported and module ref boxes never change their public types`() {
        refused(
            "let count = ref(0)\nlet value = count.contents",
            "module Counter = {\nlet count = ref(0)\nlet value = count.contents\n}",
            "@genType\nlet count = ref(0)",
        )
    }

    @Test
    fun `both mutation syntaxes and parenthesized writes are refused`() {
        listOf("count := 1", "count.contents = 1", "(count.contents) = 1", "count.contents += 1").forEach {
            refused(local("$it\ncount.contents"))
        }
    }

    @Test
    fun `aliases box arguments and returned boxes are refused`() {
        listOf("let alias = count", "consume(count)", "count", "(count)").forEach {
            refused(local(it))
        }
    }

    @Test
    fun `closure capture including unbraced lambdas is refused`() {
        listOf(
            "let get = () => count.contents\nget()",
            "let get = () => { count.contents }\nget()",
            "Array.map([1], x => count.contents + x)",
        ).forEach { refused(local(it)) }
    }

    @Test
    fun `shadowing and ambiguous binding identities are refused`() {
        refused(
            local("let count = ref(1)\ncount.contents"),
            "let run = count => {\nlet count = ref(0)\ncount.contents\n}",
            local("{\nlet count = 1\ncount\n}\ncount.contents"),
            local("if true { count.contents } else { 0 }"),
        )
    }

    @Test
    fun `constructor shadowing and open or include prevent the proof`() {
        refused(
            "let ref = x => x\n" + local("count.contents"),
            "let ref(x) = x\n" + local("count.contents"),
            "open Custom\n" + local("count.contents"),
            "include Custom\n" + local("count.contents"),
            "let f = (ref) => {\nlet count = ref(0)\ncount.contents\n}",
        )
    }

    @Test
    fun `value restriction sensitive and unknown initializers are refused`() {
        listOf("x => x", "(x) => { x }", "A.identity", "Some(0)", "[]", "[...xs]", "getItems()", "a b").forEach {
            refused("let run = () => {\nlet count = ref($it)\ncount.contents\n}")
        }
    }

    @Test
    fun `opaque interop and binding attributes cannot hide box escapes`() {
        refused(
            local("let _ = %raw(\"count.contents = 1\")\ncount.contents"),
            "let run = () => {\n@custom\nlet count = ref(0)\ncount.contents\n}",
            "@val external eval: string => unit = \"eval\"\n" +
                local("let _ = eval(\"count.contents = 1\")\ncount.contents"),
            "let _ = %raw(\"var hidden = 1\")\n" + local("count.contents"),
        )
    }

    @Test
    fun `broken lexical input and unknown read boundaries are refused`() {
        refused(
            local("count.contents a b"),
            local("count.contents") + "\n/* unfinished",
            local("count.contents") + "\nlet text = \"unfinished",
            "let run = () => {\nlet count = ref(0]\ncount.contents\n}",
            "let run = a b => {\nlet count = ref(0)\ncount.contents\n}",
            "let run = () => {\nlet count = ref(0)\n+ 1\ncount.contents\n}",
        )
    }

    private fun local(body: String): String = "let run = () => {\nlet count = ref(0)\n$body\n}"

    private fun replacement(text: String): String? =
        RescriptRefRemovalPlanner.plans(text).singleOrNull()?.replacementFor(text)

    private fun refused(vararg sources: String) {
        sources.forEach { assertTrue(RescriptRefRemovalPlanner.plans(it).isEmpty(), it) }
    }
}
