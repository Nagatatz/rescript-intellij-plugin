package com.rescript.plugin.flow

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Snapshots the DOT string produced for representative diagrams to
 * catch unintended formatting drift in the export feature.
 */
class RescriptVariantFlowDotExporterTest {
    @Test
    fun `option pattern renders a simple digraph`() {
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
            digraph SwitchFlow {
              rankdir=TB;
              root [label="switch x"];
              n0 [label="v + 1"];
              root -> n0 [label="Some(_)"];
              n1 [label="0"];
              root -> n1 [label="None"];
            }

            """.trimIndent()
        assertEquals(expected, RescriptVariantFlowDotExporter.toDot(diagram))
    }

    @Test
    fun `nested arms produce nested edges`() {
        val diagram =
            FlowDiagram(
                scrutineeText = "a",
                arms =
                    listOf(
                        FlowNode(
                            "n0",
                            "Some(_)",
                            "switch b",
                            listOf(
                                FlowNode("n0_0", "Ok(_)", "1", emptyList()),
                            ),
                        ),
                    ),
            )
        val out = RescriptVariantFlowDotExporter.toDot(diagram)
        assertTrue(out.contains("root -> n0 [label=\"Some(_)\"]"))
        assertTrue(out.contains("n0 -> n0_0 [label=\"Ok(_)\"]"))
    }

    @Test
    fun `quotes newlines and backslashes are escaped`() {
        val diagram =
            FlowDiagram(
                scrutineeText = "x",
                arms = listOf(FlowNode("n0", "P", "say \"hi\"\nnext", emptyList())),
            )
        val out = RescriptVariantFlowDotExporter.toDot(diagram)
        assertTrue(out.contains("\\\"hi\\\""))
        assertTrue(out.contains("\\n"))
    }
}
