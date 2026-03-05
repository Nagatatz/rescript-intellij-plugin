package com.rescript.plugin.inspection

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptSuggestedRefactoringInspectionTest {
    @Test
    fun `findLongFunctions detects function over threshold`() {
        val lines =
            buildList {
                add("let myFunc = () => {")
                repeat(25) { add("  let x$it = $it") }
                add("}")
            }
        val results = RescriptSuggestedRefactoringInspection.findLongFunctions(lines)
        assertTrue(results.isNotEmpty())
        assertEquals(0, results.first().line)
    }

    @Test
    fun `findLongFunctions ignores short functions`() {
        val lines =
            listOf(
                "let short = () => {",
                "  let x = 1",
                "  x + 1",
                "}",
            )
        val results = RescriptSuggestedRefactoringInspection.findLongFunctions(lines)
        assertTrue(results.isEmpty())
    }

    @Test
    fun `findRepeatedPipeChains detects duplicates`() {
        val lines =
            listOf(
                "let a = arr->Array.map(x => x + 1)->Array.filter(x => x > 0)",
                "let b = arr->Array.map(x => x + 1)->Array.filter(x => x > 0)",
            )
        val results = RescriptSuggestedRefactoringInspection.findRepeatedPipeChains(lines)
        assertTrue(results.isNotEmpty())
    }

    @Test
    fun `findRepeatedPipeChains ignores unique chains`() {
        val lines =
            listOf(
                "let a = arr->Array.map(x => x + 1)",
                "let b = arr->Array.filter(x => x > 0)",
            )
        val results = RescriptSuggestedRefactoringInspection.findRepeatedPipeChains(lines)
        assertTrue(results.isEmpty())
    }

    @Test
    fun `findNestedSwitch detects nested switch`() {
        val lines =
            listOf(
                "switch x {",
                "| Some(y) =>",
                "  switch y {",
                "  | 1 => \"one\"",
                "  | _ => \"other\"",
                "  }",
                "| None => \"none\"",
                "}",
            )
        val results = RescriptSuggestedRefactoringInspection.findNestedSwitch(lines)
        assertTrue(results.isNotEmpty())
    }

    @Test
    fun `findNestedSwitch ignores simple switch`() {
        val lines =
            listOf(
                "switch x {",
                "| Some(y) => y",
                "| None => 0",
                "}",
            )
        val results = RescriptSuggestedRefactoringInspection.findNestedSwitch(lines)
        assertTrue(results.isEmpty())
    }

    @Test
    fun `inspection can be instantiated`() {
        val inspection = RescriptSuggestedRefactoringInspection()
        assertTrue(inspection is com.intellij.codeInspection.LocalInspectionTool)
    }
}
