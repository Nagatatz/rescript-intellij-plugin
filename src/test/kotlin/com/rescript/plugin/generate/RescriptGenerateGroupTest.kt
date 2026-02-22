package com.rescript.plugin.generate

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptGenerateGroupTest {
    private val group = RescriptGenerateGroup()

    @Test
    fun `getChildren returns four actions`() {
        val children = group.getChildren(null)
        assertEquals(4, children.size)
    }

    @Test
    fun `getChildren contains RescriptGenerateSwitchAction`() {
        val children = group.getChildren(null)
        assertTrue(
            "First action should be RescriptGenerateSwitchAction",
            children[0] is RescriptGenerateSwitchAction,
        )
    }

    @Test
    fun `getChildren contains RescriptGenerateModuleTypeAction`() {
        val children = group.getChildren(null)
        assertTrue(
            "Second action should be RescriptGenerateModuleTypeAction",
            children[1] is RescriptGenerateModuleTypeAction,
        )
    }

    @Test
    fun `getChildren contains RescriptGenerateMakeAction`() {
        val children = group.getChildren(null)
        assertTrue(
            "Third action should be RescriptGenerateMakeAction",
            children[2] is RescriptGenerateMakeAction,
        )
    }

    @Test
    fun `getChildren contains RescriptGenerateJsonCodecAction`() {
        val children = group.getChildren(null)
        assertTrue(
            "Fourth action should be RescriptGenerateJsonCodecAction",
            children[3] is RescriptGenerateJsonCodecAction,
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
    }

    @Test
    fun `getChildren returns non-empty array`() {
        val children = group.getChildren(null)
        assertTrue(children.isNotEmpty())
    }
}
