package com.rescript.plugin.navigation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Unit tests for [RescriptTypeUnifier.match].
 *
 * Each case parses a query and a candidate (via [RescriptTypeParser])
 * and asserts the resulting [RescriptTypeUnifier.MatchScore]. The
 * matrix exercises every score tier (EXACT / TVAR_MATCH / PARTIAL /
 * MISMATCH), the four AST families, and the special leading-`=>`
 * "return query" mode that turns into a sub-structure compare.
 */
class RescriptTypeUnifierTest {
    private fun match(
        query: String,
        candidate: String,
    ): RescriptTypeUnifier.MatchScore {
        val q = RescriptTypeParser.parse(query) ?: error("query parse failed: $query")
        val c = RescriptTypeParser.parse(candidate) ?: error("candidate parse failed: $candidate")
        return RescriptTypeUnifier.match(q, c)
    }

    // ── EXACT ──

    @Test
    fun `same ctor is EXACT`() = assertEquals(RescriptTypeUnifier.MatchScore.EXACT, match("int", "int"))

    @Test
    fun `same arrow is EXACT`() = assertEquals(RescriptTypeUnifier.MatchScore.EXACT, match("int => int", "int => int"))

    @Test
    fun `nested generic exact`() =
        assertEquals(
            RescriptTypeUnifier.MatchScore.EXACT,
            match("option<array<int>>", "option<array<int>>"),
        )

    @Test
    fun `same tuple is EXACT`() =
        assertEquals(RescriptTypeUnifier.MatchScore.EXACT, match("(int, string)", "(int, string)"))

    @Test
    fun `arrow with tuple from is EXACT`() =
        assertEquals(
            RescriptTypeUnifier.MatchScore.EXACT,
            match("(int, int) => int", "(int, int) => int"),
        )

    @Test
    fun `result of two args is EXACT`() =
        assertEquals(
            RescriptTypeUnifier.MatchScore.EXACT,
            match("result<int, string>", "result<int, string>"),
        )

    @Test
    fun `unit matches unit`() = assertEquals(RescriptTypeUnifier.MatchScore.EXACT, match("()", "()"))

    // ── TVAR_MATCH ──

    @Test
    fun `query type var matches concrete`() =
        assertEquals(RescriptTypeUnifier.MatchScore.TVAR_MATCH, match("'a", "int"))

    @Test
    fun `query polymorphic identity matches concrete identity`() =
        assertEquals(
            RescriptTypeUnifier.MatchScore.TVAR_MATCH,
            match("'a => 'a", "int => int"),
        )

    @Test
    fun `query option of any matches option of int`() =
        assertEquals(
            RescriptTypeUnifier.MatchScore.TVAR_MATCH,
            match("option<'a>", "option<int>"),
        )

    @Test
    fun `query type var matches another type var`() =
        assertEquals(
            RescriptTypeUnifier.MatchScore.TVAR_MATCH,
            match("'a", "'b"),
        )

    @Test
    fun `nested query tvar inside concrete`() =
        assertEquals(
            RescriptTypeUnifier.MatchScore.TVAR_MATCH,
            match("array<option<'a>>", "array<option<int>>"),
        )

    // ── PARTIAL via ReturnQuery ──

    @Test
    fun `return query exact match downgrades to PARTIAL`() =
        assertEquals(
            RescriptTypeUnifier.MatchScore.PARTIAL,
            match("=> int", "string => int"),
        )

    @Test
    fun `return query against generic`() =
        assertEquals(
            RescriptTypeUnifier.MatchScore.PARTIAL,
            match(
                "=> result<int, string>",
                "string => result<int, string>",
            ),
        )

    @Test
    fun `return query with tvar still PARTIAL`() =
        assertEquals(
            RescriptTypeUnifier.MatchScore.PARTIAL,
            match("=> 'a", "int => int"),
        )

    @Test
    fun `return query against non-arrow is MISMATCH`() =
        assertEquals(
            RescriptTypeUnifier.MatchScore.MISMATCH,
            match("=> int", "int"),
        )

    @Test
    fun `return query mismatched return type is MISMATCH`() =
        assertEquals(
            RescriptTypeUnifier.MatchScore.MISMATCH,
            match("=> string", "int => int"),
        )

    // ── MISMATCH ──

    @Test
    fun `different ctors mismatch`() = assertEquals(RescriptTypeUnifier.MatchScore.MISMATCH, match("int", "string"))

    @Test
    fun `different generic ctors mismatch`() =
        assertEquals(
            RescriptTypeUnifier.MatchScore.MISMATCH,
            match("option<int>", "array<int>"),
        )

    @Test
    fun `app with different arity mismatches`() =
        assertEquals(
            RescriptTypeUnifier.MatchScore.MISMATCH,
            match("result<int>", "result<int, string>"),
        )

    @Test
    fun `tuple with different arity mismatches`() =
        assertEquals(
            RescriptTypeUnifier.MatchScore.MISMATCH,
            match("(int, string)", "(int, string, bool)"),
        )

    @Test
    fun `arrow vs ctor mismatches`() =
        assertEquals(
            RescriptTypeUnifier.MatchScore.MISMATCH,
            match("int => int", "int"),
        )

    @Test
    fun `concrete query against polymorphic candidate mismatches`() =
        assertEquals(
            RescriptTypeUnifier.MatchScore.MISMATCH,
            match("int", "'a"),
        )

    @Test
    fun `concrete query at return position against tvar candidate mismatches`() =
        assertEquals(
            RescriptTypeUnifier.MatchScore.MISMATCH,
            match("int => string", "int => 'a"),
        )

    // ── Score is the weakest sub-score ──

    @Test
    fun `arrow with tvar in arg degrades to TVAR_MATCH`() =
        assertEquals(
            RescriptTypeUnifier.MatchScore.TVAR_MATCH,
            match("'a => int", "string => int"),
        )

    @Test
    fun `app where one arg is exact and another is tvar drops to TVAR_MATCH`() =
        assertEquals(
            RescriptTypeUnifier.MatchScore.TVAR_MATCH,
            match("result<int, 'b>", "result<int, string>"),
        )

    @Test
    fun `app where one arg mismatches is MISMATCH`() =
        assertEquals(
            RescriptTypeUnifier.MatchScore.MISMATCH,
            match("result<int, string>", "result<int, bool>"),
        )
}
