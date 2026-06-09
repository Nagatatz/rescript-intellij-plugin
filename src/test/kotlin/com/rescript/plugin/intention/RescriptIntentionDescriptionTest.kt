package com.rescript.plugin.intention

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * Regression guard ensuring every `<intentionAction>` registered in `plugin.xml`
 * ships the `intentionDescriptions/<ClassName>/description.html` resource the
 * IntelliJ Platform requires.
 *
 * Missing descriptions surface as a startup `PluginException` from
 * `IntentionSearchableOptionContributor` and are also flagged by the Plugin
 * Verifier, so this test catches the omission before it reaches users.
 */
class RescriptIntentionDescriptionTest {
    /**
     * Reads `plugin.xml` and extracts the simple class name of every
     * registered `<intentionAction>`.
     *
     * @return the directory names (simple class names) expected under `intentionDescriptions/`
     */
    private fun registeredIntentionClassNames(): List<String> {
        val pluginXml = javaClass.getResource("/META-INF/plugin.xml")?.readText()
        assertNotNull(pluginXml, "plugin.xml must be present on the test classpath")
        // Match every <intentionAction>...<className>fqcn</className>...</intentionAction> block.
        val blockRegex = Regex("<intentionAction>(.*?)</intentionAction>", RegexOption.DOT_MATCHES_ALL)
        val classNameRegex = Regex("<className>\\s*([\\w.]+)\\s*</className>")
        return blockRegex
            .findAll(pluginXml!!)
            .mapNotNull { classNameRegex.find(it.groupValues[1])?.groupValues?.get(1) }
            .map { it.substringAfterLast('.') }
            .toList()
    }

    @Test
    fun `plugin xml registers intention actions`() {
        assertFalse(
            registeredIntentionClassNames().isEmpty(),
            "Expected at least one <intentionAction> registration in plugin.xml",
        )
    }

    @Test
    fun `every registered intention has a description html resource`() {
        val missing =
            registeredIntentionClassNames().filter { className ->
                javaClass.getResource("/intentionDescriptions/$className/description.html") == null
            }
        assertFalse(
            missing.isNotEmpty(),
            "Missing intentionDescriptions/<ClassName>/description.html for: $missing",
        )
    }
}
