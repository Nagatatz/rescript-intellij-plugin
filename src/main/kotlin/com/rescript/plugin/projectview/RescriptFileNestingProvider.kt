package com.rescript.plugin.projectview

import com.intellij.ide.projectView.ProjectViewNestingRulesProvider

/**
 * Registers file nesting rules so compiled `.res.js` files appear
 * as children of their corresponding `.res` source files in the Project tool window.
 *
 * This keeps the project tree clean by allowing users to collapse generated output.
 *
 * @see RescriptCompiledJsNodeDecorator for visual styling of nested files
 */
class RescriptFileNestingProvider : ProjectViewNestingRulesProvider {
    override fun addFileNestingRules(consumer: ProjectViewNestingRulesProvider.Consumer) {
        consumer.addNestingRule(".res", ".res.js")
        consumer.addNestingRule(".resi", ".resi.js")
    }
}
