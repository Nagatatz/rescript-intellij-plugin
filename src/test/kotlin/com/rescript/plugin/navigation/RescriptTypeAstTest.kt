package com.rescript.plugin.navigation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

/**
 * Shape tests for the [RescriptTypeAst] sealed hierarchy. Each node is
 * checked for property retention and data-class equality. `UnitT` is also
 * checked to be a singleton (it is declared as an `object`).
 */
class RescriptTypeAstTest {
    @Test
    fun `UnitT is a singleton`() {
        assertSame(RescriptTypeAst.UnitT, RescriptTypeAst.UnitT)
    }

    @Test
    fun `Ctor retains its name and supports equality`() {
        val a = RescriptTypeAst.Ctor("int")
        val b = RescriptTypeAst.Ctor("int")
        assertEquals("int", a.name)
        assertEquals(a, b)
        assertNotEquals(a, RescriptTypeAst.Ctor("string"))
    }

    @Test
    fun `TypeVar retains its name and supports equality`() {
        val a = RescriptTypeAst.TypeVar("a")
        val b = RescriptTypeAst.TypeVar("a")
        assertEquals("a", a.name)
        assertEquals(a, b)
        assertNotEquals(a, RescriptTypeAst.TypeVar("b"))
    }

    @Test
    fun `App retains ctor name and arg list with nested equality`() {
        val inner = RescriptTypeAst.Ctor("int")
        val outer = RescriptTypeAst.App("option", listOf(inner))
        val sameAgain = RescriptTypeAst.App("option", listOf(RescriptTypeAst.Ctor("int")))
        assertEquals("option", outer.ctor)
        assertEquals(listOf(inner), outer.args)
        assertEquals(outer, sameAgain)
        assertNotEquals(outer, RescriptTypeAst.App("option", listOf(RescriptTypeAst.Ctor("string"))))
    }

    @Test
    fun `Tuple retains its elements`() {
        val t = RescriptTypeAst.Tuple(listOf(RescriptTypeAst.Ctor("int"), RescriptTypeAst.Ctor("string")))
        assertEquals(2, t.elements.size)
        assertEquals(RescriptTypeAst.Ctor("int"), t.elements[0])
        assertEquals(RescriptTypeAst.Ctor("string"), t.elements[1])
    }

    @Test
    fun `Arrow retains from and to with structural equality`() {
        val arr1 =
            RescriptTypeAst.Arrow(
                from = RescriptTypeAst.Ctor("int"),
                to = RescriptTypeAst.Ctor("string"),
            )
        val arr2 =
            RescriptTypeAst.Arrow(
                from = RescriptTypeAst.Ctor("int"),
                to = RescriptTypeAst.Ctor("string"),
            )
        val flipped =
            RescriptTypeAst.Arrow(
                from = RescriptTypeAst.Ctor("string"),
                to = RescriptTypeAst.Ctor("int"),
            )
        assertEquals(arr1, arr2)
        assertNotEquals(arr1, flipped)
    }

    @Test
    fun `ReturnQuery wraps a target type`() {
        val target = RescriptTypeAst.App("option", listOf(RescriptTypeAst.TypeVar("a")))
        val q = RescriptTypeAst.ReturnQuery(target)
        assertEquals(target, q.target)
        assertEquals(q, RescriptTypeAst.ReturnQuery(target))
    }
}
