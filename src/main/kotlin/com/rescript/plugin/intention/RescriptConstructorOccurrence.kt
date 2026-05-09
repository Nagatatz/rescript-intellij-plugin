package com.rescript.plugin.intention

import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile

/**
 * Token-level shape of how a UIDENT is being used at a particular
 * source offset, as classified by [RescriptConstructorOccurrenceClassifier].
 *
 * Only [CONSTRUCTOR], [PATTERN] and [MODULE_QUALIFIED_TAIL] participate in
 * the project-wide rename — [OTHER] is the explicit "leave this token
 * alone" answer for callers that need to know the token wasn't a
 * constructor (e.g. a JSX element name or a stray UIDENT inside a string).
 */
enum class ConstructorOccurrenceKind {
    /** Constructor invocation, e.g. `Foo(x)` or a 0-ary `Foo` after `=` / `,`. */
    CONSTRUCTOR,

    /** Pattern in a `switch` arm or `type` declaration arm, e.g. `| Foo(_)`. */
    PATTERN,

    /** Tail of a module-qualified path, e.g. the `Foo` in `Module.Foo`. */
    MODULE_QUALIFIED_TAIL,

    /** Anything else (JSX element name, type-position UIDENT, comment, …). */
    OTHER,
}

/**
 * One match returned by [RescriptConstructorOccurrenceFinder]; carries the
 * file, the exact text range covering the UIDENT (so a write action can
 * splice in the new name), and the classifier verdict.
 *
 * [range] is always a [TextRange] over the UIDENT itself, never including
 * a leading `Module.` qualifier — `MODULE_QUALIFIED_TAIL` matches still
 * point at just the trailing constructor name.
 */
data class RescriptConstructorOccurrence(
    val file: VirtualFile,
    val range: TextRange,
    val kind: ConstructorOccurrenceKind,
)
