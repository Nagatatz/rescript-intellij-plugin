package com.rescript.plugin.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.rescript.plugin.lang.psi.RescriptFile

/**
 * Local inspection that detects common ReScript style issues and
 * suggests idiomatic improvements.
 *
 * Checks three rules:
 * 1. **Redundant boolean expression:** `if cond { true } else { false }` can be simplified to `cond`
 * 2. **Belt.* usage:** `Belt.Array.map` etc. should use the modern `Array.map` API
 * 3. **Boolean switch:** `switch x { | true => a | false => b }` should use `if x { a } else { b }`
 *
 * Each rule provides a [LocalQuickFix] for automatic correction.
 *
 * Implements [LocalInspectionTool] for the IntelliJ inspection framework.
 */
class RescriptStyleLintInspection : LocalInspectionTool() {
    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
    ): PsiElementVisitor =
        object : PsiElementVisitor() {
            override fun visitFile(file: PsiFile) {
                if (file !is RescriptFile) return
                val text = file.text

                checkRedundantBooleans(text, file, holder)
                checkBeltUsage(text, file, holder)
                checkBooleanSwitch(text, file, holder)
            }
        }

    private fun checkRedundantBooleans(
        text: String,
        file: PsiFile,
        holder: ProblemsHolder,
    ) {
        for (match in REDUNDANT_BOOL_PATTERN.findAll(text)) {
            val element = file.findElementAt(match.range.first) ?: continue
            holder.registerProblem(
                element,
                "Redundant boolean expression — simplify to the condition itself",
                SimplifyBooleanQuickFix(),
            )
        }
    }

    private fun checkBeltUsage(
        text: String,
        file: PsiFile,
        holder: ProblemsHolder,
    ) {
        for (match in BELT_USAGE_PATTERN.findAll(text)) {
            val element = file.findElementAt(match.range.first) ?: continue
            val modernApi = match.groupValues[1]
            holder.registerProblem(
                element,
                "Belt is deprecated — use $modernApi instead",
                ReplaceBeltQuickFix(),
            )
        }
    }

    private fun checkBooleanSwitch(
        text: String,
        file: PsiFile,
        holder: ProblemsHolder,
    ) {
        for (match in BOOLEAN_SWITCH_PATTERN.findAll(text)) {
            val element = file.findElementAt(match.range.first) ?: continue
            holder.registerProblem(
                element,
                "Boolean switch can be simplified to an if expression",
                SimplifyBooleanSwitchQuickFix(),
            )
        }
    }

    /** Quick fix that simplifies `if cond { true } else { false }` to `cond`. */
    private class SimplifyBooleanQuickFix : LocalQuickFix {
        override fun getFamilyName(): String = "Simplify redundant boolean expression"

        override fun applyFix(
            project: Project,
            descriptor: ProblemDescriptor,
        ) {
            val file = descriptor.psiElement?.containingFile ?: return
            val document = file.viewProvider.document ?: return
            val offset = descriptor.psiElement?.textRange?.startOffset ?: return
            val text = document.text
            val match = REDUNDANT_BOOL_PATTERN.find(text, offset) ?: return
            if (match.range.first != offset) return
            val condition = match.groupValues[1].trim()
            document.replaceString(match.range.first, match.range.last + 1, condition)
        }
    }

    /** Quick fix that replaces `Belt.Module.fn` with `Module.fn`. */
    private class ReplaceBeltQuickFix : LocalQuickFix {
        override fun getFamilyName(): String = "Replace Belt.* with modern API"

        override fun applyFix(
            project: Project,
            descriptor: ProblemDescriptor,
        ) {
            val file = descriptor.psiElement?.containingFile ?: return
            val document = file.viewProvider.document ?: return
            val offset = descriptor.psiElement?.textRange?.startOffset ?: return
            val text = document.text
            val match = BELT_USAGE_PATTERN.find(text, offset) ?: return
            if (match.range.first != offset) return
            val replacement = match.groupValues[1]
            document.replaceString(match.range.first, match.range.last + 1, replacement)
        }
    }

    /** Quick fix that converts boolean switch to if expression. */
    private class SimplifyBooleanSwitchQuickFix : LocalQuickFix {
        override fun getFamilyName(): String = "Convert boolean switch to if expression"

        override fun applyFix(
            project: Project,
            descriptor: ProblemDescriptor,
        ) {
            val file = descriptor.psiElement?.containingFile ?: return
            val document = file.viewProvider.document ?: return
            val offset = descriptor.psiElement?.textRange?.startOffset ?: return
            val text = document.text
            val match = BOOLEAN_SWITCH_PATTERN.find(text, offset) ?: return
            if (match.range.first != offset) return
            val converted = convertBooleanSwitch(match.value)
            if (converted != null) {
                document.replaceString(match.range.first, match.range.last + 1, converted)
            }
        }
    }

    companion object {
        // Matches: if cond { true } else { false }
        internal val REDUNDANT_BOOL_PATTERN =
            Regex("""if\s+(.+?)\s*\{\s*true\s*}\s*else\s*\{\s*false\s*}""")

        // Matches: Belt.Array.map, Belt.List.filter, etc.
        internal val BELT_USAGE_PATTERN =
            Regex("""Belt\.(Array|List|Map|Set|Option|Result|Int|Float|String)\.(\w+)""")

        // Matches: switch expr { | true => a | false => b }
        internal val BOOLEAN_SWITCH_PATTERN =
            Regex("""switch\s+(\w+)\s*\{\s*\|\s*true\s*=>\s*(.+?)\s*\|\s*false\s*=>\s*(.+?)\s*}""")

        /**
         * Simplifies a redundant boolean `if` to just the condition.
         *
         * @param text the text containing `if cond { true } else { false }`
         * @return the simplified condition, or null if no match
         */
        internal fun simplifyRedundantBoolean(text: String): String? {
            val match = REDUNDANT_BOOL_PATTERN.find(text) ?: return null
            return match.groupValues[1].trim()
        }

        /**
         * Replaces a `Belt.*` usage with the modern API equivalent.
         *
         * @param text the text containing `Belt.Module.fn`
         * @return the replacement with Belt prefix removed, or null if no match
         */
        internal fun replaceBelt(text: String): String? {
            val match = BELT_USAGE_PATTERN.find(text) ?: return null
            return match.groupValues[1] + "." + match.groupValues[2]
        }

        /**
         * Converts a boolean switch to an if expression.
         *
         * @param text the text containing `switch x { | true => a | false => b }`
         * @return the converted if expression, or null if no match
         */
        internal fun convertBooleanSwitch(text: String): String? {
            val match = BOOLEAN_SWITCH_PATTERN.find(text) ?: return null
            val expr = match.groupValues[1].trim()
            val trueCase = match.groupValues[2].trim()
            val falseCase = match.groupValues[3].trim()
            return "if $expr { $trueCase } else { $falseCase }"
        }
    }
}
