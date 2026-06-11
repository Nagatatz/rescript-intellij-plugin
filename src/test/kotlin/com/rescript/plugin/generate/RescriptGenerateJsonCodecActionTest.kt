package com.rescript.plugin.generate

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.rescript.plugin.lang.RecordField
import com.rescript.plugin.lang.TypeShape
import com.rescript.plugin.lang.VariantConstructor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptGenerateJsonCodecActionTest {
    @Test
    fun `getActionUpdateThread returns BGT`() {
        assertEquals(ActionUpdateThread.BGT, RescriptGenerateJsonCodecAction().getActionUpdateThread())
    }

    // --- isValidShape ---

    @Test
    fun `isValidShape returns true for record with fields`() {
        val shape = TypeShape.Record(listOf(RecordField("name", "string", false)))
        assertTrue(RescriptGenerateJsonCodecAction.isValidShape(shape))
    }

    @Test
    fun `isValidShape returns false for empty record`() {
        val shape = TypeShape.Record(emptyList())
        assertFalse(RescriptGenerateJsonCodecAction.isValidShape(shape))
    }

    @Test
    fun `isValidShape returns true for variant with constructors`() {
        val shape = TypeShape.Variant(listOf(VariantConstructor("Red", null)))
        assertTrue(RescriptGenerateJsonCodecAction.isValidShape(shape))
    }

    @Test
    fun `isValidShape returns false for empty variant`() {
        val shape = TypeShape.Variant(emptyList())
        assertFalse(RescriptGenerateJsonCodecAction.isValidShape(shape))
    }

    @Test
    fun `isValidShape returns false for Unknown shape`() {
        assertFalse(RescriptGenerateJsonCodecAction.isValidShape(TypeShape.Unknown))
    }

    @Test
    fun `isValidShape returns true for record with multiple fields`() {
        val shape =
            TypeShape.Record(
                listOf(
                    RecordField("name", "string", false),
                    RecordField("age", "int", false),
                    RecordField("email", "option<string>", false),
                ),
            )
        assertTrue(RescriptGenerateJsonCodecAction.isValidShape(shape))
    }

    @Test
    fun `isValidShape returns true for variant with payload`() {
        val shape =
            TypeShape.Variant(
                listOf(
                    VariantConstructor("Success", "string"),
                    VariantConstructor("Error", "int"),
                ),
            )
        assertTrue(RescriptGenerateJsonCodecAction.isValidShape(shape))
    }
}
