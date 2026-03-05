package com.rescript.plugin.projectview

import com.intellij.ide.projectView.ProjectViewNestingRulesProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptFileNestingProviderTest {
    private val provider = RescriptFileNestingProvider()

    @Test
    fun `registers res to res js nesting rule`() {
        val rules = collectRules()
        assertTrue(rules.any { it.first == ".res" && it.second == ".res.js" }, "Should contain .res -> .res.js rule")
    }

    @Test
    fun `registers resi to resi js nesting rule`() {
        val rules = collectRules()
        assertTrue(
            rules.any { it.first == ".resi" && it.second == ".resi.js" },
            "Should contain .resi -> .resi.js rule",
        )
    }

    @Test
    fun `registers exactly two rules`() {
        val rules = collectRules()
        assertEquals(2, rules.size, "Should register exactly 2 nesting rules")
    }

    private fun collectRules(): List<Pair<String, String>> {
        val rules = mutableListOf<Pair<String, String>>()
        val consumer =
            ProjectViewNestingRulesProvider.Consumer { parentSuffix, childSuffix ->
                rules.add(parentSuffix to childSuffix)
            }
        provider.addFileNestingRules(consumer)
        return rules
    }
}
