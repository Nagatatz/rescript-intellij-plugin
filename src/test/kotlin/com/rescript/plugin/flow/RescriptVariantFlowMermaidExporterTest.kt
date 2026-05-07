package com.rescript.plugin.flow

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Snapshots the Mermaid string produced for representative diagrams
 * so that any unintended formatting drift surfaces as a test failure
 * rather than a silent UI regression.
 */
class RescriptVariantFlowMermaidExporterTest {
    @Test
    fun `option pattern renders root with two arms`() {
        val diagram =
            FlowDiagram(
                scrutineeText = "x",
                arms =
                    listOf(
                        FlowNode("n0", "Some(_)", "v + 1", emptyList()),
                        FlowNode("n1", "None", "0", emptyList()),
                    ),
            )
        val expected =
            """
            flowchart TD
              root["switch x"]
              n0["v + 1"]
              root -->|"Some(_)"| n0
              n1["0"]
              root -->|"None"| n1

            """.trimIndent()
        assertEquals(expected, RescriptVariantFlowMermaidExporter.toMermaid(diagram))
    }

    @Test
    fun `nested switch arms are rendered as a sub-tree`() {
        val diagram =
            FlowDiagram(
                scrutineeText = "a",
                arms =
                    listOf(
                        FlowNode(
                            "n0",
                            "Some(_)",
                            "switch b {",
                            children =
                                listOf(
                                    FlowNode("n0_0", "Ok(_)", "1", emptyList()),
                                    FlowNode("n0_1", "Error(_)", "2", emptyList()),
                                ),
                        ),
                        FlowNode("n1", "None", "0", emptyList()),
                    ),
            )
        val out = RescriptVariantFlowMermaidExporter.toMermaid(diagram)
        // Outer edge from root to Some(_) arm
        assertTrue(out.contains("root -->|\"Some(_)\"| n0"))
        // Inner edges from the Some(_) arm to its children
        assertTrue(out.contains("n0 -->|\"Ok(_)\"| n0_0"))
        assertTrue(out.contains("n0 -->|\"Error(_)\"| n0_1"))
    }

    @Test
    fun `quote and backslash characters are escaped in labels`() {
        val diagram =
            FlowDiagram(
                scrutineeText = "x",
                arms = listOf(FlowNode("n0", "Quoted", "say \"hi\"", emptyList())),
            )
        val out = RescriptVariantFlowMermaidExporter.toMermaid(diagram)
        assertTrue(out.contains("&quot;hi&quot;"))
    }

    @Test
    fun `empty body preview falls back to pattern summary as node label`() {
        val diagram =
            FlowDiagram(
                scrutineeText = "x",
                arms = listOf(FlowNode("n0", "Loading", "", emptyList())),
            )
        val out = RescriptVariantFlowMermaidExporter.toMermaid(diagram)
        assertTrue(out.contains("n0[\"Loading\"]"))
    }
}
