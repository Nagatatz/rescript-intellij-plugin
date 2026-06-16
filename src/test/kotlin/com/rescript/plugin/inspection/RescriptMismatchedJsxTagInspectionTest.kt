package com.rescript.plugin.inspection

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementVisitor
import com.rescript.plugin.ParsingTestHelper
import com.rescript.plugin.RescriptParsingTestExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Tests for [RescriptMismatchedJsxTagInspection].
 *
 * Drives the real lexer+parser via [RescriptParsingTestExtension] and runs the
 * inspection's visitor against a manually built [ProblemsHolder], so detection is
 * verified end-to-end without a heavy fixture.
 *
 * @see RescriptMismatchedJsxTagInspection
 */
@ExtendWith(RescriptParsingTestExtension::class)
class RescriptMismatchedJsxTagInspectionTest {
    private lateinit var parsingHelper: ParsingTestHelper

    private fun problems(code: String): List<ProblemDescriptor> {
        val file = parsingHelper.parseCode(code)
        val holder = ProblemsHolder(InspectionManager.getInstance(file.project), file, true)
        val visitor = RescriptMismatchedJsxTagInspection().buildVisitor(holder, true)
        file.accept(
            object : PsiRecursiveElementVisitor() {
                override fun visitElement(element: PsiElement) {
                    visitor.visitElement(element)
                    super.visitElement(element)
                }
            },
        )
        return holder.results
    }

    @Test
    fun testDetectsMismatchedHtmlTag() {
        val results = problems("let x = <div></span>")
        assertEquals(1, results.size)
        assertTrue(results[0].descriptionTemplate.contains("div"))
        assertTrue(results[0].descriptionTemplate.contains("span"))
    }

    @Test
    fun testDetectsMismatchedComponentTag() {
        val results = problems("let x = <Foo></Bar>")
        assertEquals(1, results.size)
    }

    @Test
    fun testNoProblemForMatchingTag() {
        assertEquals(0, problems("let x = <div></div>").size)
    }

    @Test
    fun testNoProblemForMatchingNestedSameName() {
        // Nested elements with identical names must not be flagged.
        assertEquals(0, problems("let x = <div><div></div></div>").size)
    }

    @Test
    fun testNoProblemForFragment() {
        assertEquals(0, problems("let x = <> </>").size)
    }

    @Test
    fun testNoProblemForSelfClosing() {
        assertEquals(0, problems("let x = <input />").size)
    }
}
