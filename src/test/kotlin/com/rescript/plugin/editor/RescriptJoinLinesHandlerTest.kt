package com.rescript.plugin.editor

import com.intellij.codeInsight.editorActions.JoinLinesHandlerDelegate
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptJoinLinesHandlerTest {
    @Test
    fun `instance can be created`() {
        val handler = RescriptJoinLinesHandler()
        assertNotNull(handler)
    }

    @Test
    fun `is a JoinLinesHandlerDelegate`() {
        val handler: Any = RescriptJoinLinesHandler()
        assertTrue(handler is JoinLinesHandlerDelegate)
    }
}
