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
 * Local inspection that detects unnecessary `ref` usage in ReScript code.
 *
 * Scans let bindings for `ref(...)` initializers and warns when the
 * mutable reference is never reassigned (`:=`) within the same file,
 * suggesting removal of the unnecessary `ref` wrapper.
 *
 * Provides a quick fix to replace `ref(value)` with `value`.
 *
 * Implements [LocalInspectionTool] for the IntelliJ inspection framework.
 */
class RescriptMutabilityInspection : LocalInspectionTool() {
    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
    ): PsiElementVisitor =
        object : PsiElementVisitor() {
            override fun visitFile(file: PsiFile) {
                if (file !is RescriptFile) return
                val text = file.text
                val refs = findRefBindings(text)
                val reassignments = findReassignments(text)

                for (ref in refs) {
                    if (ref.name !in reassignments) {
                        val element = file.findElementAt(ref.offset)
                        if (element != null) {
                            holder.registerProblem(
                                element,
                                "Mutable ref '${ref.name}' is never reassigned — consider using a plain let binding",
                                RemoveRefQuickFix(ref.value),
                            )
                        }
                    }
                }
            }
        }

    /** Quick fix that replaces `let name = ref(value)` with `let name = value`. */
    private class RemoveRefQuickFix(
        private val value: String,
    ) : LocalQuickFix {
        override fun getFamilyName(): String = "Remove unnecessary ref"

        override fun getName(): String = "Replace ref($value) with $value"

        override fun applyFix(
            project: Project,
            descriptor: ProblemDescriptor,
        ) {
            val file = descriptor.psiElement?.containingFile ?: return
            val document = file.viewProvider.document ?: return
            val text = document.text
            // Find the ref pattern on the same line as the element
            val elementOffset = descriptor.psiElement?.textRange?.startOffset ?: return
            val lineNumber = document.getLineNumber(elementOffset)
            val lineStart = document.getLineStartOffset(lineNumber)
            val lineEnd = document.getLineEndOffset(lineNumber)
            val line = text.substring(lineStart, lineEnd)

            val replaced = removeRef(line)
            if (replaced != null && replaced != line) {
                document.replaceString(lineStart, lineEnd, replaced)
            }
        }
    }

    companion object {
        // Matches: let name = ref(value)
        internal val REF_BINDING_PATTERN =
            Regex("""let\s+(\w+)\s*=\s*ref\((.+?)\)""")

        // Matches: name :=
        internal val REASSIGNMENT_PATTERN =
            Regex("""(\w+)\s*:=""")

        /** Pattern matching `ref(...)` for removal during quick fix. */
        private val REF_REMOVAL_PATTERN = Regex("""ref\(.+?\)""")

        /**
         * Represents a ref binding found in source text.
         *
         * @property name the variable name
         * @property value the value inside ref(...)
         * @property offset the character offset where the binding starts
         */
        data class RefBinding(
            val name: String,
            val value: String,
            val offset: Int,
        )

        /**
         * Finds all `let name = ref(value)` bindings in the text.
         *
         * @param text the source text to scan
         * @return list of ref bindings found
         */
        internal fun findRefBindings(text: String): List<RefBinding> =
            REF_BINDING_PATTERN
                .findAll(text)
                .map { match ->
                    RefBinding(
                        name = match.groupValues[1],
                        value = match.groupValues[2],
                        offset = match.range.first,
                    )
                }.toList()

        /**
         * Finds all variable names that have `:=` reassignment in the text.
         *
         * @param text the source text to scan
         * @return set of variable names that are reassigned
         */
        internal fun findReassignments(text: String): Set<String> =
            REASSIGNMENT_PATTERN.findAll(text).map { it.groupValues[1] }.toSet()

        /**
         * Removes the `ref(...)` wrapper from a line, keeping just the value.
         *
         * @param line the source line containing `let name = ref(value)`
         * @return the line with ref removed, or null if no match
         */
        internal fun removeRef(line: String): String? {
            val match = REF_BINDING_PATTERN.find(line) ?: return null
            val value = match.groupValues[2]
            return line.replaceFirst(REF_REMOVAL_PATTERN, value)
        }
    }
}
