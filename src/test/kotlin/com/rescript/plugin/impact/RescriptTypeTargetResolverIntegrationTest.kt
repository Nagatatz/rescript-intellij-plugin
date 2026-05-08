package com.rescript.plugin.impact

import com.intellij.openapi.project.Project
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.rescript.plugin.IntelliJPlatformExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Drives [RescriptTypeTargetResolver.resolveAt] through real PSI by
 * configuring fixture files for the five common type-declaration
 * shapes. Each test places the caret on the declared name (`t`) and
 * checks that the resolved [TypeTarget] carries that name.
 *
 * Backs the post-merge "5 種類の型定義で動作する" acceptance check
 * from `.steering/20260508-002-type-impact-preview/requirements.md`
 * with an automated regression gate.
 */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptTypeTargetResolverIntegrationTest {
    private lateinit var myFixture: CodeInsightTestFixture
    private lateinit var project: Project

    private fun resolveAtCaret(
        filename: String,
        source: String,
    ): TypeTarget? {
        val psiFile = myFixture.configureByText(filename, source)
        val offset = source.indexOf("<caret>")
        require(offset >= 0) { "Test source must contain a <caret> marker" }
        // configureByText strips the marker before computing the offset, so
        // the editor's caret model already points at the right spot.
        return RescriptTypeTargetResolver.resolveAt(psiFile, myFixture.editor.caretModel.offset)
    }

    @Test
    fun `type alias resolves to its local name`() {
        val target = resolveAtCaret("Alias.res", "type <caret>t = int")
        assertNotNull(target)
        assertEquals("t", target!!.localName)
    }

    @Test
    fun `record type resolves to its local name`() {
        val target = resolveAtCaret("Record.res", "type <caret>t = { x: int }")
        assertNotNull(target)
        assertEquals("t", target!!.localName)
    }

    @Test
    fun `variant type resolves to its local name`() {
        val target = resolveAtCaret("Variant.res", "type <caret>t = | A | B(int)")
        assertNotNull(target)
        assertEquals("t", target!!.localName)
    }

    @Test
    fun `polymorphic variant type resolves to its local name`() {
        val target = resolveAtCaret("PolyVariant.res", "type <caret>t = [#a | #b]")
        assertNotNull(target)
        assertEquals("t", target!!.localName)
    }

    @Test
    fun `abstract type resolves to its local name`() {
        val target = resolveAtCaret("Abstract.res", "type <caret>t")
        assertNotNull(target)
        assertEquals("t", target!!.localName)
    }

    @Test
    fun `caret outside any type declaration returns null`() {
        val target = resolveAtCaret("LetOnly.res", "let <caret>x = 1")
        assertNull(target)
    }
}
