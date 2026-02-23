package com.rescript.plugin.hierarchy.call

import com.intellij.ide.hierarchy.HierarchyNodeDescriptor
import com.intellij.ide.hierarchy.HierarchyTreeStructure
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement

/**
 * Tree structure for the "Callees" view in call hierarchy.
 *
 * Builds a tree of functions that the root function calls, using
 * [RescriptCallAnalyzer.findCallees] to discover call relationships.
 */
class RescriptCalleeTreeStructure(
    project: Project,
    element: PsiElement,
) : HierarchyTreeStructure(
        project,
        RescriptCallHierarchyNodeDescriptor(project, null, element, true),
    ) {
    override fun buildChildren(descriptor: HierarchyNodeDescriptor): Array<Any> {
        val element = descriptor.psiElement ?: return emptyArray()

        val callees = RescriptCallAnalyzer.findCallees(element, myProject)

        return callees
            .map { ref ->
                RescriptCallHierarchyNodeDescriptor(
                    myProject,
                    descriptor,
                    ref.declaration,
                    false,
                )
            }.toTypedArray()
    }
}
