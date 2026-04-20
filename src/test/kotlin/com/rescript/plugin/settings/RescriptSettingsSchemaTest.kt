package com.rescript.plugin.settings

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptSettingsSchemaTest {
    @Test
    fun `entries contain the expected number of fields and separators`() {
        val fields = RescriptSettingsSchema.entries.filterIsInstance<SchemaEntry.Field<*>>()
        val separators = RescriptSettingsSchema.entries.filter { it === SchemaEntry.Separator }

        assertEquals(19, fields.size, "should expose all 19 persisted settings")
        assertEquals(5, separators.size, "should retain the 5 group separators from the legacy layout")
    }

    @Test
    fun `descriptor ids are unique`() {
        val ids =
            RescriptSettingsSchema.entries
                .filterIsInstance<SchemaEntry.Field<*>>()
                .map { it.descriptor.id }
        assertEquals(ids.size, ids.toSet().size, "descriptor ids must be unique for component lookup")
    }

    @Test
    fun `path descriptor ids are classified consistently`() {
        val pathIds =
            RescriptSettingsSchema.entries
                .filterIsInstance<SchemaEntry.Field<*>>()
                .mapNotNull { (it.descriptor as? PathDescriptor)?.id }
                .toSet()

        assertEquals(RescriptSettingsSchema.pathDescriptorIds, pathIds)
    }

    @Test
    fun `every descriptor round-trips against RescriptProjectSettings`() {
        val settings = RescriptProjectSettings()
        RescriptSettingsSchema.entries
            .filterIsInstance<SchemaEntry.Field<*>>()
            .forEach { entry -> roundTrip(entry, settings) }
    }

    @Test
    fun `layout begins with the LSP path and ends with the log level`() {
        val fields = RescriptSettingsSchema.entries.filterIsInstance<SchemaEntry.Field<*>>()
        assertEquals("lspServerPath", fields.first().descriptor.id)
        assertEquals("logLevel", fields.last().descriptor.id)
    }

    @Test
    fun `first entry is a field and the layout includes at least one separator block`() {
        assertTrue(RescriptSettingsSchema.entries.first() is SchemaEntry.Field<*>)
        assertTrue(RescriptSettingsSchema.entries.any { it === SchemaEntry.Separator })
    }

    private fun <T> roundTrip(
        entry: SchemaEntry.Field<T>,
        settings: RescriptProjectSettings,
    ) {
        val original = entry.descriptor.currentValue(settings)
        entry.descriptor.applyValue(settings, original)
        assertEquals(original, entry.descriptor.currentValue(settings))
    }
}
