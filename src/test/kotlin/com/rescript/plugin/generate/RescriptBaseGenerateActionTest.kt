package com.rescript.plugin.generate

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptBaseGenerateActionTest {
    @Test
    fun testActionUpdateThreadIsBGT() {
        val action =
            object : RescriptBaseGenerateAction("Test", "Test description") {
                override fun actionPerformed(e: AnActionEvent) {}

                override fun update(e: AnActionEvent) {}
            }
        assertEquals(ActionUpdateThread.BGT, action.actionUpdateThread)
    }

    @Test
    fun testAllConcreteActionsExtendBaseGenerateAction() {
        val actions: List<Any> =
            listOf(
                RescriptGenerateSwitchAction(),
                RescriptGenerateMakeAction(),
                RescriptGenerateModuleTypeAction(),
                RescriptGenerateModuleImplAction(),
                RescriptGenerateRecordValueAction(),
                RescriptGenerateJsonCodecAction(),
            )
        for (action in actions) {
            assertTrue(
                action is RescriptBaseGenerateAction,
                "${action::class.simpleName} should extend RescriptBaseGenerateAction",
            )
        }
    }

    @Test
    fun testAllConcreteActionsReturnBGT() {
        val actions =
            listOf(
                RescriptGenerateSwitchAction(),
                RescriptGenerateMakeAction(),
                RescriptGenerateModuleTypeAction(),
                RescriptGenerateModuleImplAction(),
                RescriptGenerateRecordValueAction(),
                RescriptGenerateJsonCodecAction(),
            )
        for (action in actions) {
            assertEquals(
                ActionUpdateThread.BGT,
                action.actionUpdateThread,
                "${action::class.simpleName} should return BGT",
            )
        }
    }
}
