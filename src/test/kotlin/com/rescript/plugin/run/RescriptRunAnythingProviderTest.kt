package com.rescript.plugin.run

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptRunAnythingProviderTest {
    private val provider = RescriptRunAnythingProvider()

    @Test
    fun `instance can be created`() {
        assertNotNull(provider)
    }

    @Test
    fun `getCommand returns the input value`() {
        assertEquals("rescript build", provider.getCommand("rescript build"))
    }

    @Test
    fun `getCommand returns arbitrary string unchanged`() {
        assertEquals("anything", provider.getCommand("anything"))
    }

    @Test
    fun `getCommand returns empty string unchanged`() {
        assertEquals("", provider.getCommand(""))
    }

    @Test
    fun `getHelpCommand returns rescript`() {
        assertEquals("rescript", provider.getHelpCommand())
    }

    @Test
    fun `getHelpGroupTitle returns ReScript`() {
        assertEquals("ReScript", provider.getHelpGroupTitle())
    }

    @Test
    fun `getHelpCommandPlaceholder returns expected format`() {
        assertEquals("rescript <command>", provider.getHelpCommandPlaceholder())
    }

    @Test
    fun `getCompletionGroupTitle returns ReScript`() {
        assertEquals("ReScript", provider.getCompletionGroupTitle())
    }

    @Test
    fun `is a RunAnythingProviderBase`() {
        val subject: Any = provider
        assertTrue(subject is com.intellij.ide.actions.runAnything.activity.RunAnythingProviderBase<*>)
    }
}
