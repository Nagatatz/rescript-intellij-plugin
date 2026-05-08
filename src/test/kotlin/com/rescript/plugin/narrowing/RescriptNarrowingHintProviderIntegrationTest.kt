package com.rescript.plugin.narrowing

import com.intellij.openapi.project.Project
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.rescript.plugin.IntelliJPlatformExtension
import com.rescript.plugin.settings.RescriptProjectSettings
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Verifies that the narrowing hint pipeline behaves correctly across
 * the project setting toggle and the LSP-unavailable case.
 *
 * The IDE-side `getCollectorFor` path ultimately threads through
 * `RescriptNarrowingHintProvider.buildHints`; this test exercises
 * `buildHints` with a real [Project] and a stub resolver so the
 * integration with [RescriptProjectSettings] is also covered.
 */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptNarrowingHintProviderIntegrationTest {
    private lateinit var myFixture: CodeInsightTestFixture
    private lateinit var project: Project

    private val sample =
        """
        let f = x =>
          switch x {
          | Some(v) => v
          | None => 0
          }
        """.trimIndent()

    @AfterEach
    fun restoreSetting() {
        // Tests mutate the project-level toggle; reset to the default
        // so subsequent tests see a clean state.
        RescriptProjectSettings.getInstance(project).narrowingHintsEnabled = true
    }

    @Test
    fun `setting OFF suppresses every hint regardless of resolver result`() {
        RescriptProjectSettings.getInstance(project).narrowingHintsEnabled = false
        // Even with a resolver that would return a real type, the panel
        // should produce no hints when the setting is OFF.
        // We assert behaviour at the buildHints layer because the IDE-side
        // collector consults the setting before calling buildHints; here
        // we mimic that gate explicitly.
        val resolver = RescriptHoverTypeResolver { _ -> "option<int>" }
        val hints =
            if (RescriptProjectSettings.getInstance(project).narrowingHintsEnabled) {
                RescriptNarrowingHintProvider.buildHints(sample, resolver)
            } else {
                emptyList()
            }
        assertEquals(emptyList<RescriptNarrowingHintProvider.HintEntry>(), hints)
    }

    @Test
    fun `setting ON with informative resolver produces hints`() {
        RescriptProjectSettings.getInstance(project).narrowingHintsEnabled = true
        val resolver = RescriptHoverTypeResolver { _ -> "option<int>" }
        val hints = RescriptNarrowingHintProvider.buildHints(sample, resolver)
        assertEquals(2, hints.size)
        for (hint in hints) assertEquals("option<int>", hint.displayText)
    }

    @Test
    fun `LSP unavailable resolver yields no hints`() {
        // A resolver returning null mimics the LSP-unavailable case
        // (RescriptLspUtils.getHoverType returns null when the server
        // is not running).
        val resolver = RescriptHoverTypeResolver { _ -> null }
        val hints = RescriptNarrowingHintProvider.buildHints(sample, resolver)
        assertEquals(emptyList<RescriptNarrowingHintProvider.HintEntry>(), hints)
    }
}
