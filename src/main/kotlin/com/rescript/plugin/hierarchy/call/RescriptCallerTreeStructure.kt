package com.rescript.plugin.hierarchy.call

import com.intellij.ide.hierarchy.HierarchyNodeDescriptor
import com.intellij.ide.hierarchy.HierarchyTreeStructure
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement

/**
 * Tree structure for the "Callers" view in call hierarchy.
 *
 * Builds a tree of functions that call the root function, using
 * [RescriptCallAnalyzer.findCallers] to discover call relationships.
 */
class RescriptCallerTreeStructure(
    project: Project,
    element: PsiElement,
) : HierarchyTreeStructure(
        project,
        RescriptCallHierarchyNodeDescriptor(project, null, element, true),
    ) {
    override fun buildChildren(descriptor: HierarchyNodeDescriptor): Array<Any> {
        val element = descriptor.psiElement ?: return emptyArray()

        val callers = RescriptCallAnalyzer.findCallers(element, myProject)

        return callers
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
