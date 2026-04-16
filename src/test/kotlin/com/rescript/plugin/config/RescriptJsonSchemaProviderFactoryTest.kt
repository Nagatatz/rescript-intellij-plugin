package com.rescript.plugin.config

import com.intellij.openapi.project.Project
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.jetbrains.jsonSchema.extension.SchemaType
import com.jetbrains.jsonSchema.impl.JsonSchemaVersion
import com.rescript.plugin.IntelliJPlatformExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Unit tests for [RescriptJsonSchemaProviderFactory] and the bundled
 * private file provider it returns.
 *
 * Verifies the factory yields a provider that recognizes ReScript
 * configuration filenames (`rescript.json`, `bsconfig.json`), exposes the
 * embedded schema resource, and reports the documented schema metadata.
 */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptJsonSchemaProviderFactoryTest {
    private lateinit var myFixture: CodeInsightTestFixture
    private lateinit var project: Project

    private val factory = RescriptJsonSchemaProviderFactory()

    @Test
    fun testGetProvidersReturnsSingleProvider() {
        val providers = factory.getProviders(project)
        assertEquals(1, providers.size)
    }

    @Test
    fun testProviderRecognizesRescriptJson() {
        val provider = factory.getProviders(project).single()
        val file = myFixture.addFileToProject("rescript.json", "{}").virtualFile
        assertTrue(provider.isAvailable(file))
    }

    @Test
    fun testProviderRecognizesBsconfigJson() {
        val provider = factory.getProviders(project).single()
        val file = myFixture.addFileToProject("bsconfig.json", "{}").virtualFile
        assertTrue(provider.isAvailable(file))
    }

    @Test
    fun testProviderIgnoresUnrelatedJson() {
        val provider = factory.getProviders(project).single()
        val file = myFixture.addFileToProject("package.json", "{}").virtualFile
        assertFalse(provider.isAvailable(file))
    }

    @Test
    fun testProviderIgnoresNonJsonFiles() {
        val provider = factory.getProviders(project).single()
        val file = myFixture.addFileToProject("rescript.json.bak", "{}").virtualFile
        assertFalse(provider.isAvailable(file))
    }

    @Test
    fun testProviderName() {
        val provider = factory.getProviders(project).single()
        assertEquals("ReScript", provider.name)
    }

    @Test
    fun testProviderSchemaType() {
        val provider = factory.getProviders(project).single()
        assertEquals(SchemaType.embeddedSchema, provider.schemaType)
    }

    @Test
    fun testProviderSchemaVersion() {
        val provider = factory.getProviders(project).single()
        assertEquals(JsonSchemaVersion.SCHEMA_4, provider.schemaVersion)
    }

    @Test
    fun testProviderSchemaFileResolves() {
        val provider = factory.getProviders(project).single()
        // Returns null only if /schemas/rescript.schema.json is missing from the classpath.
        assertNotNull(provider.schemaFile)
    }
}
