package com.rescript.plugin.intention

import com.rescript.plugin.lsp.RescriptLspUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptMissingArmsBuilderTest {
    private val optionConstructors =
        listOf(
            RescriptLspUtils.VariantInfo("Some", hasPayload = true),
            RescriptLspUtils.VariantInfo("None", hasPayload = false),
        )
    private val resultConstructors =
        listOf(
            RescriptLspUtils.VariantInfo("Ok", hasPayload = true),
            RescriptLspUtils.VariantInfo("Error", hasPayload = true),
        )
    private val rgbConstructors =
        listOf(
            RescriptLspUtils.VariantInfo("Red", hasPayload = false),
            RescriptLspUtils.VariantInfo("Green", hasPayload = false),
            RescriptLspUtils.VariantInfo("Blue", hasPayload = false),
        )

    @Test
    fun `option with only Some arm suggests None`() {
        val source =
            """
            let f = x => switch x {
            | Some(v) => v
            }
            """.trimIndent()
        val offset = source.indexOf("switch")
        val result = RescriptMissingArmsBuilder.computeMissing(source, offset, optionConstructors)
        assertNotNull(result)
        assertEquals(listOf("None"), result!!.missingNames)
        // bodyEndOffset points at the closing `}`.
        assertEquals('}', source[result.insertOffset])
        // Inserted text begins directly with the indented arm because the
        // existing body already ends with `\n`.
        assertEquals("| None => todo\n", result.insertText)
    }

    @Test
    fun `result fully covered returns null`() {
        val source =
            """
            let f = x => switch x {
            | Ok(v) => v
            | Error(_) => 0
            }
            """.trimIndent()
        val offset = source.indexOf("switch")
        val result = RescriptMissingArmsBuilder.computeMissing(source, offset, resultConstructors)
        assertNull(result)
    }

    @Test
    fun `custom variant with two of three covered suggests the third`() {
        val source =
            """
            let f = x => switch x {
            | Red => 0
            | Green => 1
            }
            """.trimIndent()
        val offset = source.indexOf("switch")
        val result = RescriptMissingArmsBuilder.computeMissing(source, offset, rgbConstructors)
        assertNotNull(result)
        assertEquals(listOf("Blue"), result!!.missingNames)
        assertEquals("| Blue => todo\n", result.insertText)
    }

    @Test
    fun `or-pattern arm covers both constructors`() {
        val source =
            """
            let f = x => switch x {
            | Red | Green => 0
            }
            """.trimIndent()
        val offset = source.indexOf("switch")
        val result = RescriptMissingArmsBuilder.computeMissing(source, offset, rgbConstructors)
        assertNotNull(result)
        assertEquals(listOf("Blue"), result!!.missingNames)
    }

    @Test
    fun `wildcard arm makes switch exhaustive`() {
        val source =
            """
            let f = x => switch x {
            | Some(v) => v
            | _ => 0
            }
            """.trimIndent()
        val offset = source.indexOf("switch")
        val result = RescriptMissingArmsBuilder.computeMissing(source, offset, optionConstructors)
        assertNull(result)
    }

    @Test
    fun `bare lident binding arm makes switch exhaustive`() {
        val source =
            """
            let f = x => switch x {
            | anything => 0
            }
            """.trimIndent()
        val offset = source.indexOf("switch")
        val result = RescriptMissingArmsBuilder.computeMissing(source, offset, optionConstructors)
        assertNull(result)
    }

    @Test
    fun `nested switch picks the inner switch when caret is inside`() {
        val source =
            """
            let f = x => switch x {
            | Some(inner) => switch inner {
                | Red => 0
              }
            }
            """.trimIndent()
        // Caret on the inner `switch`
        val offset = source.indexOf("switch inner")
        val result = RescriptMissingArmsBuilder.computeMissing(source, offset, rgbConstructors)
        assertNotNull(result)
        // Inner switch is missing Green and Blue
        assertEquals(listOf("Green", "Blue"), result!!.missingNames)
    }

    @Test
    fun `unterminated switch returns null`() {
        // Closing brace deliberately missing — the collector skips arms whose
        // body end could not be determined.
        val source = "let f = x => switch x { | Some(_) => 1"
        val offset = source.indexOf("switch")
        val result = RescriptMissingArmsBuilder.computeMissing(source, offset, optionConstructors)
        assertNull(result)
    }

    @Test
    fun `isInsideSwitch and hasWildcardArm helpers`() {
        val sourceWild =
            """
            let f = x => switch x {
            | Some(v) => v
            | _ => 0
            }
            """.trimIndent()
        val offsetIn = sourceWild.indexOf("switch")
        assertTrue(RescriptMissingArmsBuilder.isInsideSwitch(sourceWild, offsetIn))
        assertTrue(RescriptMissingArmsBuilder.hasWildcardArm(sourceWild, offsetIn))

        val sourceOutside = "let x = 42\nlet y = x + 1\n"
        assertTrue(!RescriptMissingArmsBuilder.isInsideSwitch(sourceOutside, 0))
    }

    @Test
    fun `scrutineeOffset returns the offset of the matched expression`() {
        val source =
            """
            let f = x => switch x {
            | Some(v) => v
            }
            """.trimIndent()
        val caret = source.indexOf("switch")
        val scrut = RescriptMissingArmsBuilder.scrutineeOffset(source, caret)
        assertNotNull(scrut)
        // The scrutinee is `x`, which appears after `switch `.
        assertEquals('x', source[scrut!!])
    }
}
