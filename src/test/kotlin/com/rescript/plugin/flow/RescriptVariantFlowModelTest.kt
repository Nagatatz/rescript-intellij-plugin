package com.rescript.plugin.flow

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Drives the pure-function decision tree builder through the same
 * pattern shapes covered by the switch arm collector, plus nested
 * switches and offset-resolution edge cases.
 */
class RescriptVariantFlowModelTest {
    private fun offsetOf(
        source: String,
        marker: String,
    ): Int = source.indexOf(marker).also { require(it >= 0) { "marker '$marker' not found" } }

    @Test
    fun `option pattern produces two arms with body previews`() {
        val source =
            """
            let f = x =>
              switch x {
              | Some(v) => v + 1
              | None => 0
              }
            """.trimIndent()
        val diagram = RescriptVariantFlowModel.buildAtOffset(source, offsetOf(source, "switch"))
        assertNotNull(diagram)
        assertEquals("x", diagram!!.scrutineeText)
        assertEquals(2, diagram.arms.size)
        assertEquals("Some(_)", diagram.arms[0].patternSummary)
        assertEquals("v + 1", diagram.arms[0].bodyPreview)
        assertEquals("None", diagram.arms[1].patternSummary)
        assertEquals("0", diagram.arms[1].bodyPreview)
        assertEquals(emptyList<FlowNode>(), diagram.arms[0].children)
    }

    @Test
    fun `result pattern carries Ok and Error arms`() {
        val source =
            """
            let g = r =>
              switch r {
              | Ok(v) => v
              | Error(_) => 0
              }
            """.trimIndent()
        val diagram = RescriptVariantFlowModel.buildAtOffset(source, offsetOf(source, "switch"))!!
        assertEquals(listOf("Ok(_)", "Error(_)"), diagram.arms.map { it.patternSummary })
    }

    @Test
    fun `custom variant arms keep their pattern summaries`() {
        val source =
            """
            let c = s =>
              switch s {
              | Loading => 0
              | Loaded(_) => 1
              | Failed(_) => -1
              }
            """.trimIndent()
        val diagram = RescriptVariantFlowModel.buildAtOffset(source, offsetOf(source, "switch"))!!
        assertEquals(listOf("Loading", "Loaded(_)", "Failed(_)"), diagram.arms.map { it.patternSummary })
    }

    @Test
    fun `nested switch becomes a sub-graph`() {
        val source =
            """
            let n = (a, b) =>
              switch a {
              | Some(_) =>
                switch b {
                | Ok(_) => 1
                | Error(_) => 2
                }
              | None => 0
              }
            """.trimIndent()
        val diagram = RescriptVariantFlowModel.buildAtOffset(source, offsetOf(source, "switch a"))!!
        val someArm = diagram.arms.first { it.patternSummary == "Some(_)" }
        assertEquals(2, someArm.children.size)
        assertEquals(setOf("Ok(_)", "Error(_)"), someArm.children.map { it.patternSummary }.toSet())
        // The None arm has no nested switch.
        val noneArm = diagram.arms.first { it.patternSummary == "None" }
        assertEquals(emptyList<FlowNode>(), noneArm.children)
    }

    @Test
    fun `offset inside a nested switch picks the inner one`() {
        val source =
            """
            let n = (a, b) =>
              switch a {
              | Some(_) =>
                switch b {
                | Ok(_) => 1
                | Error(_) => 2
                }
              | None => 0
              }
            """.trimIndent()
        val innerOffset = source.indexOf("Ok(_)")
        val diagram = RescriptVariantFlowModel.buildAtOffset(source, innerOffset)!!
        assertEquals("b", diagram.scrutineeText)
        assertEquals(listOf("Ok(_)", "Error(_)"), diagram.arms.map { it.patternSummary })
    }

    @Test
    fun `offset outside any switch returns null`() {
        val source = "let a = 1\nlet b = 2\n"
        assertNull(RescriptVariantFlowModel.buildAtOffset(source, 0))
    }

    @Test
    fun `body preview truncates long single-line bodies`() {
        val long = "x".repeat(80)
        val source =
            """
            let f = x =>
              switch x {
              | _ => $long
              }
            """.trimIndent()
        val diagram = RescriptVariantFlowModel.buildAtOffset(source, offsetOf(source, "switch"))!!
        val preview = diagram.arms.first().bodyPreview
        assertEquals(40, preview.length)
        assertTrue(preview.endsWith("…"))
    }

    @Test
    fun `deeply nested switches are truncated past the depth cap`() {
        // 5-level nested switch: depth 0 (outer) → depth 4 (innermost)
        val source =
            buildString {
                appendLine("let f = a =>")
                appendLine("  switch a {")
                appendLine("  | _ =>")
                appendLine("    switch a {")
                appendLine("    | _ =>")
                appendLine("      switch a {")
                appendLine("      | _ =>")
                appendLine("        switch a {")
                appendLine("        | _ =>")
                appendLine("          switch a {")
                appendLine("          | _ => 0")
                appendLine("          }")
                appendLine("        }")
                appendLine("      }")
                appendLine("    }")
                appendLine("  }")
            }
        val diagram = RescriptVariantFlowModel.buildAtOffset(source, source.indexOf("switch a"))!!
        // Walk down the chain. depth 0,1,2,3 keep their child lists,
        // depth 4 must be the single "(deeper switch hidden)" leaf.
        var current = diagram.arms.single()
        repeat(RescriptVariantFlowModel.MAX_NESTING_DEPTH) {
            assertEquals(1, current.children.size, "depth $it should still expand")
            current = current.children.single()
        }
        // current is now the truncation marker
        assertEquals(1, current.children.size)
        assertEquals("(deeper switch hidden)", current.children.single().patternSummary)
    }

    @Test
    fun `tuple scrutinee produces tuple pattern summaries`() {
        val source =
            """
            let render = (info, error) =>
              switch (info, error) {
              | (Some(i), _) => i.name
              | (None, Some(message)) => message
              | (None, None) => ""
              }
            """.trimIndent()
        val diagram = RescriptVariantFlowModel.buildAtOffset(source, offsetOf(source, "switch"))!!
        assertEquals(
            listOf("(Some(_), _)", "(None, Some(_))", "(None, None)"),
            diagram.arms.map { it.patternSummary },
        )
    }

    @Test
    fun `classifyArm returns CONSTRUCTOR for uppercase variant pattern`() {
        assertEquals(
            ArmKind.CONSTRUCTOR,
            RescriptVariantFlowModel.classifyArm("Some(_)", "v + 1", hasChildren = false),
        )
    }

    @Test
    fun `classifyArm returns WILDCARD for bare underscore`() {
        assertEquals(
            ArmKind.WILDCARD,
            RescriptVariantFlowModel.classifyArm("_", "0", hasChildren = false),
        )
    }

    @Test
    fun `classifyArm returns PATTERN_BINDING for lowercase identifier pattern`() {
        assertEquals(
            ArmKind.PATTERN_BINDING,
            RescriptVariantFlowModel.classifyArm("other", "other + 1", hasChildren = false),
        )
    }

    @Test
    fun `classifyArm returns TODO_PLACEHOLDER for todo body`() {
        assertEquals(
            ArmKind.TODO_PLACEHOLDER,
            RescriptVariantFlowModel.classifyArm("Loaded(_)", "todo", hasChildren = false),
        )
    }

    @Test
    fun `classifyArm rejects todoItem identifier as TODO_PLACEHOLDER`() {
        // bodyPreview "todoItem" must not match — `startsWith("todo")` is too
        // loose and would catch real identifiers. Only `todo` exactly and
        // `todo` followed by a space (e.g. `todo // remember`) qualify.
        assertEquals(
            ArmKind.CONSTRUCTOR,
            RescriptVariantFlowModel.classifyArm("Some(_)", "todoItem", hasChildren = false),
        )
    }

    @Test
    fun `classifyArm returns NESTED_SWITCH when arm has children`() {
        assertEquals(
            ArmKind.NESTED_SWITCH,
            RescriptVariantFlowModel.classifyArm("Some(_)", "switch v {", hasChildren = true),
        )
    }

    @Test
    fun `classifyArm precedence puts NESTED_SWITCH above WILDCARD and TODO_PLACEHOLDER`() {
        // hasChildren wins over the rest, so a `_` pattern that nests a switch
        // is NESTED_SWITCH rather than WILDCARD.
        assertEquals(
            ArmKind.NESTED_SWITCH,
            RescriptVariantFlowModel.classifyArm("_", "todo", hasChildren = true),
        )
    }

    @Test
    fun `buildAtOffset populates kind on resulting FlowNode`() {
        val source =
            """
            let f = x =>
              switch x {
              | Some(_) => 1
              | None => todo
              }
            """.trimIndent()
        val diagram = RescriptVariantFlowModel.buildAtOffset(source, offsetOf(source, "switch"))!!
        assertEquals(ArmKind.CONSTRUCTOR, diagram.arms[0].kind)
        assertEquals(ArmKind.TODO_PLACEHOLDER, diagram.arms[1].kind)
    }
}
