package com.rescript.plugin.completion

import com.rescript.plugin.lang.RecordField
import com.rescript.plugin.lang.VariantConstructor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/**
 * Unit tests for the pure placeholder builder that renders record skeletons
 * and variant constructor candidates.
 */
class RescriptPlaceholderBuilderTest {
    private fun field(name: String) = RecordField(name = name, typeAnnotation = "int", isMutable = false)

    @Test
    fun `record with no fields yields null`() {
        assertNull(RescriptPlaceholderBuilder.buildRecordPlaceholder(emptyList()))
    }

    @Test
    fun `record with one field`() {
        assertEquals("{ name: _ }", RescriptPlaceholderBuilder.buildRecordPlaceholder(listOf(field("name"))))
    }

    @Test
    fun `record with multiple fields preserves order`() {
        val fields = listOf(field("name"), field("age"), field("email"))
        assertEquals("{ name: _, age: _, email: _ }", RescriptPlaceholderBuilder.buildRecordPlaceholder(fields))
    }

    @Test
    fun `mutable field is still listed`() {
        val mutable = RecordField(name = "count", typeAnnotation = "int", isMutable = true)
        assertEquals("{ count: _ }", RescriptPlaceholderBuilder.buildRecordPlaceholder(listOf(mutable)))
    }

    @Test
    fun `variant with payload renders parenthesized hole`() {
        val result = RescriptPlaceholderBuilder.buildVariantPlaceholders(listOf(VariantConstructor("Some", "'a")))
        assertEquals(listOf(VariantPlaceholder("Some", "Some(_)")), result)
    }

    @Test
    fun `variant without payload renders bare name`() {
        val result = RescriptPlaceholderBuilder.buildVariantPlaceholders(listOf(VariantConstructor("None", null)))
        assertEquals(listOf(VariantPlaceholder("None", "None")), result)
    }

    @Test
    fun `option-shaped variant produces both constructors`() {
        val result =
            RescriptPlaceholderBuilder.buildVariantPlaceholders(
                listOf(VariantConstructor("Some", "'a"), VariantConstructor("None", null)),
            )
        assertEquals(
            listOf(VariantPlaceholder("Some", "Some(_)"), VariantPlaceholder("None", "None")),
            result,
        )
    }

    @Test
    fun `result-shaped variant produces both payload constructors`() {
        val result =
            RescriptPlaceholderBuilder.buildVariantPlaceholders(
                listOf(VariantConstructor("Ok", "'a"), VariantConstructor("Error", "'b")),
            )
        assertEquals(
            listOf(VariantPlaceholder("Ok", "Ok(_)"), VariantPlaceholder("Error", "Error(_)")),
            result,
        )
    }

    @Test
    fun `empty constructor list yields empty placeholders`() {
        assertEquals(emptyList<VariantPlaceholder>(), RescriptPlaceholderBuilder.buildVariantPlaceholders(emptyList()))
    }
}
