package com.rescript.plugin.binding

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [DtsGenerateBindingAction]'s companion utility functions.
 *
 * Tests the output file name computation logic that strips the `.d` suffix
 * from TypeScript definition file names. The IDE-dependent action workflow
 * (Node.js detection, progress UI, file writing) is not tested here.
 *
 * @see DtsGenerateBindingAction
 */
class DtsGenerateBindingActionTest {
    // ── computeOutputName ────────────────────────────────────────────────

    @Test
    fun testComputeOutputNameWithDotDSuffix() {
        // "lodash.d" (from "lodash.d.ts") → "lodash"
        assertEquals("lodash", DtsGenerateBindingAction.computeOutputName("lodash.d"))
    }

    @Test
    fun testComputeOutputNameWithoutDotDSuffix() {
        // "react" (from "react.ts") → "react"
        assertEquals("react", DtsGenerateBindingAction.computeOutputName("react"))
    }

    @Test
    fun testComputeOutputNameScopedPackage() {
        // "@types/node.d" → "@types/node"
        assertEquals("@types/node", DtsGenerateBindingAction.computeOutputName("@types/node.d"))
    }

    @Test
    fun testComputeOutputNameWithMultipleDots() {
        // "express.serve-static.d" → "express.serve-static"
        assertEquals(
            "express.serve-static",
            DtsGenerateBindingAction.computeOutputName("express.serve-static.d"),
        )
    }

    @Test
    fun testComputeOutputNameJustD() {
        // "d" alone should not be stripped (endsWith(".d") is false for "d")
        assertEquals("d", DtsGenerateBindingAction.computeOutputName("d"))
    }

    @Test
    fun testComputeOutputNameDotDOnly() {
        // ".d" → "" (the .d suffix is stripped entirely)
        assertEquals("", DtsGenerateBindingAction.computeOutputName(".d"))
    }

    @Test
    fun testComputeOutputNameEmpty() {
        assertEquals("", DtsGenerateBindingAction.computeOutputName(""))
    }

    @Test
    fun testComputeOutputNameEndsWithUppercaseD() {
        // "myModule.D" should NOT be stripped (case-sensitive check for ".d")
        assertEquals("myModule.D", DtsGenerateBindingAction.computeOutputName("myModule.D"))
    }
}
