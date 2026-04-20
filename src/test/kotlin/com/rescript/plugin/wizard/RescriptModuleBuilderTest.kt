package com.rescript.plugin.wizard

import com.rescript.plugin.RescriptIcons
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

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
    fun `default package manager is PNPM`() {
        assertEquals(PackageManager.PNPM, builder.packageManager)
    }

    @Test
    fun `default validation library is ZOD`() {
        assertEquals(ValidationLibrary.ZOD, builder.validationLibrary)
    }

    @Test
    fun `validation library can be changed`() {
        builder.validationLibrary = ValidationLibrary.SURY
        assertEquals(ValidationLibrary.SURY, builder.validationLibrary)
    }

    @Test
    fun `default selected template is BASIC`() {
        assertEquals(ProjectTemplate.BASIC, builder.selectedTemplate)
    }

    @Test
    fun `package manager can be changed`() {
        builder.packageManager = PackageManager.PNPM
        assertEquals(PackageManager.PNPM, builder.packageManager)
    }

    @Test
    fun `selected template can be changed`() {
        builder.selectedTemplate = ProjectTemplate.VITE_REACT
        assertEquals(ProjectTemplate.VITE_REACT, builder.selectedTemplate)
    }

    @Test
    fun `all templates can be selected`() {
        ProjectTemplate.entries.forEach {
            builder.selectedTemplate = it
            assertEquals(it, builder.selectedTemplate)
        }
    }
}
