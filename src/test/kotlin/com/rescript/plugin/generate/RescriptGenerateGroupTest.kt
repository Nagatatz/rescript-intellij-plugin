package com.rescript.plugin.generate

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptGenerateGroupTest {
    private val group = RescriptGenerateGroup()

    @Test
    fun `getChildren returns five actions`() {
        val children = group.getChildren(null)
        assertEquals(5, children.size)
    }

    @Test
    fun `getChildren contains RescriptGenerateSwitchAction`() {
        val children = group.getChildren(null)
        assertTrue(
            children[0] is RescriptGenerateSwitchAction,
            "First action should be RescriptGenerateSwitchAction",
        )
    }

    @Test
    fun `getChildren contains RescriptGenerateModuleTypeAction`() {
        val children = group.getChildren(null)
        assertTrue(
            children[1] is RescriptGenerateModuleTypeAction,
            "Second action should be RescriptGenerateModuleTypeAction",
        )
    }

    @Test
    fun `getChildren contains RescriptGenerateMakeAction`() {
        val children = group.getChildren(null)
        assertTrue(
            children[2] is RescriptGenerateMakeAction,
            "Third action should be RescriptGenerateMakeAction",
        )
    }

    @Test
    fun `getChildren contains RescriptGenerateRecordValueAction`() {
        val children = group.getChildren(null)
        assertTrue(
            children[3] is RescriptGenerateRecordValueAction,
            "Fourth action should be RescriptGenerateRecordValueAction",
        )
    }

    @Test
    fun `getChildren contains RescriptGenerateJsonCodecAction`() {
        val children = group.getChildren(null)
        assertTrue(
            children[4] is RescriptGenerateJsonCodecAction,
            "Fifth action should be RescriptGenerateJsonCodecAction",
        )
    }

    @Test
    fun `getChildren returns same array on multiple calls`() {
        val first = group.getChildren(null)
        val second = group.getChildren(null)
        // The actions array is a stored field, so the same instances should be returned
        assertTrue(first[0] === second[0])
        assertTrue(first[1] === second[1])
        assertTrue(first[2] === second[2])
        assertTrue(first[3] === second[3])
        assertTrue(first[4] === second[4])
    }

    @Test
    fun `getChildren returns non-empty array`() {
        val children = group.getChildren(null)
        assertTrue(children.isNotEmpty())
    }
}
