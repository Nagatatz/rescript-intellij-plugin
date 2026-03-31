package com.rescript.plugin.refactor

import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.SuggestedNameInfo
import com.intellij.refactoring.rename.NameSuggestionProvider
import com.rescript.plugin.lang.psi.RescriptElementTypes
import com.rescript.plugin.lang.psi.RescriptFile
import com.rescript.plugin.lang.psi.RescriptPsiUtils

/**
 * Provides name suggestions during rename refactoring for ReScript elements.
 *
 * Generates context-aware name candidates based on:
 * - Element type: module declarations suggest "myModule", types suggest "myType"
 * - Existing name: variations like camelCase and adding/removing prefixes
 * - File name: derives suggestions from the containing file name
 *
 * When LSP is available, type-based suggestions could be enhanced with hover information
 * (e.g., `User.t` → "user", `array<Item.t>` → "items").
 */
class RescriptNameSuggestionProvider : NameSuggestionProvider {
    override fun getSuggestedNames(
        element: PsiElement,
        nameSuggestionContext: PsiElement?,
        result: MutableSet<String>,
    ): SuggestedNameInfo? {
        if (element.containingFile !is RescriptFile) return null

        val currentName = RescriptPsiUtils.extractName(element)
        if (currentName == "(anonymous)" || currentName == "(unknown)") return null

        val elementType = element.node?.elementType

        // Add type-based suggestions
        when (elementType) {
            RescriptElementTypes.MODULE_DECLARATION -> {
                result.add(currentName)
                result.add("my$currentName")
            }

            RescriptElementTypes.TYPE_DECLARATION -> {
                result.add(currentName)
                result.add("${currentName}Type")
            }

            RescriptElementTypes.LET_DECLARATION -> {
                result.add(currentName)
                // Suggest camelCase variations
                if (currentName.contains("_")) {
                    result.add(snakeToCamelCase(currentName))
                }
            }

            RescriptElementTypes.EXCEPTION_DECLARATION -> {
                result.add(currentName)
                if (!currentName.endsWith("Error")) {
                    result.add("${currentName}Error")
                }
            }

            else -> {
                result.add(currentName)
            }
        }

        // Add file-name-based suggestion
        val fileName = element.containingFile?.virtualFile?.nameWithoutExtension
        if (fileName != null && fileName != currentName) {
            result.add(toCamelCase(fileName))
        }

        return SuggestedNameInfo.NULL_INFO
    }

    companion object {
        /**
         * Converts a snake_case string to camelCase.
         *
         * @param name the snake_case name (e.g., "my_variable")
         * @return the camelCase version (e.g., "myVariable")
         */
        internal fun snakeToCamelCase(name: String): String {
            val parts = name.split("_")
            return parts.first() +
                parts.drop(1).joinToString("") { part ->
                    part.replaceFirstChar { it.uppercaseChar() }
                }
        }

        /**
         * Converts a PascalCase or hyphenated string to camelCase.
         *
         * @param name the input name (e.g., "UserProfile" or "user-profile")
         * @return the camelCase version (e.g., "userProfile")
         */
        internal fun toCamelCase(name: String): String {
            // Handle hyphenated names
            if (name.contains("-")) {
                val parts = name.split("-")
                return parts.first().lowercase() +
                    parts.drop(1).joinToString("") { part ->
                        part.replaceFirstChar { it.uppercaseChar() }
                    }
            }
            // Handle PascalCase → camelCase
            return name.replaceFirstChar { it.lowercaseChar() }
        }
    }
}
