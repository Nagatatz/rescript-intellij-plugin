package com.rescript.plugin.navigation

/**
 * AST node for the small subset of ReScript type expressions that
 * `RescriptTypeParser` understands and `RescriptTypeUnifier` matches
 * against. The hierarchy is intentionally narrow — just enough to model
 * arrows, tuples, type applications, type variables and the `unit`
 * literal — so the search contributor can rank candidates structurally
 * without depending on the language server.
 *
 * Records, polymorphic variants, labeled arguments, and other ReScript
 * type forms are deferred to a later iteration.
 */
sealed class RescriptTypeAst {
    /** The `unit` / `()` type. */
    object UnitT : RescriptTypeAst()

    /**
     * Plain named type without arguments — `int`, `string`, a project-
     * defined `User` etc. Capturing the lowercase / uppercase form is
     * left to the caller so the unifier can compare names verbatim.
     */
    data class Ctor(
        val name: String,
    ) : RescriptTypeAst()

    /** Type variable like `'a` (`name` excludes the leading apostrophe). */
    data class TypeVar(
        val name: String,
    ) : RescriptTypeAst()

    /** Type application: `option<int>`, `array<option<int>>`, etc. */
    data class App(
        val ctor: String,
        val args: List<RescriptTypeAst>,
    ) : RescriptTypeAst()

    /** Tuple `(a, b, c)`. Always at least two elements at construction. */
    data class Tuple(
        val elements: List<RescriptTypeAst>,
    ) : RescriptTypeAst()

    /** Function arrow `from => to`. Right-associative at parse time. */
    data class Arrow(
        val from: RescriptTypeAst,
        val to: RescriptTypeAst,
    ) : RescriptTypeAst()

    /**
     * Query-only node: produced when the user types a leading `=>` to
     * search for "functions returning T". Never appears in candidate
     * ASTs — the unifier consumes it in a special arm that compares
     * [target] against the right-hand side of an arrow candidate.
     */
    data class ReturnQuery(
        val target: RescriptTypeAst,
    ) : RescriptTypeAst()
}
