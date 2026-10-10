package com.rescript.plugin.repl

import com.rescript.plugin.wizard.IntegrationTestSupport
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

/** Exercises the public REPL execution path with an actual compiler and runtime. */
class RescriptReplIntegrationTest {
    @TempDir
    lateinit var projectDir: Path

    @Test
    fun `real compiler evaluates and preserves existing files through syntax failure`() {
        IntegrationTestSupport.requireBinary("npm")
        IntegrationTestSupport.requireBinary("node")
        Files.writeString(
            projectDir.resolve("package.json"),
            """{"name":"repl-regression","type":"module","private":true,"devDependencies":{"rescript":"12.2.0"}}""",
        )
        Files.writeString(
            projectDir.resolve("rescript.json"),
            """{"name":"repl-regression","sources":[{"dir":"src"}],"package-specs":{"module":"esmodule","in-source":true},"suffix":".res.js"}""",
        )
        val src = Files.createDirectory(projectDir.resolve("src"))
        val source = Files.writeString(src.resolve("RescriptRepl__Eval.res"), "let sentinel = 7\n")
        val install =
            IntegrationTestSupport.exec(projectDir, listOf("npm", "install", "--no-audit", "--no-fund"))
        assertTrue(install.succeeded, install.stdout + install.stderr)

        // Js.log produces a deprecation warning in 12.2.0; successful builds must still run.
        assertEquals("42", RescriptReplExecutor.execute("42", projectDir.toString()))
        val output = src.resolve("RescriptRepl__Eval.res.js")
        val compiledSentinel = Files.readString(output)
        val map = Files.writeString(src.resolve("RescriptRepl__Eval.res.js.map"), "original map")

        assertTrue(RescriptReplExecutor.execute("let =", projectDir.toString()).startsWith("Compile error"))
        assertEquals("let sentinel = 7\n", Files.readString(source))
        assertEquals(compiledSentinel, Files.readString(output))
        assertEquals("original map", Files.readString(map))
        Files.list(src).use { paths ->
            assertTrue(paths.noneMatch { it.fileName.toString().startsWith("RescriptRepl__Eval_") })
        }
    }
}
