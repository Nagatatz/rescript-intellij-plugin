package com.rescript.plugin.lsp

/**
 * Parses ReScript function signature strings and variant type definitions
 * from LSP hover responses.
 *
 * Used by multiple features including pipe chain hints, labeled args insertion,
 * case split, and argument conversion.
 *
 * @see RescriptLspUtils
 */
object RescriptLspSignatureParser {
    /**
     * Represents a labeled parameter parsed from a function signature.
     *
     * @param name the parameter label name (without ~)
     * @param type the parameter type annotation
     * @param isOptional true if the parameter has a default value (=?)
     */
    data class LabeledParam(
        val name: String,
        val type: String,
        val isOptional: Boolean,
    )

    // Match labeled params like ~name: type or ~name: type=?
    // Uses .+? to allow = in types (e.g., (int, string) => unit)
    private val LABELED_PARAM_PATTERN = Regex("""~(\w+)\s*:\s*(.+?)\s*(=\?)?\s*$""")

    // Match variant constructors like Name or Name(payload)
    private val CONSTRUCTOR_PATTERN = Regex("""^([A-Z]\w*)(?:\((.+)\))?$""")

    /**
     * Parses labeled parameters from a ReScript function signature string.
     *
     * Handles signatures like `(~name: string, ~age: int=?, unit) => person`.
     *
     * @param signature the function signature text
     * @return list of labeled parameters found in the signature
     */
    fun parseSignatureLabels(signature: String): List<LabeledParam> {
        val params = mutableListOf<LabeledParam>()

        // Extract parameter list between first ( and matching )
        val parenContent = extractParenContent(signature) ?: return params

        // Split by comma, respecting nested parens/brackets
        val parts = splitByComma(parenContent)

        for (part in parts) {
            val trimmed = part.trim()
            // Match ~label: type or ~label: type=?
            val match = LABELED_PARAM_PATTERN.find(trimmed)
            if (match != null) {
                params.add(
                    LabeledParam(
                        name = match.groupValues[1],
                        type = match.groupValues[2].trim(),
                        isOptional = match.groupValues[3].isNotEmpty(),
                    ),
                )
            }
        }

        return params
    }

    /** Information about a variant constructor. */
    data class VariantInfo(
        val name: String,
        val hasPayload: Boolean,
    )

    /**
     * Parses variant constructors from a type hover result.
     *
     * Used by case split to expand a variable into all constructors.
     *
     * @param typeText the type text from LSP hover (e.g., "option<int>" or "color")
     * @return list of constructor names with optional payload indicator, or empty if not a variant
     */
    fun parseVariantConstructors(typeText: String): List<VariantInfo> {
        val results = mutableListOf<VariantInfo>()

        // Handle built-in types
        if (typeText.startsWith("option<") || typeText == "option") {
            results.add(VariantInfo("Some", hasPayload = true))
            results.add(VariantInfo("None", hasPayload = false))
            return results
        }
        if (typeText.startsWith("result<") || typeText == "result") {
            results.add(VariantInfo("Ok", hasPayload = true))
            results.add(VariantInfo("Error", hasPayload = true))
            return results
        }

        // Parse inline variant definitions: | Constructor1 | Constructor2(payload)
        if (typeText.contains("|")) {
            val arms = typeText.split("|").map { it.trim() }.filter { it.isNotEmpty() }
            for (arm in arms) {
                val match = CONSTRUCTOR_PATTERN.find(arm)
                if (match != null) {
                    results.add(
                        VariantInfo(
                            name = match.groupValues[1],
                            hasPayload = match.groupValues[2].isNotEmpty(),
                        ),
                    )
                }
            }
        }

        return results
    }

    // ── Internal helpers ──────────────────────────────────────────────

    /** Extracts content between the first ( and its matching ). */
    internal fun extractParenContent(text: String): String? {
        val start = text.indexOf('(')
        if (start < 0) return null

        var depth = 0
        for (i in start until text.length) {
            when (text[i]) {
                '(' -> depth++
                ')' -> {
                    depth--
                    if (depth == 0) {
                        return text.substring(start + 1, i)
                    }
                }
            }
        }
        return null
    }

    /** Splits text by comma, respecting nested parentheses and angle brackets. */
    internal fun splitByComma(text: String): List<String> {
        val parts = mutableListOf<String>()
        var depth = 0
        var current = StringBuilder()

        for (ch in text) {
            when (ch) {
                '(', '<', '{', '[' -> {
                    depth++
                    current.append(ch)
                }
                ')', '>', '}', ']' -> {
                    depth--
                    current.append(ch)
                }
                ',' -> {
                    if (depth == 0) {
                        parts.add(current.toString())
                        current = StringBuilder()
                    } else {
                        current.append(ch)
                    }
                }
                else -> current.append(ch)
            }
        }

        if (current.isNotEmpty()) {
            parts.add(current.toString())
        }

        return parts
    }
}
