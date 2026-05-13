package com.rescript.plugin.flow

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Locks in the help text shown in the Switch Flow tool window's empty
 * state. The panel itself is exempt under the Swing-UI clause; this test
 * guards the hint strings against accidental edits and ensures each
 * reason has both a long-form message and a short status counterpart.
 */
class RescriptVariantFlowHintsTest {
    @Test
    fun `every Reason yields a non-empty long-form message`() {
        RescriptVariantFlowHints.Reason.values().forEach { reason ->
            val msg = RescriptVariantFlowHints.emptyStateMessage(reason)
            assertFalse(msg.isBlank(), "expected non-blank message for $reason")
        }
    }

    @Test
    fun `every Reason yields a non-empty short status label`() {
        RescriptVariantFlowHints.Reason.values().forEach { reason ->
            val label = RescriptVariantFlowHints.shortStatusLabel(reason)
            assertFalse(label.isBlank(), "expected non-blank status label for $reason")
        }
    }

    @Test
    fun `NO_EDITOR message walks the user through opening a res file`() {
        val msg = RescriptVariantFlowHints.emptyStateMessage(RescriptVariantFlowHints.Reason.NO_EDITOR)
        assertTrue(msg.contains("How to use"), "expected 'How to use' guidance: $msg")
        assertTrue(msg.contains("Open"), "expected the 'Open' instruction: $msg")
        assertTrue(msg.contains("switch"), "expected `switch` keyword guidance: $msg")
    }

    @Test
    fun `NO_EDITOR message advertises the toolbar exports`() {
        val msg = RescriptVariantFlowHints.emptyStateMessage(RescriptVariantFlowHints.Reason.NO_EDITOR)
        assertTrue(msg.contains("Mermaid"), "expected Mermaid mention: $msg")
        assertTrue(msg.contains("DOT"), "expected DOT mention: $msg")
    }

    @Test
    fun `NOT_RESCRIPT message mentions res and resi file extensions`() {
        val msg = RescriptVariantFlowHints.emptyStateMessage(RescriptVariantFlowHints.Reason.NOT_RESCRIPT)
        assertTrue(msg.contains(".res"), "expected `.res` mention: $msg")
        assertTrue(msg.contains(".resi"), "expected `.resi` mention: $msg")
    }

    @Test
    fun `NO_SWITCH message tells the user to move the caret`() {
        val msg = RescriptVariantFlowHints.emptyStateMessage(RescriptVariantFlowHints.Reason.NO_SWITCH)
        assertTrue(msg.contains("caret"), "expected caret guidance: $msg")
        assertTrue(msg.contains("switch"), "expected `switch` keyword mention: $msg")
    }
}
