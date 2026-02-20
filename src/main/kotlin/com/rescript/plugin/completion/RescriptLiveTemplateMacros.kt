package com.rescript.plugin.completion

import com.intellij.codeInsight.template.Expression
import com.intellij.codeInsight.template.ExpressionContext
import com.intellij.codeInsight.template.Macro
import com.intellij.codeInsight.template.Result
import com.intellij.codeInsight.template.TextResult

/**
 * Live Template macro that resolves to the current ReScript module name.
 *
 * In ReScript, the module name is derived from the file name (e.g.,
 * `MyModule.res` -> `MyModule`). Use as `rescriptModuleName()` in
 * Live Template variable expressions.
 */
class RescriptModuleNameMacro : Macro() {
    override fun getName(): String = "rescriptModuleName"

    override fun getPresentableName(): String = "rescriptModuleName()"

    override fun calculateResult(
        params: Array<out Expression>,
        context: ExpressionContext?,
    ): Result? {
        val moduleName = resolveModuleName(context) ?: return null
        return TextResult(moduleName)
    }

    companion object {
        /**
         * Extracts the module name from the containing file.
         *
         * @param context the expression context providing access to the current file
         * @return the module name (file name without extension), or null if unavailable
         */
        fun resolveModuleName(context: ExpressionContext?): String? =
            context
                ?.psiElementAtStartOffset
                ?.containingFile
                ?.virtualFile
                ?.nameWithoutExtension
    }
}

/**
 * Live Template macro that resolves to the current React component name.
 *
 * In ReScript, a React component name equals the module name (file name).
 * Use as `rescriptComponentName()` in Live Template variable expressions.
 */
class RescriptComponentNameMacro : Macro() {
    override fun getName(): String = "rescriptComponentName"

    override fun getPresentableName(): String = "rescriptComponentName()"

    override fun calculateResult(
        params: Array<out Expression>,
        context: ExpressionContext?,
    ): Result? {
        val name = RescriptModuleNameMacro.resolveModuleName(context) ?: return null
        return TextResult(name)
    }
}
