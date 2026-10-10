package com.rescript.plugin.refactor

import com.intellij.openapi.command.undo.UndoManager
import com.intellij.openapi.fileEditor.FileEditorManager
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

/** Compiles before/after/undo source to verify bindings, closures and single evaluation. */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptExtractFunctionIntegrationTest {
    private lateinit var myFixture: CodeInsightTestFixture
    private lateinit var project: Project

    @TempDir
    lateinit var projectDir: Path

    @Test
    fun `handler preserves local bindings lambda capture and side effect count`() {
        IntegrationTestSupport.requireBinary("npm")
        IntegrationTestSupport.requireBinary("node")
        Files.writeString(
            projectDir.resolve("package.json"),
            """{"name":"extract-regression","type":"module","private":true,"devDependencies":{"rescript":"12.2.0"}}""",
        )
        Files.writeString(
            projectDir.resolve("rescript.json"),
            """
            {"name":"extract-regression","sources":[{"dir":"src"}],
            "package-specs":{"module":"esmodule","in-source":true},"suffix":".res.js"}
            """.trimIndent(),
        )
        val source = Files.createDirectory(projectDir.resolve("src")).resolve("Example.res")
        val install = IntegrationTestSupport.exec(projectDir, listOf("npm", "install", "--no-audit", "--no-fund"))
        assertTrue(install.succeeded, install.stdout + install.stderr)
        val cases =
            listOf(
                """
                let counter = ref(0)
                let extractedFunction = 42
                let compute = (base) => {
                  let result = <selection>{
                    let temp = base + 1
                    counter.contents = counter.contents + 1
                    temp * 2
                  }</selection>
                  result
                }
                Console.log(compute(3))
                Console.log(counter.contents)
                """.trimIndent() to "8\n1",
                """
                let base = 5
                let compute = <selection>(item) => {
                  item + base
                }</selection>
                Console.log(compute(2))
                """.trimIndent() to "7",
                "<selection>Console.log(\"hello\")</selection>" to "hello",
            )
        for ((marked, output) in cases) {
            myFixture.configureByText("Example.res", marked)
            val original = myFixture.editor.document.text
            RescriptExtractFunctionHandler().invoke(project, myFixture.editor, myFixture.file, null)
            val transformed = myFixture.editor.document.text
            assertTrue(original != transformed)
            val undo = UndoManager.getInstance(project)
            val editor = FileEditorManager.getInstance(project).getSelectedEditor(myFixture.file.virtualFile)
            assertTrue(undo.isUndoAvailable(editor))
            undo.undo(editor)
            val restored = myFixture.editor.document.text
            assertEquals(original, restored)
            for (text in listOf(original, transformed, restored)) {
                Files.writeString(source, text)
                val compile = IntegrationTestSupport.exec(projectDir, listOf("npx", "rescript", "build"))
                assertTrue(compile.succeeded, compile.stdout + compile.stderr)
                val run = IntegrationTestSupport.exec(projectDir, listOf("node", "src/Example.res.js"))
                assertTrue(run.succeeded, run.stdout + run.stderr)
                assertEquals(output, run.stdout.trim())
            }
        }
    }
}
