package com.rescript.plugin.generate

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction

/**
 * Generate action that creates a skeleton module implementation from
 * a `module type` declaration at the caret position.
 *
 * Parses the type signature to extract `let`, `type`, and nested `module`
 * declarations, then generates a concrete module with placeholder bodies
 * (e.g., `assert(false)` for functions, `unit` for abstract types).
 *
 * @see RescriptGenerateModuleTypeAction for the inverse operation
 * @see AnAction
 */
class RescriptGenerateModuleImplAction :
    AnAction("Module Implementation", "Generate module implementation from module type", null) {
    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        e.getData(CommonDataKeys.PSI_FILE) ?: return
        val document = editor.document
        val offset = editor.caretModel.offset

        // Find module type declaration in file text
        val text = document.text
        val moduleTypeInfo = findModuleTypeAtCaret(text, offset) ?: return

        val skeleton = generateSkeleton(moduleTypeInfo.name, moduleTypeInfo.declarations)

        WriteCommandAction.runWriteCommandAction(e.project) {
            val insertOffset = moduleTypeInfo.endOffset + 1
            val insertion = "\n\n$skeleton"
            document.insertString(
                insertOffset.coerceAtMost(document.textLength),
                insertion,
            )
        }
    }

    override fun update(e: AnActionEvent) {
        val editor =
            e.getData(CommonDataKeys.EDITOR) ?: run {
                e.presentation.isEnabled = false
                return
            }
        val document = editor.document
        val offset = editor.caretModel.offset
        e.presentation.isEnabled = findModuleTypeAtCaret(document.text, offset) != null
    }

    companion object {
        /** Parsed module type information. */
        data class ModuleTypeInfo(
            val name: String,
            val declarations: List<TypeDeclaration>,
            val endOffset: Int,
        )

        /** A single declaration within a module type. */
        data class TypeDeclaration(
            val kind: String,
            val name: String,
            val typeAnnotation: String,
        )

        // Pattern for `module type Name = {`
        private val MODULE_TYPE_PATTERN = Regex("""module\s+type\s+(\w+)\s*=\s*\{""")

        // Patterns for declarations inside the module type
        private val LET_DECL_PATTERN = Regex("""^\s*let\s+(\w+)\s*:\s*(.+)$""")
        private val TYPE_DECL_PATTERN = Regex("""^\s*type\s+(\w+)(.*)$""")
        private val MODULE_DECL_PATTERN = Regex("""^\s*module\s+(\w+)\s*:""")

        /**
         * Finds the module type declaration surrounding the given caret offset.
         *
         * @param text the full document text
         * @param caretOffset the caret position
         * @return parsed module type info, or null if not found
         */
        internal fun findModuleTypeAtCaret(
            text: String,
            caretOffset: Int,
        ): ModuleTypeInfo? {
            for (match in MODULE_TYPE_PATTERN.findAll(text)) {
                val startOffset = match.range.first
                val bodyStart = match.range.last + 1

                // Find matching closing brace
                var depth = 1
                var pos = bodyStart
                while (pos < text.length && depth > 0) {
                    when (text[pos]) {
                        '{' -> depth++
                        '}' -> depth--
                    }
                    pos++
                }
                val endOffset = pos - 1

                if (caretOffset in startOffset..endOffset) {
                    val body = text.substring(bodyStart, endOffset)
                    val declarations = parseModuleTypeBody(body)
                    return ModuleTypeInfo(match.groupValues[1], declarations, endOffset)
                }
            }
            return null
        }

        /**
         * Parses the body of a module type to extract declarations.
         *
         * @param body the text inside the module type braces
         * @return list of type declarations
         */
        internal fun parseModuleTypeBody(body: String): List<TypeDeclaration> {
            val declarations = mutableListOf<TypeDeclaration>()

            for (line in body.lines()) {
                val trimmed = line.trim()
                if (trimmed.isEmpty()) continue

                val letMatch = LET_DECL_PATTERN.find(trimmed)
                if (letMatch != null) {
                    declarations.add(
                        TypeDeclaration("let", letMatch.groupValues[1], letMatch.groupValues[2].trim()),
                    )
                    continue
                }

                val typeMatch = TYPE_DECL_PATTERN.find(trimmed)
                if (typeMatch != null) {
                    declarations.add(
                        TypeDeclaration("type", typeMatch.groupValues[1], typeMatch.groupValues[2].trim()),
                    )
                    continue
                }

                val moduleMatch = MODULE_DECL_PATTERN.find(trimmed)
                if (moduleMatch != null) {
                    declarations.add(
                        TypeDeclaration("module", moduleMatch.groupValues[1], ""),
                    )
                }
            }

            return declarations
        }

        /**
         * Generates a skeleton module implementation from a module type name and its declarations.
         *
         * @param name the module type name (used to derive the module name)
         * @param declarations the parsed type declarations
         * @return the skeleton module source text
         */
        internal fun generateSkeleton(
            name: String,
            declarations: List<TypeDeclaration>,
        ): String =
            buildString {
                // Derive module name from type name (strip "Type" suffix if present)
                val moduleName =
                    if (name.endsWith("Type")) {
                        name.removeSuffix("Type") + "Impl"
                    } else {
                        name + "Impl"
                    }

                appendLine("module $moduleName: $name = {")
                for (decl in declarations) {
                    when (decl.kind) {
                        "let" -> {
                            val impl = generateLetImpl(decl.typeAnnotation)
                            appendLine("  let ${decl.name}$impl")
                        }
                        "type" -> {
                            if (decl.typeAnnotation.isEmpty()) {
                                appendLine("  type ${decl.name} = unit")
                            } else {
                                appendLine("  type ${decl.name}${decl.typeAnnotation}")
                            }
                        }
                        "module" -> {
                            appendLine("  module ${decl.name} = {")
                            appendLine("  }")
                        }
                    }
                }
                append("}")
            }

        /**
         * Generates a let binding implementation based on the type annotation.
         *
         * @param typeAnnotation the type annotation from the module type
         * @return the implementation string (e.g., " = (a, b) => assert(false)")
         */
        internal fun generateLetImpl(typeAnnotation: String): String {
            // Check if it's a function type (contains =>)
            if (typeAnnotation.contains("=>")) {
                val parts = typeAnnotation.split("=>").map { it.trim() }
                if (parts.size >= 2) {
                    val paramsPart = parts.dropLast(1).joinToString(", ")
                    // Generate parameter names
                    val params = generateParamNames(paramsPart)
                    return " = ($params) => assert(false)"
                }
            }

            // Non-function type: provide a placeholder
            return when {
                typeAnnotation == "string" -> " = \"\""
                typeAnnotation == "int" -> " = 0"
                typeAnnotation == "float" -> " = 0.0"
                typeAnnotation == "bool" -> " = false"
                typeAnnotation == "unit" -> " = ()"
                typeAnnotation.startsWith("option<") -> " = None"
                typeAnnotation.startsWith("array<") -> " = []"
                else -> " = assert(false)"
            }
        }

        /**
         * Generates parameter names from a function parameter type string.
         *
         * @param paramsPart the parameter type string (e.g., "string, int")
         * @return the generated parameter names (e.g., "a, b")
         */
        internal fun generateParamNames(paramsPart: String): String {
            // Handle labeled arguments (~name: type)
            val labelPattern = Regex("""~(\w+)\s*:""")
            val labels = labelPattern.findAll(paramsPart).map { it.groupValues[1] }.toList()

            if (labels.isNotEmpty()) {
                return labels.joinToString(", ") { "~$it" }
            }

            // For positional params, generate a, b, c, ...
            val parts = paramsPart.split(",").filter { it.isNotBlank() }
            val names = ('a'..'z').take(parts.size)
            return names.joinToString(", ") { it.toString() }
        }
    }
}
