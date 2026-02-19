package com.rescript.plugin.config

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory
import com.jetbrains.jsonSchema.extension.SchemaType
import com.jetbrains.jsonSchema.impl.JsonSchemaVersion

/**
 * Factory that registers a JSON Schema provider for ReScript configuration files.
 *
 * Provides schema validation and completion for `rescript.json` and `bsconfig.json`
 * using the bundled schema at `/schemas/rescript.schema.json`.
 */
class RescriptJsonSchemaProviderFactory : JsonSchemaProviderFactory {
    override fun getProviders(project: Project): List<JsonSchemaFileProvider> = listOf(RescriptJsonSchemaFileProvider())
}

/** Provides the embedded JSON schema for ReScript configuration files. */
private class RescriptJsonSchemaFileProvider : JsonSchemaFileProvider {
    override fun isAvailable(file: VirtualFile): Boolean = file.name in CONFIG_FILE_NAMES

    override fun getName(): String = "ReScript"

    override fun getSchemaFile(): VirtualFile? =
        JsonSchemaProviderFactory.getResourceFile(
            RescriptJsonSchemaProviderFactory::class.java,
            SCHEMA_PATH,
        )

    override fun getSchemaType(): SchemaType = SchemaType.embeddedSchema

    override fun getSchemaVersion(): JsonSchemaVersion = JsonSchemaVersion.SCHEMA_4

    companion object {
        private const val SCHEMA_PATH = "/schemas/rescript.schema.json"
        private val CONFIG_FILE_NAMES = setOf("rescript.json", "bsconfig.json")
    }
}
