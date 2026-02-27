package com.rescript.plugin.intention

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Tests for [RescriptExpandDestructuringIntention] expansion and parsing logic. */
class RescriptExpandDestructuringIntentionTest {
    @Test
    fun `expandDestructuring with simple fields`() {
        val result = RescriptExpandDestructuringIntention.expandDestructuring("let {name, age} = user")
        assertEquals(
            """
            let name = user.name
            let age = user.age
            """.trimIndent(),
            result,
        )
    }

    @Test
    fun `expandDestructuring preserves indentation`() {
        val result = RescriptExpandDestructuringIntention.expandDestructuring("  let {x, y} = point")
        assertEquals("  let x = point.x\n  let y = point.y", result)
    }

    @Test
    fun `expandDestructuring with alias fields`() {
        val result = RescriptExpandDestructuringIntention.expandDestructuring("let {name: n, age: a} = user")
        assertEquals(
            """
            let n = user.name
            let a = user.age
            """.trimIndent(),
            result,
        )
    }

    @Test
    fun `expandDestructuring with mixed simple and alias fields`() {
        val result = RescriptExpandDestructuringIntention.expandDestructuring("let {name, age: a, email} = user")
        assertEquals(
            """
            let name = user.name
            let a = user.age
            let email = user.email
            """.trimIndent(),
            result,
        )
    }

    @Test
    fun `expandDestructuring returns null for spread pattern`() {
        assertNull(RescriptExpandDestructuringIntention.expandDestructuring("let {name, ...rest} = user"))
    }

    @Test
    fun `expandDestructuring returns null for nested destructuring`() {
        assertNull(RescriptExpandDestructuringIntention.expandDestructuring("let {a: {b}} = x"))
    }

    @Test
    fun `expandDestructuring returns null for non-destructuring line`() {
        assertNull(RescriptExpandDestructuringIntention.expandDestructuring("let x = 42"))
    }

    @Test
    fun `expandDestructuring returns null for empty braces`() {
        assertNull(RescriptExpandDestructuringIntention.expandDestructuring("let {} = user"))
    }

    @Test
    fun `expandDestructuring with single field`() {
        val result = RescriptExpandDestructuringIntention.expandDestructuring("let {name} = user")
        assertEquals("let name = user.name", result)
    }

    @Test
    fun `expandDestructuring with complex expression`() {
        val result =
            RescriptExpandDestructuringIntention.expandDestructuring("let {x, y} = getPoint()")
        assertEquals(
            """
            let x = getPoint().x
            let y = getPoint().y
            """.trimIndent(),
            result,
        )
    }

    @Test
    fun `expandDestructuring with spaces around fields`() {
        val result =
            RescriptExpandDestructuringIntention.expandDestructuring("let { name , age } = user")
        assertEquals(
            """
            let name = user.name
            let age = user.age
            """.trimIndent(),
            result,
        )
    }

    // ── parseFields tests ──────────────────────────────────────────

    @Test
    fun `parseFields with simple fields`() {
        val fields = RescriptExpandDestructuringIntention.parseFields("name, age, email")
        assertEquals(
            listOf("name" to "name", "age" to "age", "email" to "email"),
            fields,
        )
    }

    @Test
    fun `parseFields with alias fields`() {
        val fields = RescriptExpandDestructuringIntention.parseFields("name: n, age: a")
        assertEquals(
            listOf("name" to "n", "age" to "a"),
            fields,
        )
    }

    @Test
    fun `parseFields with whitespace`() {
        val fields = RescriptExpandDestructuringIntention.parseFields(" name , age : a ")
        assertEquals(
            listOf("name" to "name", "age" to "a"),
            fields,
        )
    }

    @Test
    fun `parseFields with empty string`() {
        val fields = RescriptExpandDestructuringIntention.parseFields("")
        assertEquals(emptyList<Pair<String, String>>(), fields)
    }

    @Test
    fun `parseFields with trailing comma`() {
        val fields = RescriptExpandDestructuringIntention.parseFields("name, age,")
        assertEquals(
            listOf("name" to "name", "age" to "age"),
            fields,
        )
    }
}
