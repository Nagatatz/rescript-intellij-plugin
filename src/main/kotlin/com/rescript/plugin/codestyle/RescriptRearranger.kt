package com.rescript.plugin.codestyle

import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.arrangement.ArrangementEntry
import com.intellij.psi.codeStyle.arrangement.ArrangementSettings
import com.intellij.psi.codeStyle.arrangement.ArrangementSettingsSerializer
import com.intellij.psi.codeStyle.arrangement.DefaultArrangementSettingsSerializer
import com.intellij.psi.codeStyle.arrangement.Rearranger
import com.intellij.psi.codeStyle.arrangement.match.ArrangementEntryMatcher
import com.intellij.psi.codeStyle.arrangement.match.StdArrangementEntryMatcher
import com.intellij.psi.codeStyle.arrangement.match.StdArrangementMatchRule
import com.intellij.psi.codeStyle.arrangement.model.ArrangementAtomMatchCondition
import com.intellij.psi.codeStyle.arrangement.model.ArrangementMatchCondition
import com.intellij.psi.codeStyle.arrangement.std.ArrangementSettingsToken
import com.intellij.psi.codeStyle.arrangement.std.ArrangementStandardSettingsAware
import com.intellij.psi.codeStyle.arrangement.std.CompositeArrangementSettingsToken
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementSettings
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.lang.psi.RescriptElementTypes

/**
 * Code rearranger that sorts top-level declarations in ReScript files
 * according to a canonical ordering.
 *
 * The default order is:
 * 1. `open` / `include` statements
 * 2. `type` declarations
 * 3. `exception` declarations
 * 4. `module` declarations
 * 5. `external` declarations
 * 6. `let` declarations
 *
 * Triggered via Code > Rearrange Code.
 *
 * Implements [Rearranger] and [ArrangementStandardSettingsAware] for
 * the IntelliJ code arrangement framework.
 */
class RescriptRearranger :
    Rearranger<RescriptArrangementEntry>,
    ArrangementStandardSettingsAware {
    override fun parse(
        root: PsiElement,
        document: Document?,
        ranges: MutableCollection<out TextRange>,
        settings: ArrangementSettings,
    ): MutableList<RescriptArrangementEntry> {
        val entries = mutableListOf<RescriptArrangementEntry>()

        for (child in root.children) {
            val elementType = child.node?.elementType ?: continue
            val category = classifyElement(elementType) ?: continue
            val range = child.textRange

            // Only include elements within the requested ranges
            if (ranges.any { it.intersects(range) }) {
                entries.add(RescriptArrangementEntry(child, range, category))
            }
        }

        return entries
    }

    override fun parseWithNew(
        root: PsiElement,
        document: Document?,
        ranges: MutableCollection<out TextRange>,
        element: PsiElement,
        settings: ArrangementSettings,
    ): Pair<RescriptArrangementEntry, MutableList<RescriptArrangementEntry>>? = null

    override fun getBlankLines(
        settings: CodeStyleSettings,
        parent: RescriptArrangementEntry?,
        previous: RescriptArrangementEntry?,
        target: RescriptArrangementEntry,
    ): Int = -1 // Use default blank line handling

    override fun getSerializer(): ArrangementSettingsSerializer =
        DefaultArrangementSettingsSerializer(getDefaultSettings())

    override fun getDefaultSettings(): StdArrangementSettings {
        val rules =
            DEFAULT_ORDER.map {
                StdArrangementMatchRule(
                    StdArrangementEntryMatcher(
                        ArrangementAtomMatchCondition(StdArrangementTokens.EntryType.FIELD),
                    ),
                    StdArrangementTokens.Order.BY_NAME,
                )
            }

        return StdArrangementSettings.createByMatchRules(
            emptyList(),
            rules,
        )
    }

    override fun getSupportedGroupingTokens(): MutableList<CompositeArrangementSettingsToken>? = null

    override fun getSupportedMatchingTokens(): MutableList<CompositeArrangementSettingsToken>? = null

    override fun isEnabled(
        token: ArrangementSettingsToken,
        current: ArrangementMatchCondition?,
    ): Boolean = true

    override fun buildMatcher(condition: ArrangementMatchCondition): ArrangementEntryMatcher =
        StdArrangementEntryMatcher(
            ArrangementAtomMatchCondition(StdArrangementTokens.EntryType.FIELD),
        )

    override fun getMutexes(): MutableCollection<MutableSet<ArrangementSettingsToken>> = mutableListOf()

    companion object {
        /** Canonical ordering of ReScript top-level declaration categories. */
        internal val DEFAULT_ORDER =
            listOf(
                DeclarationCategory.OPEN_INCLUDE,
                DeclarationCategory.TYPE,
                DeclarationCategory.EXCEPTION,
                DeclarationCategory.MODULE,
                DeclarationCategory.EXTERNAL,
                DeclarationCategory.LET,
            )

        /**
         * Maps a PSI element type to its declaration category.
         *
         * @param elementType the PSI element type to classify
         * @return the category, or null if the element is not a recognized declaration
         */
        internal fun classifyElement(elementType: IElementType): DeclarationCategory? =
            when (elementType) {
                RescriptElementTypes.OPEN_STATEMENT, RescriptElementTypes.INCLUDE_STATEMENT ->
                    DeclarationCategory.OPEN_INCLUDE
                RescriptElementTypes.TYPE_DECLARATION -> DeclarationCategory.TYPE
                RescriptElementTypes.EXCEPTION_DECLARATION -> DeclarationCategory.EXCEPTION
                RescriptElementTypes.MODULE_DECLARATION -> DeclarationCategory.MODULE
                RescriptElementTypes.EXTERNAL_DECLARATION -> DeclarationCategory.EXTERNAL
                RescriptElementTypes.LET_DECLARATION -> DeclarationCategory.LET
                else -> null
            }

        /**
         * Returns the sort order index for a given category.
         *
         * @param category the declaration category
         * @return the zero-based sort order index
         */
        internal fun sortOrder(category: DeclarationCategory): Int = DEFAULT_ORDER.indexOf(category)
    }
}

/**
 * Categories of top-level declarations in ReScript for arrangement purposes.
 */
enum class DeclarationCategory {
    OPEN_INCLUDE,
    TYPE,
    EXCEPTION,
    MODULE,
    EXTERNAL,
    LET,
}

/**
 * Arrangement entry wrapping a ReScript top-level declaration.
 *
 * Implements [ArrangementEntry] to integrate with the IntelliJ arrangement framework.
 *
 * @property element the PSI element for this declaration
 * @property range the text range of the declaration in the document
 * @property category the declaration category for sorting
 */
class RescriptArrangementEntry(
    val element: PsiElement,
    val range: TextRange,
    val category: DeclarationCategory,
) : ArrangementEntry {
    override fun getStartOffset(): Int = range.startOffset

    override fun getEndOffset(): Int = range.endOffset

    override fun getParent(): ArrangementEntry? = null

    override fun getChildren(): MutableList<out ArrangementEntry> = mutableListOf()

    override fun getDependencies(): MutableList<out ArrangementEntry>? = null

    override fun canBeMatched(): Boolean = true
}
