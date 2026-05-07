package com.rescript.plugin.flow

import com.rescript.plugin.narrowing.RescriptSwitchArmCollector
import com.rescript.plugin.narrowing.SwitchArm

/**
 * One node in the variant-flow decision tree: a single arm of a
 * `switch` expression, optionally with nested arms that come from a
 * `switch` inside this arm's body.
 *
 * @property id stable, Mermaid-safe identifier for this node
 * @property patternSummary short pattern label (`Some(_)`, `None`, …)
 * @property bodyPreview first non-blank line of the arm body, capped
 *   at [BODY_PREVIEW_MAX_LENGTH] characters; empty when there is no
 *   meaningful body text (e.g. nested switch with no other code)
 * @property children child nodes for a nested `switch` inside this
 *   arm's body, or empty when the arm body is leaf code
 */
data class FlowNode(
    val id: String,
    val patternSummary: String,
    val bodyPreview: String,
    val children: List<FlowNode>,
)

/**
 * Top-level decision tree for one `switch` expression: the scrutinee
 * text drives a collection of arm nodes.
 *
 * @property scrutineeText source text of the scrutinee (the `X` in
 *   `switch X { ... }`), trimmed
 * @property arms top-level arms of the switch
 */
data class FlowDiagram(
    val scrutineeText: String,
    val arms: List<FlowNode>,
)

/**
 * Builds a [FlowDiagram] for the `switch` expression containing a
 * given offset, by reusing the arm boundaries already produced by
 * [RescriptSwitchArmCollector] and re-grouping them into a tree.
 *
 * Pure function (`(String, Int) → FlowDiagram?`); the IDE-facing
 * panel passes the editor document text and the caret offset and
 * renders whatever this returns.
 */
object RescriptVariantFlowModel {
    /** Maximum number of characters retained for an arm body preview. */
    internal const val BODY_PREVIEW_MAX_LENGTH = 40

    /**
     * Maximum nesting depth of switches that we walk into. Beyond this
     * the deeper branch is replaced by a single "(deeper switch hidden)"
     * leaf, both to limit Mermaid output size and to keep the panel
     * legible.
     */
    internal const val MAX_NESTING_DEPTH = 3

    /**
     * Returns the flow diagram for the innermost `switch` containing
     * [offset], or `null` when the offset is not inside any switch.
     *
     * @param source full text of the file
     * @param offset zero-based caret offset
     */
    fun buildAtOffset(
        source: String,
        offset: Int,
    ): FlowDiagram? {
        val allArms = RescriptSwitchArmCollector.collect(source)
        if (allArms.isEmpty()) return null

        val byScrutinee = allArms.groupBy { it.scrutineeRange }

        // Find the innermost switch whose span contains the offset.
        // We extend the lower bound to the start of the `switch`
        // keyword so that placing the caret on the keyword (or in
        // the whitespace between `switch` and the scrutinee) still
        // identifies the surrounding switch.
        val containing =
            byScrutinee.entries
                .mapNotNull { (range, arms) ->
                    val keywordStart = switchKeywordStart(source, range.startOffset)
                    val span = keywordStart..arms.last().bodyEndOffset
                    if (offset in span) Triple(range, arms, span.last - keywordStart) else null
                }.minByOrNull { it.third } ?: return null

        val (scrutineeRange, arms) = containing.first to containing.second
        val scrutineeText =
            source
                .substring(scrutineeRange.startOffset, scrutineeRange.endOffset)
                .trim()

        val flowArms =
            arms.mapIndexed { idx, arm ->
                buildArmNode(source, arm, allArms, "n$idx", depth = 0)
            }
        return FlowDiagram(scrutineeText = scrutineeText, arms = flowArms)
    }

    private fun buildArmNode(
        source: String,
        arm: SwitchArm,
        allArms: List<SwitchArm>,
        idPrefix: String,
        depth: Int,
    ): FlowNode {
        val nestedArms = nestedArmsOf(arm, allArms)
        val children =
            when {
                nestedArms.isEmpty() -> {
                    emptyList()
                }

                depth >= MAX_NESTING_DEPTH -> {
                    listOf(
                        FlowNode(
                            id = "${idPrefix}_truncated",
                            patternSummary = "(deeper switch hidden)",
                            bodyPreview = "",
                            children = emptyList(),
                        ),
                    )
                }

                else -> {
                    nestedArms.mapIndexed { idx, na ->
                        buildArmNode(source, na, allArms, "${idPrefix}_$idx", depth + 1)
                    }
                }
            }
        val bodyPreview = previewOf(source, arm.arrowOffset, arm.bodyEndOffset)
        return FlowNode(
            id = idPrefix,
            patternSummary = arm.patternSummary,
            bodyPreview = bodyPreview,
            children = children,
        )
    }

    /**
     * Returns the arms of the *outermost* nested switch sitting inside
     * [outer]'s body, or empty if there is none. Multiple sibling
     * nested switches are not modelled — only the first one is shown,
     * because Mermaid `flowchart` output works best as a tree.
     */
    private fun nestedArmsOf(
        outer: SwitchArm,
        allArms: List<SwitchArm>,
    ): List<SwitchArm> {
        val candidates =
            allArms.filter { otherArm ->
                otherArm !== outer &&
                    otherArm.scrutineeRange.startOffset in outer.arrowOffset..outer.bodyEndOffset
            }
        if (candidates.isEmpty()) return emptyList()
        // Group by scrutinee — each group is one nested switch — and
        // pick the one that starts earliest within the body.
        val groups = candidates.groupBy { it.scrutineeRange }
        val first = groups.entries.minByOrNull { it.key.startOffset } ?: return emptyList()
        return first.value
    }

    /**
     * Walks backward from [scrutineeStart] over whitespace to locate
     * the start offset of the preceding `switch` keyword. Falls back
     * to [scrutineeStart] when the keyword can't be found verbatim
     * (defensive — collector guarantees it's there).
     */
    private fun switchKeywordStart(
        source: String,
        scrutineeStart: Int,
    ): Int {
        var i = scrutineeStart - 1
        while (i >= 0 && source[i].isWhitespace()) i--
        val keywordEnd = i + 1
        val keywordStart = keywordEnd - SWITCH_KEYWORD.length
        return if (keywordStart >= 0 && source.regionMatches(keywordStart, SWITCH_KEYWORD, 0, SWITCH_KEYWORD.length)) {
            keywordStart
        } else {
            scrutineeStart
        }
    }

    private const val SWITCH_KEYWORD = "switch"

    private fun previewOf(
        source: String,
        from: Int,
        to: Int,
    ): String {
        if (from >= to) return ""
        val raw = source.substring(from, to).trim()
        val firstLine =
            raw
                .lineSequence()
                .map { it.trim() }
                .firstOrNull { it.isNotEmpty() }
                ?: ""
        return if (firstLine.length > BODY_PREVIEW_MAX_LENGTH) {
            firstLine.substring(0, BODY_PREVIEW_MAX_LENGTH - 1) + "…"
        } else {
            firstLine
        }
    }
}
