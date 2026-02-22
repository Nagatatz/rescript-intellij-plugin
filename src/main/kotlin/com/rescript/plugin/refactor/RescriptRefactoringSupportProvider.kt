package com.rescript.plugin.refactor

import com.intellij.lang.refactoring.RefactoringSupportProvider
import com.intellij.psi.PsiElement

/**
 * Registers refactoring support for the ReScript language.
 *
 * Provides the Introduce Variable handler (`Ctrl+Alt+V`) and enables
 * Safe Delete for ReScript elements. Registered via `lang.refactoringSupport`
 * extension point in `plugin.xml`.
 *
 * @see RescriptExtractVariableHandler for the Introduce Variable implementation
 * @see RescriptSafeDeleteProcessor for Safe Delete support
 */
class RescriptRefactoringSupportProvider : RefactoringSupportProvider() {
    override fun getIntroduceVariableHandler() = RescriptExtractVariableHandler()

    override fun isSafeDeleteAvailable(element: PsiElement) = true
}
