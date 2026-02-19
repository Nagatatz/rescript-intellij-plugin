package com.rescript.plugin.structure

import com.intellij.ide.structureView.StructureViewBuilder
import com.intellij.ide.structureView.StructureViewModel
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder
import com.intellij.lang.PsiStructureViewFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile

/**
 * Factory that creates the Structure View for ReScript files.
 *
 * Provides a tree-based view of top-level declarations (let, type, module, etc.)
 * accessible via the Structure tool window.
 */
class RescriptStructureViewFactory : PsiStructureViewFactory {
    override fun getStructureViewBuilder(psiFile: PsiFile): StructureViewBuilder =
        object : TreeBasedStructureViewBuilder() {
            override fun createStructureViewModel(editor: Editor?): StructureViewModel =
                RescriptStructureViewModel(psiFile, editor)
        }
}
