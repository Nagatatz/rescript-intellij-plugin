package com.rescript.plugin.refactor

import com.intellij.lang.refactoring.RefactoringSupportProvider
import com.intellij.psi.PsiElement
import com.intellij.refactoring.RefactoringActionHandler

/**
 * Registers refactoring support for the ReScript language.
 *
 * Provides the Introduce Variable handler (`Ctrl+Alt+V`), Introduce Constant
 * handler, and enables Safe Delete for ReScript elements. Registered via
 * `lang.refactoringSupport` extension point in `plugin.xml`.
 *
 * @see RescriptExtractVariableHandler for the Introduce Variable implementation
 * @see RescriptIntroduceConstantHandler for the Introduce Constant implementation
 * @see RescriptSafeDeleteProcessor for Safe Delete support
 */
class RescriptRefactoringSupportProvider : RefactoringSupportProvider() {
    override fun getIntroduceVariableHandler() = RescriptExtractVariableHandler()

    override fun getIntroduceConstantHandler(): RefactoringActionHandler = RescriptIntroduceConstantHandler()

    override fun isSafeDeleteAvailable(element: PsiElement) = true
}
