package com.rescript.plugin.editor

import com.intellij.codeInsight.editorActions.JoinLinesHandlerDelegate
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptJoinLinesHandlerTest {
    @Test
    fun `instance can be created`() {
        val handler = RescriptJoinLinesHandler()
        assertNotNull(handler)
    }

    @Test
    fun `is a JoinLinesHandlerDelegate`() {
        val handler = RescriptJoinLinesHandler()
        assertTrue(handler is JoinLinesHandlerDelegate)
    }
}
