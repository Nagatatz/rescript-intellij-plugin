package com.rescript.plugin.cli

import com.rescript.plugin.flow.FlowDiagram
import com.rescript.plugin.flow.FlowNode
import com.rescript.plugin.flow.RescriptVariantFlowMermaidExporter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files
import java.util.concurrent.TimeUnit

/**
 * Verifies that [RescriptVariantFlowMermaidExporter.toMermaid]
 * produces source that the Mermaid CLI (`mmdc`) can render to SVG.
 *
 * Skipped automatically when `mmdc` is not on PATH (probed by
 * [ExternalCliAvailability]). CI installs the CLI in the build job
 * so the gate runs there; developer machines without `mmdc` see
 * the test reported as skipped.
 */
class RescriptVariantFlowMermaidExporterCliTest {
    @BeforeEach
    fun assumeCliPresent() {
        Assumptions.assumeTrue(
            ExternalCliAvailability.isMermaidCliAvailable(),
            "mmdc not on PATH; skipping Mermaid CLI verification",
        )
    }

    @Test
    fun `option pattern produces Mermaid that mmdc can render`() {
        val diagram =
            FlowDiagram(
                scrutineeText = "x",
                arms =
                    listOf(
                        FlowNode("n0", "Some(_)", "v + 1", emptyList()),
                        FlowNode("n1", "None", "0", emptyList()),
                    ),
            )
        val mermaid = RescriptVariantFlowMermaidExporter.toMermaid(diagram)
        val tmpDir = Files.createTempDirectory("mermaid-cli-test").toFile()
        try {
            val input = File(tmpDir, "diagram.mmd").also { it.writeText(mermaid) }
            val output = File(tmpDir, "diagram.svg")
            // Ubuntu 23.10+ runners (used by GitHub Actions) disable
            // unprivileged user namespaces, so Puppeteer's bundled Chromium
            // crashes with "No usable sandbox!" unless we pass --no-sandbox.
            // Locally this is a no-op since the flag is permitted regardless.
            val puppeteerConfig =
                File(tmpDir, "puppeteer.json")
                    .also { it.writeText("""{"args": ["--no-sandbox"]}""") }
            val process =
                ProcessBuilder(
                    "mmdc",
                    "-i",
                    input.path,
                    "-o",
                    output.path,
                    "-p",
                    puppeteerConfig.path,
                ).redirectErrorStream(true)
                    .start()
            val finished = process.waitFor(60, TimeUnit.SECONDS)
            if (!finished) {
                process.destroyForcibly()
                throw AssertionError("mmdc timed out after 60s")
            }
            val log = process.inputStream.bufferedReader().readText()
            assertEquals(0, process.exitValue(), "mmdc failed: $log")
            assertTrue(output.exists() && output.length() > 0, "expected non-empty SVG at ${output.path}")
        } finally {
            tmpDir.deleteRecursively()
        }
    }
}
