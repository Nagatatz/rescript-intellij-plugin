package com.rescript.plugin.cli

import com.rescript.plugin.flow.FlowDiagram
import com.rescript.plugin.flow.FlowNode
import com.rescript.plugin.flow.RescriptVariantFlowDotExporter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files
import java.util.concurrent.TimeUnit

/**
 * Verifies that [RescriptVariantFlowDotExporter.toDot] produces
 * graphviz DOT that the `dot` binary can render to SVG.
 *
 * Skipped automatically when `dot` is not on PATH (probed by
 * [ExternalCliAvailability]). CI installs `graphviz` in the build
 * job so the gate runs there; developer machines without `dot` see
 * the test reported as skipped.
 */
class RescriptVariantFlowDotExporterCliTest {
    @BeforeEach
    fun assumeCliPresent() {
        Assumptions.assumeTrue(
            ExternalCliAvailability.isDotAvailable(),
            "graphviz dot not on PATH; skipping DOT CLI verification",
        )
    }

    @Test
    fun `nested arms produce DOT that graphviz can render`() {
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
                                FlowNode("n0_1", "Error(_)", "2", emptyList()),
                            ),
                        ),
                        FlowNode("n1", "None", "0", emptyList()),
                    ),
            )
        val dot = RescriptVariantFlowDotExporter.toDot(diagram)
        val tmpDir = Files.createTempDirectory("dot-cli-test").toFile()
        try {
            val input = File(tmpDir, "diagram.dot").also { it.writeText(dot) }
            val output = File(tmpDir, "diagram.svg")
            val process =
                ProcessBuilder("dot", "-Tsvg", "-o", output.path, input.path)
                    .redirectErrorStream(true)
                    .start()
            val finished = process.waitFor(30, TimeUnit.SECONDS)
            if (!finished) {
                process.destroyForcibly()
                throw AssertionError("dot timed out after 30s")
            }
            val log = process.inputStream.bufferedReader().readText()
            assertEquals(0, process.exitValue(), "dot failed: $log")
            assertTrue(output.exists() && output.length() > 0, "expected non-empty SVG at ${output.path}")
        } finally {
            tmpDir.deleteRecursively()
        }
    }
}
