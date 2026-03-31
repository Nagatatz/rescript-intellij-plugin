package com.rescript.plugin.projectview

import com.intellij.ide.projectView.ProjectViewNestingRulesProvider
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptFileNestingProviderTest {
    private val provider = RescriptFileNestingProvider()

    @Test
    fun `registers res to compiled js nesting rules`() {
        val rules = collectRules()
        val resRules = rules.filter { it.first == ".res" }.map { it.second }
        assertTrue(resRules.contains(".res.js"), "Should contain .res -> .res.js rule")
        assertTrue(resRules.contains(".res.mjs"), "Should contain .res -> .res.mjs rule")
        assertTrue(resRules.contains(".res.cjs"), "Should contain .res -> .res.cjs rule")
        assertTrue(resRules.contains(".bs.js"), "Should contain .res -> .bs.js rule")
        assertTrue(resRules.contains(".bs.mjs"), "Should contain .res -> .bs.mjs rule")
        assertTrue(resRules.contains(".bs.cjs"), "Should contain .res -> .bs.cjs rule")
    }

    @Test
    fun `registers resi to compiled js nesting rules`() {
        val rules = collectRules()
        val resiRules = rules.filter { it.first == ".resi" }.map { it.second }
        assertTrue(resiRules.contains(".resi.js"), "Should contain .resi -> .resi.js rule")
        assertTrue(resiRules.contains(".resi.mjs"), "Should contain .resi -> .resi.mjs rule")
        assertTrue(resiRules.contains(".resi.cjs"), "Should contain .resi -> .resi.cjs rule")
    }

    @Test
    fun `registers nine rules total`() {
        val rules = collectRules()
        assertTrue(rules.size == 9, "Should register exactly 9 nesting rules, got ${rules.size}")
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
