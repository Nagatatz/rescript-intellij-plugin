package com.rescript.plugin.refactor

import com.intellij.lang.refactoring.NamesValidator
import com.intellij.openapi.project.Project

/**
 * Validates identifiers and keywords for the ReScript language.
 *
 * ReScript has two kinds of identifiers:
 * - lident: starts with lowercase letter or underscore (variables, functions, fields)
 * - uident: starts with uppercase letter (modules, variants, exceptions)
 */
class RescriptNamesValidator : NamesValidator {
    override fun isIdentifier(
        name: String,
        project: Project?,
    ): Boolean {
        if (name.isEmpty()) return false
        return LIDENT_REGEX.matches(name) || UIDENT_REGEX.matches(name)
    }

    override fun isKeyword(
        name: String,
        project: Project?,
    ): Boolean = name in KEYWORDS

    companion object {
        private val LIDENT_REGEX = Regex("^[a-z_][a-zA-Z0-9_']*$")
        private val UIDENT_REGEX = Regex("^[A-Z][a-zA-Z0-9_']*$")

        private val KEYWORDS =
            setOf(
                "and",
                "as",
                "assert",
                "async",
                "await",
                "begin",
                "catch",
                "class",
                "constraint",
                "do",
                "done",
                "downto",
                "else",
                "end",
                "exception",
                "external",
                "false",
                "for",
                "functor",
                "if",
                "in",
                "include",
                "inherit",
                "initializer",
                "lazy",
                "let",
                "list",
                "module",
                "mutable",
                "new",
                "nonrec",
                "object",
                "of",
                "open",
                "or",
                "pri",
                "pub",
                "raw",
                "rec",
                "ref",
                "raise",
                "sig",
                "struct",
                "switch",
                "then",
                "true",
                "try",
                "type",
                "unpack",
                "val",
                "virtual",
                "when",
                "while",
                "with",
            )
    }
}
