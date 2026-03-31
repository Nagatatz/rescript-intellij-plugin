package com.rescript.plugin.projectview

import com.intellij.ide.projectView.ProjectViewNestingRulesProvider

/**
 * Registers file nesting rules for compiled ReScript JS output files as a
 * supplementary mechanism to [RescriptTreeStructureProvider].
 *
 * Covers all output suffixes: `.res.js`/`.mjs`/`.cjs`, `.bs.js`/`.mjs`/`.cjs`,
 * and their `.resi` counterparts.
 *
 * @see RescriptTreeStructureProvider for the primary nesting implementation
 * @see RescriptCompiledJsNodeDecorator for visual styling of nested files
 */
class RescriptFileNestingProvider : ProjectViewNestingRulesProvider {
    override fun addFileNestingRules(consumer: ProjectViewNestingRulesProvider.Consumer) {
        // .res -> compiled JS variants
        consumer.addNestingRule(".res", ".res.js")
        consumer.addNestingRule(".res", ".res.mjs")
        consumer.addNestingRule(".res", ".res.cjs")
        consumer.addNestingRule(".res", ".bs.js")
        consumer.addNestingRule(".res", ".bs.mjs")
        consumer.addNestingRule(".res", ".bs.cjs")
        // .resi -> compiled JS variants
        consumer.addNestingRule(".resi", ".resi.js")
        consumer.addNestingRule(".resi", ".resi.mjs")
        consumer.addNestingRule(".resi", ".resi.cjs")
    }
}
