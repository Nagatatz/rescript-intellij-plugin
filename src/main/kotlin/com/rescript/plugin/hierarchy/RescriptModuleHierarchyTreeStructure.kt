package com.rescript.plugin.hierarchy

import com.intellij.ide.hierarchy.HierarchyNodeDescriptor
import com.intellij.ide.hierarchy.HierarchyTreeStructure
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.rescript.plugin.lang.psi.RescriptElementTypes
import com.rescript.plugin.lang.psi.RescriptFile

/**
 * Tree structure for the module nesting hierarchy.
 * Shows the nested module structure within a file.
 */
class RescriptModuleHierarchyTreeStructure(
    project: Project,
    element: PsiElement,
) : HierarchyTreeStructure(
        project,
        RescriptModuleHierarchyNodeDescriptor(project, null, element, true),
    ) {
    override fun buildChildren(descriptor: HierarchyNodeDescriptor): Array<Any> {
        val element = descriptor.psiElement ?: return emptyArray()

        val children =
            element.children.filter {
                it.node?.elementType == RescriptElementTypes.MODULE_DECLARATION
            }

        return children
            .map { child ->
                RescriptModuleHierarchyNodeDescriptor(
                    myProject,
                    descriptor,
                    child,
                    false,
                )
            }.toTypedArray()
    }
}

/**
 * Tree structure for module dependencies.
 * Shows modules referenced by open/include statements.
 */
class RescriptModuleDependencyTreeStructure(
    project: Project,
    element: PsiElement,
) : HierarchyTreeStructure(
        project,
        RescriptModuleHierarchyNodeDescriptor(project, null, element, true),
    ) {
    override fun buildChildren(descriptor: HierarchyNodeDescriptor): Array<Any> {
        val element = descriptor.psiElement ?: return emptyArray()

        // Only expand the root level (the file itself)
        if (element !is RescriptFile) return emptyArray()

        val references = RescriptDependencyAnalyzer.extractModuleReferences(element)

        return references
            .map { ref ->
                RescriptModuleHierarchyNodeDescriptor(
                    myProject,
                    descriptor,
                    ref.element,
                    false,
                )
            }.toTypedArray()
    }
}
