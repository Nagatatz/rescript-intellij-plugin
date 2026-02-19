package com.rescript.plugin.wizard

import com.rescript.plugin.RescriptIcons
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test

class RescriptModuleBuilderTest {
    private val builder = RescriptModuleBuilder()

    @Test
    fun `presentable name is ReScript`() {
        assertEquals("ReScript", builder.presentableName)
    }

    @Test
    fun `description is set`() {
        assertEquals("Create a new ReScript project", builder.description)
    }

    @Test
    fun `group name is ReScript`() {
        assertEquals("ReScript", builder.groupName)
    }

    @Test
    fun `node icon is ReScript file icon`() {
        assertEquals(RescriptIcons.FILE, builder.nodeIcon)
    }

    @Test
    fun `module type is not null`() {
        assertNotNull(builder.moduleType)
    }

    @Test
    fun `default package manager is NPM`() {
        assertEquals(PackageManager.NPM, builder.packageManager)
    }

    @Test
    fun `default includeReact is false`() {
        assertFalse(builder.includeReact)
    }

    @Test
    fun `package manager can be changed`() {
        builder.packageManager = PackageManager.PNPM
        assertEquals(PackageManager.PNPM, builder.packageManager)
    }

    @Test
    fun `includeReact can be changed`() {
        builder.includeReact = true
        assertEquals(true, builder.includeReact)
    }
}
