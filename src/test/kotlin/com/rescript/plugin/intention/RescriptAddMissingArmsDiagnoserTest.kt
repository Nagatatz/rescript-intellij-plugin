package com.rescript.plugin.intention

import com.rescript.plugin.lsp.RescriptLspSignatureParser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Covers every branch of [RescriptAddMissingArmsDiagnoser.diagnose] and the
 * [RescriptAddMissingArmsDiagnoser.messageFor] mapping. The intention class
 * itself remains exempt from unit tests under the LSP-bound rule in
 * `.claude/rules/testing.md`; this file is the pure-logic counterpart that
 * validates the new diagnostic surface.
 */
class RescriptAddMissingArmsDiagnoserTest {
    private val optionVariantType = "option<int>"
    private val rgbVariantType = "Red | Green | Blue"

    @Test
    fun `NotInSwitch outcome when caret is outside any switch`() {
        val source = "let x = 1\n"
        val outcome =
            RescriptAddMissingArmsDiagnoser.diagnose(
                source = source,
                offset = 0,
                lspServerAvailable = true,
                hoverProbe = { optionVariantType },
            )
        assertEquals(ArmsOutcome.NotInSwitch, outcome)
    }

    @Test
    fun `LspUnavailable outcome when caret is inside switch but LSP is not running`() {
        val source =
            """
            let f = x => switch x {
            | Some(v) => v
            }
            """.trimIndent()
        val offset = source.indexOf("switch")
        val outcome =
            RescriptAddMissingArmsDiagnoser.diagnose(
                source = source,
                offset = offset,
                lspServerAvailable = false,
                hoverProbe = { error("hoverProbe should not be queried when LSP is unavailable") },
            )
        assertEquals(ArmsOutcome.LspUnavailable, outcome)
    }

    @Test
    fun `HoverEmpty outcome when LSP hover returns null`() {
        val source =
            """
            let f = x => switch x {
            | Some(v) => v
            }
            """.trimIndent()
        val offset = source.indexOf("switch")
        val outcome =
            RescriptAddMissingArmsDiagnoser.diagnose(
                source = source,
                offset = offset,
                lspServerAvailable = true,
                hoverProbe = { null },
            )
        assertEquals(ArmsOutcome.HoverEmpty, outcome)
    }

    @Test
    fun `NotVariant outcome when hover yields a non-variant type`() {
        val source =
            """
            let f = x => switch x {
            | Some(v) => v
            }
            """.trimIndent()
        val offset = source.indexOf("switch")
        val outcome =
            RescriptAddMissingArmsDiagnoser.diagnose(
                source = source,
                offset = offset,
                lspServerAvailable = true,
                hoverProbe = { "int" },
            )
        assertTrue(outcome is ArmsOutcome.NotVariant)
        assertEquals("int", (outcome as ArmsOutcome.NotVariant).typeText)
    }

    @Test
    fun `NoMissingArms outcome when every constructor already has an arm`() {
        val source =
            """
            let f = x => switch x {
            | Red => 0
            | Green => 1
            | Blue => 2
            }
            """.trimIndent()
        val offset = source.indexOf("switch")
        val outcome =
            RescriptAddMissingArmsDiagnoser.diagnose(
                source = source,
                offset = offset,
                lspServerAvailable = true,
                hoverProbe = { rgbVariantType },
            )
        assertEquals(ArmsOutcome.NoMissingArms, outcome)
    }

    @Test
    fun `Ready outcome when missing arms can be computed`() {
        val source =
            """
            let f = x => switch x {
            | Red => 0
            }
            """.trimIndent()
        val offset = source.indexOf("switch")
        val outcome =
            RescriptAddMissingArmsDiagnoser.diagnose(
                source = source,
                offset = offset,
                lspServerAvailable = true,
                hoverProbe = { rgbVariantType },
            )
        assertTrue(outcome is ArmsOutcome.Ready)
        val result = (outcome as ArmsOutcome.Ready).result
        assertEquals(listOf("Green", "Blue"), result.missingNames)
    }

    @Test
    fun `messageFor returns null on NotInSwitch and Ready`() {
        assertNull(RescriptAddMissingArmsDiagnoser.messageFor(ArmsOutcome.NotInSwitch))
        // Construct a dummy Ready outcome — `messageFor` ignores the payload.
        val ready =
            ArmsOutcome.Ready(
                result =
                    MissingArmsResult(
                        insertOffset = 0,
                        insertText = "",
                        missingNames = emptyList(),
                    ),
            )
        assertNull(RescriptAddMissingArmsDiagnoser.messageFor(ready))
    }

    @Test
    fun `messageFor surfaces a non-empty message for each failure outcome`() {
        val failures =
            listOf(
                ArmsOutcome.NoScrutinee,
                ArmsOutcome.LspUnavailable,
                ArmsOutcome.HoverEmpty,
                ArmsOutcome.NotVariant("int"),
                ArmsOutcome.NoMissingArms,
            )
        failures.forEach { outcome ->
            val message = RescriptAddMissingArmsDiagnoser.messageFor(outcome)
            assertNotNull(message, "expected message for $outcome")
            assertTrue(message!!.startsWith("Add missing switch arms:"))
        }
    }

    @Test
    fun `NotVariant message embeds the hovered type text`() {
        val message =
            RescriptAddMissingArmsDiagnoser.messageFor(
                ArmsOutcome.NotVariant("int"),
            )!!
        assertTrue(message.contains("`int`"), "expected backtick-quoted type in $message")
    }

    @Test
    fun `LspUnavailable message mentions starting the language server`() {
        val message =
            RescriptAddMissingArmsDiagnoser.messageFor(
                ArmsOutcome.LspUnavailable,
            )!!
        assertTrue(message.contains("LSP"))
        assertTrue(message.contains("Start"))
    }

    // ── 2nd-pass: bare type name resolved via project-wide lookup ──

    // Fixtures need at least one arm because the underlying switch-arm
    // collector keys off `|`; a literal empty `switch x { }` produces
    // no arms and the diagnoser would short-circuit before reaching the
    // resolver. The fixtures below carry a single arm so the diagnoser
    // walks all the way through and we can observe the resolver call.

    @Test
    fun `bare type-name hover triggers resolver fallback`() {
        val source =
            """
            let f = x => switch x {
            | Red => "red"
            }
            """.trimIndent()
        val offset = source.indexOf("switch")
        var resolverArg: String? = null
        val outcome =
            RescriptAddMissingArmsDiagnoser.diagnose(
                source = source,
                offset = offset,
                lspServerAvailable = true,
                hoverProbe = { "color" },
                resolveByTypeName = { name ->
                    resolverArg = name
                    listOf(
                        RescriptLspSignatureParser.VariantInfo("Red", false),
                        RescriptLspSignatureParser.VariantInfo("Blue", false),
                    )
                },
            )
        assertEquals("color", resolverArg)
        assertTrue(outcome is ArmsOutcome.Ready, "expected Ready, got $outcome")
    }

    @Test
    fun `resolver is not consulted when hover already yields constructors`() {
        val source =
            """
            let f = x => switch x {
            | Red => "red"
            }
            """.trimIndent()
        val offset = source.indexOf("switch")
        var resolverInvoked = false
        RescriptAddMissingArmsDiagnoser.diagnose(
            source = source,
            offset = offset,
            lspServerAvailable = true,
            hoverProbe = { "option<int>" },
            resolveByTypeName = {
                resolverInvoked = true
                emptyList()
            },
        )
        assertTrue(!resolverInvoked, "resolver must not be queried when hover already yields constructors")
    }

    @Test
    fun `bare type-name hover with empty resolver falls back to NotVariant`() {
        val source =
            """
            let f = x => switch x {
            | Red => "red"
            }
            """.trimIndent()
        val offset = source.indexOf("switch")
        val outcome =
            RescriptAddMissingArmsDiagnoser.diagnose(
                source = source,
                offset = offset,
                lspServerAvailable = true,
                hoverProbe = { "unknownType" },
                resolveByTypeName = { emptyList() },
            )
        assertTrue(outcome is ArmsOutcome.NotVariant, "expected NotVariant, got $outcome")
    }

    @Test
    fun `type application hover bypasses resolver fallback`() {
        // option<int> should be handled by the existing hardcoded path,
        // not delegated to the project-wide resolver.
        val source =
            """
            let f = x => switch x {
            | Some(v) => v
            }
            """.trimIndent()
        val offset = source.indexOf("switch")
        var resolverInvoked = false
        val outcome =
            RescriptAddMissingArmsDiagnoser.diagnose(
                source = source,
                offset = offset,
                lspServerAvailable = true,
                hoverProbe = { "option<int>" },
                resolveByTypeName = {
                    resolverInvoked = true
                    emptyList()
                },
            )
        assertTrue(!resolverInvoked)
        assertTrue(outcome is ArmsOutcome.Ready)
    }
}
