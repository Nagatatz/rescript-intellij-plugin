package com.rescript.plugin.intention

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Tests for [RescriptNestedSwitchFlattener], the pure-syntax analyzer that
 * computes the [FlattenPlan] folding a nested `switch` into its outer arm.
 */
class RescriptNestedSwitchFlattenerTest {
    /** Returns the offset of the first occurrence of [marker] in [source]. */
    private fun caretAt(
        source: String,
        marker: String,
    ): Int = source.indexOf(marker)

    @Test
    fun `flattens an option nest into the outer Some pattern`() {
        val source =
            """
            let f = x =>
              switch x {
              | Some(y) => switch y {
                | Some(z) => handleZ(z)
                | None => fallback
                }
              | None => none
              }
            """.trimIndent()
        val plan = RescriptNestedSwitchFlattener.plan(source, caretAt(source, "Some(y)"))!!
        assertEquals(2, plan.armCount)
        assertEquals(
            "| Some(Some(z)) => handleZ(z)\n  | Some(None) => fallback",
            plan.replacementText,
        )
    }

    @Test
    fun `flattens a result nest into the outer Ok pattern`() {
        val source =
            """
            switch x {
            | Ok(v) => switch v {
              | Ok(n) => n
              | Error(e) => 0
              }
            | Error(e) => -1
            }
            """.trimIndent()
        val plan = RescriptNestedSwitchFlattener.plan(source, caretAt(source, "Ok(v)"))!!
        assertEquals(2, plan.armCount)
        assertEquals(
            "| Ok(Ok(n)) => n\n| Ok(Error(e)) => 0",
            plan.replacementText,
        )
    }

    @Test
    fun `flattens a bare binding into the inner patterns verbatim`() {
        val source =
            """
            switch x {
            | y => switch y {
              | Some(z) => a
              | None => b
              }
            }
            """.trimIndent()
        val plan = RescriptNestedSwitchFlattener.plan(source, caretAt(source, "y =>"))!!
        assertEquals(2, plan.armCount)
        assertEquals(
            "| Some(z) => a\n| None => b",
            plan.replacementText,
        )
    }

    @Test
    fun `rejects an arm that introduces no binding`() {
        val source =
            """
            switch x {
            | None => switch y {
              | Some(z) => a
              | None => b
              }
            | Some(v) => other
            }
            """.trimIndent()
        assertNull(RescriptNestedSwitchFlattener.plan(source, caretAt(source, "| None =>") + 2))
    }

    @Test
    fun `rejects an arm that introduces two bindings`() {
        val source =
            """
            switch x {
            | Loaded(a, b) => switch a {
              | Some(z) => z
              | None => 0
              }
            | Loading => -1
            }
            """.trimIndent()
        assertNull(RescriptNestedSwitchFlattener.plan(source, caretAt(source, "Loaded")))
    }

    @Test
    fun `rejects an inner scrutinee that is not the outer binding`() {
        val source =
            """
            switch x {
            | Some(y) => switch f(y) {
              | Some(z) => a
              | None => b
              }
            | None => c
            }
            """.trimIndent()
        assertNull(RescriptNestedSwitchFlattener.plan(source, caretAt(source, "Some(y)")))
    }

    @Test
    fun `rejects an inner arm that uses an or-pattern`() {
        val source =
            """
            switch x {
            | Some(y) => switch y {
              | Some(z) | Other(z) => a
              | None => b
              }
            | None => c
            }
            """.trimIndent()
        assertNull(RescriptNestedSwitchFlattener.plan(source, caretAt(source, "Some(y)")))
    }

    @Test
    fun `rejects an inner arm that has a when guard`() {
        val source =
            """
            switch x {
            | Some(y) => switch y {
              | Some(z) when z > 0 => a
              | None => b
              }
            | None => c
            }
            """.trimIndent()
        assertNull(RescriptNestedSwitchFlattener.plan(source, caretAt(source, "Some(y)")))
    }

    @Test
    fun `rejects an outer arm that has a when guard`() {
        val source =
            """
            switch x {
            | Some(y) when y > 0 => switch y {
              | Some(z) => a
              | None => b
              }
            | None => c
            }
            """.trimIndent()
        assertNull(RescriptNestedSwitchFlattener.plan(source, caretAt(source, "Some(y)")))
    }

    @Test
    fun `rejects a body that mixes other expressions with the switch`() {
        val source =
            """
            switch x {
            | Some(y) => {
                let r = compute()
                switch y {
                | Some(z) => a
                | None => b
                }
              }
            | None => c
            }
            """.trimIndent()
        assertNull(RescriptNestedSwitchFlattener.plan(source, caretAt(source, "Some(y)")))
    }

    @Test
    fun `preserves the outer arm indentation on continuation lines`() {
        val source =
            """
            let f = x =>
              switch x {
                  | Some(y) => switch y {
                    | Some(z) => a
                    | None => b
                    }
                  | None => c
                  }
            """.trimIndent()
        val plan = RescriptNestedSwitchFlattener.plan(source, caretAt(source, "Some(y)"))!!
        // The outer arm `|` sits at column 6, so continuation lines carry six spaces.
        assertEquals(
            "| Some(Some(z)) => a\n      | Some(None) => b",
            plan.replacementText,
        )
    }

    @Test
    fun `spans the outer arm from its pipe through the inner switch close brace`() {
        val source =
            """
            switch x {
            | Some(y) => switch y {
              | Some(z) => a
              | None => b
              }
            | None => c
            }
            """.trimIndent()
        val plan = RescriptNestedSwitchFlattener.plan(source, caretAt(source, "Some(y)"))!!
        assertEquals(source.indexOf("| Some(y)"), plan.replaceStart)
        val replaced = source.substring(plan.replaceStart, plan.replaceEnd)
        assertTrue(replaced.startsWith("| Some(y) =>"), "span should start at the outer pipe")
        assertTrue(replaced.endsWith("}"), "span should end at the inner switch close brace")
        // The trailing outer arm must remain outside the replaced range.
        assertTrue(!replaced.contains("| None => c"), "span must not swallow the next outer arm")
    }
}
