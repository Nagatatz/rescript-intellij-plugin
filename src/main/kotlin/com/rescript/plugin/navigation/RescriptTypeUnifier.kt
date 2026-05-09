package com.rescript.plugin.navigation

/**
 * Compares a parsed query type against a parsed candidate type and
 * returns the strongest [MatchScore] that holds.
 *
 * The unifier is what turns the existing substring-based ReScript Type
 * Signature Search into a Hoogle-style structural search:
 *
 * - `int => int` only matches functions whose actual signature is
 *   `int => int`, not `string => int`.
 * - Type variables in the **query** behave as wildcards that bind to
 *   anything in the candidate (with a slightly lower [TVAR_MATCH] score
 *   so exact matches win).
 * - A query that starts with `=>` (i.e. [RescriptTypeAst.ReturnQuery])
 *   only matches candidates that are arrows, by comparing the query's
 *   target against the arrow's right-hand side.
 *
 * The unifier is pure — every recursive helper takes ASTs in and
 * returns a `MatchScore` out — so the entire decision matrix lives in
 * unit tests.
 */
object RescriptTypeUnifier {
    /**
     * How well a query matched a candidate. Larger [weight] is a
     * better match; [MISMATCH] means "do not show this candidate".
     */
    enum class MatchScore(
        val weight: Int,
    ) {
        /** Structures match exactly with no type-variable substitution required. */
        EXACT(100),

        /** Structures match after binding query type-variables to concrete candidate types. */
        TVAR_MATCH(60),

        /** Only a sub-structure matches (e.g. the return position via `=> T`). */
        PARTIAL(30),

        /** Structures cannot be reconciled. */
        MISMATCH(0),
    }

    /**
     * Top-level entry point. Returns the strongest score produced by
     * matching [query] against [candidate]; the contributor sorts hits
     * by `MatchScore.weight` so users see exact matches first.
     */
    fun match(
        query: RescriptTypeAst,
        candidate: RescriptTypeAst,
    ): MatchScore {
        if (query is RescriptTypeAst.ReturnQuery) {
            if (candidate !is RescriptTypeAst.Arrow) return MatchScore.MISMATCH
            val inner = match(query.target, candidate.to)
            return demote(inner)
        }
        return matchStructural(query, candidate)
    }

    /**
     * Recursive structural compare. Type variables in [query] act as
     * wildcards (logging the substitution into [TVAR_MATCH]) — type
     * variables in [candidate] alone do not, so a user-supplied
     * concrete type doesn't accidentally match a polymorphic
     * candidate position.
     */
    private fun matchStructural(
        query: RescriptTypeAst,
        candidate: RescriptTypeAst,
    ): MatchScore {
        if (query is RescriptTypeAst.TypeVar) return MatchScore.TVAR_MATCH

        return when {
            query is RescriptTypeAst.UnitT && candidate is RescriptTypeAst.UnitT -> {
                MatchScore.EXACT
            }

            query is RescriptTypeAst.Ctor && candidate is RescriptTypeAst.Ctor -> {
                if (query.name == candidate.name) MatchScore.EXACT else MatchScore.MISMATCH
            }

            query is RescriptTypeAst.App && candidate is RescriptTypeAst.App -> {
                if (query.ctor != candidate.ctor || query.args.size != candidate.args.size) {
                    MatchScore.MISMATCH
                } else {
                    minScore(query.args.zip(candidate.args).map { (q, c) -> matchStructural(q, c) })
                }
            }

            query is RescriptTypeAst.Tuple && candidate is RescriptTypeAst.Tuple -> {
                if (query.elements.size != candidate.elements.size) {
                    MatchScore.MISMATCH
                } else {
                    minScore(query.elements.zip(candidate.elements).map { (q, c) -> matchStructural(q, c) })
                }
            }

            query is RescriptTypeAst.Arrow && candidate is RescriptTypeAst.Arrow -> {
                minScore(
                    listOf(
                        matchStructural(query.from, candidate.from),
                        matchStructural(query.to, candidate.to),
                    ),
                )
            }

            else -> {
                MatchScore.MISMATCH
            }
        }
    }

    /**
     * Selects the worst score in [scores] — a tuple of three matching
     * elements only counts as good as its weakest pair, so a single
     * mismatch fails the whole structure.
     */
    private fun minScore(scores: List<MatchScore>): MatchScore {
        if (scores.isEmpty()) return MatchScore.EXACT
        return scores.minBy { it.weight }
    }

    /**
     * Caps a sub-match score at [PARTIAL] — used when the
     * `=> T` query mode succeeded only because the right-hand side
     * matched, even if the matched sub-structure was an exact hit.
     */
    private fun demote(score: MatchScore): MatchScore =
        when (score) {
            MatchScore.EXACT -> MatchScore.PARTIAL
            MatchScore.TVAR_MATCH -> MatchScore.PARTIAL
            MatchScore.PARTIAL -> MatchScore.PARTIAL
            MatchScore.MISMATCH -> MatchScore.MISMATCH
        }
}
