package com.rescript.plugin.refactor

import com.intellij.openapi.project.Project
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.rescript.plugin.IntelliJPlatformExtension
import com.rescript.plugin.wizard.IntegrationTestSupport
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

/** Compiles and executes source before and after applying the actual editor handler. */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptInlineIntegrationTest {
    private lateinit var myFixture: CodeInsightTestFixture
    private lateinit var project: Project

    @TempDir
    lateinit var projectDir: Path

    @Test
    fun `editor transformation compiles and preserves runtime output`() {
        IntegrationTestSupport.requireBinary("npm")
        IntegrationTestSupport.requireBinary("node")
        myFixture.configureByText(
            "Example.res",
            "let compute = () => {\n  let <caret>x = 1 + 2\n  x * 3\n}\nConsole.log(compute())",
        )
        val original = myFixture.editor.document.text
        val element = myFixture.file.findElementAt(myFixture.editor.caretModel.offset)!!
        val handler = RescriptInlineHandler()
        assertTrue(handler.canInlineElement(element))
        handler.inlineElement(project, myFixture.editor, element)
        val transformed = myFixture.editor.document.text
        assertEquals("let compute = () => {\n  (1 + 2) * 3\n}\nConsole.log(compute())", transformed)

        Files.writeString(
            projectDir.resolve("package.json"),
            """{"name":"inline-regression","type":"module","private":true,"devDependencies":{"rescript":"12.2.0"}}""",
        )
        Files.writeString(
            projectDir.resolve("rescript.json"),
            """{"name":"inline-regression","sources":[{"dir":"src"}],"package-specs":{"module":"esmodule","in-source":true},"suffix":".res.js"}""",
        )
        val source = Files.createDirectory(projectDir.resolve("src")).resolve("Example.res")
        val install = IntegrationTestSupport.exec(projectDir, listOf("npm", "install", "--no-audit", "--no-fund"))
        assertTrue(install.succeeded, install.stdout + install.stderr)
        for (text in listOf(original, transformed)) {
            Files.writeString(source, text)
            val compile = IntegrationTestSupport.exec(projectDir, listOf("npx", "rescript", "build"))
            assertTrue(compile.succeeded, compile.stdout + compile.stderr)
            val run = IntegrationTestSupport.exec(projectDir, listOf("node", "src/Example.res.js"))
            assertTrue(run.succeeded, run.stdout + run.stderr)
            assertEquals("9", run.stdout.trim())
        }
    }
}
