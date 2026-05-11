package com.rescript.plugin.navigation

/**
 * Pulls the binding name and explicit `: T = …` annotation out of a
 * top-level ReScript declaration's source text. Used by
 * [RescriptTypeSignatureSearchContributor] to feed the signature
 * portion into [RescriptTypeParser] before unifying against the
 * user's query.
 *
 * The extractor is pure (`String → ParsedDeclaration?`) so the test
 * matrix runs without an IntelliJ Platform fixture. Inputs that don't
 * carry an explicit annotation (`let x = 5`) or don't fit the simple
 * `let|external|type [rec] name: …` grammar yield `null`, which the
 * contributor treats as "ineligible candidate".
 */
object RescriptDeclarationSignatureExtractor {
    /**
     * Result of pulling the binding name and explicit `: T = …`
     * annotation out of a top-level declaration's source text.
     *
     * @property name the binding name (e.g. `map`)
     * @property nameOffset offset of [name] inside the declaration's
     *   own source text — the contributor adds the parent's start
     *   offset before navigating
     * @property signatureText the trimmed signature text suitable
     *   both for the AST parser and the renderer
     */
    data class ParsedDeclaration(
        val name: String,
        val nameOffset: Int,
        val signatureText: String,
    )

    /**
     * Pulls `let name: signature = …` / `external name: signature = …`
     * apart so the contributor can feed [ParsedDeclaration.signatureText]
     * into the type parser. Returns `null` when no `:` annotation is
     * present (the candidate has only an inferred type) or the form
     * doesn't fit the simple grammar.
     */
    fun parseDeclaration(declarationText: String): ParsedDeclaration? {
        val headerMatch = DECLARATION_HEADER.find(declarationText) ?: return null
        val name = headerMatch.groupValues[1]
        val afterColon = declarationText.substring(headerMatch.range.last + 1)
        val bindingEqualsOffset = findBindingEquals(afterColon)
        val rawSignature =
            if (bindingEqualsOffset >= 0) {
                afterColon.substring(0, bindingEqualsOffset)
            } else {
                afterColon
            }
        val signatureText = rawSignature.trim()
        if (signatureText.isEmpty()) return null
        return ParsedDeclaration(
            name = name,
            nameOffset = headerMatch.range.first + headerMatch.value.indexOf(name),
            signatureText = signatureText,
        )
    }

    /**
     * Finds the offset of the first depth-0 binding `=` in [text]
     * (i.e. an `=` that is not part of `=>` or `==` and is not
     * inside parens / angles / braces / brackets). Returns -1 if
     * no such `=` exists.
     */
    fun findBindingEquals(text: String): Int {
        var depth = 0
        var i = 0
        while (i < text.length) {
            val c = text[i]
            when (c) {
                '(', '<', '{', '[' -> {
                    depth++
                }

                ')', '>', '}', ']' -> {
                    if (depth > 0) depth--
                }

                '=' -> {
                    val next = if (i + 1 < text.length) text[i + 1] else ' '
                    if (depth == 0 && next != '>' && next != '=') return i
                }

                '\n' -> {
                    if (depth == 0) return i
                }

                else -> {
                    Unit
                }
            }
            i++
        }
        return -1
    }

    /**
     * Matches the leading `let|external|type [rec] name:` portion;
     * the signature is parsed separately so we can stop at the
     * first **binding** `=` rather than the `=` that's part of `=>`.
     */
    private val DECLARATION_HEADER =
        Regex(
            """^(?:let|external|type)\s+(?:rec\s+)?(\w[\w']*)\s*:""",
            RegexOption.MULTILINE,
        )
}
