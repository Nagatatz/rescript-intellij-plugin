package com.rescript.plugin.interop

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/**
 * Validates the line-based heuristic in [RescriptInteropClassifier]
 * against the four interop categories tracked by the Risk Map.
 */
class RescriptInteropClassifierTest {
    @Test
    fun `Obj_magic anywhere in the line is high risk`() {
        assertEquals(
            InteropKind.OBJ_MAGIC to RiskLevel.HIGH,
            RescriptInteropClassifier.classify("let x = Obj.magic(payload)"),
        )
    }

    @Test
    fun `%raw at line start is high risk`() {
        assertEquals(
            InteropKind.RAW to RiskLevel.HIGH,
            RescriptInteropClassifier.classify("%raw(\"console.log('hi')\")"),
        )
    }

    @Test
    fun `external with bs decorator is medium risk`() {
        val line = "@bs.send external setTitle: (window, string) => unit = \"setTitle\""
        assertEquals(InteropKind.EXTERNAL to RiskLevel.MEDIUM, RescriptInteropClassifier.classify(line))
    }

    @Test
    fun `plain external is low risk`() {
        val line = "external alert: string => unit = \"alert\""
        assertEquals(InteropKind.EXTERNAL to RiskLevel.LOW, RescriptInteropClassifier.classify(line))
    }

    @Test
    fun `bs_attr alone is low risk`() {
        assertEquals(InteropKind.BS_ATTR to RiskLevel.LOW, RescriptInteropClassifier.classify("@bs.module(\"chalk\")"))
    }

    @Test
    fun `unrelated line returns null`() {
        assertNull(RescriptInteropClassifier.classify("let answer = 42"))
    }

    @Test
    fun `leading whitespace is tolerated`() {
        assertEquals(
            InteropKind.OBJ_MAGIC to RiskLevel.HIGH,
            RescriptInteropClassifier.classify("    let n = Obj.magic(x)"),
        )
    }

    @Test
    fun `%%raw is also high risk`() {
        assertEquals(
            InteropKind.RAW to RiskLevel.HIGH,
            RescriptInteropClassifier.classify("%%raw(`function add(a,b) { return a + b; }`)"),
        )
    }
}
