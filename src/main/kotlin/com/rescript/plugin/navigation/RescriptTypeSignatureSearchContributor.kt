package com.rescript.plugin.navigation

import com.intellij.ide.actions.searcheverywhere.FoundItemDescriptor
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributor
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributorFactory
import com.intellij.ide.actions.searcheverywhere.WeightedSearchEverywhereContributor
import com.intellij.ide.util.gotoByName.GotoFileCellRenderer
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import com.rescript.plugin.RescriptFileType
import com.rescript.plugin.RescriptInterfaceFileType
import com.rescript.plugin.lang.psi.RescriptFile
import com.rescript.plugin.lang.psi.RescriptPsiUtils
import javax.swing.ListCellRenderer

/**
 * Search Everywhere contributor that enables searching by type signatures.
 *
 * Users can search for functions by typing signatures like `string -> int`
 * or `option<'a> -> 'a`, and this contributor will match declarations whose
 * type annotations contain the queried pattern.
 *
 * @see WeightedSearchEverywhereContributor
 * @see RescriptSearchEverywhereContributor for symbol name search
 */
class RescriptTypeSignatureSearchContributor(
    event: AnActionEvent,
) : WeightedSearchEverywhereContributor<Any> {
    private val myProject = event.getData(CommonDataKeys.PROJECT)

    override fun getSearchProviderId(): String = "RescriptTypeSignatureSearch"

    override fun getGroupName(): String = "ReScript Types"

    override fun getSortWeight(): Int = 160

    override fun isShownInSeparateTab(): Boolean = true

    override fun showInFindResults(): Boolean = false

    override fun fetchWeightedElements(
        pattern: String,
        progressIndicator: ProgressIndicator,
        consumer: Processor<in FoundItemDescriptor<Any>>,
    ) {
        if (pattern.isBlank() || !looksLikeTypeQuery(pattern)) return

        val project = myProject ?: return
        val scope = GlobalSearchScope.projectScope(project)
        val psiManager = PsiManager.getInstance(project)
        val queryTokens = tokenizeSignature(pattern)

        ApplicationManager.getApplication().runReadAction {
            for (fileType in listOf(RescriptFileType, RescriptInterfaceFileType)) {
                for (vf in com.intellij.psi.search.FileTypeIndex
                    .getFiles(fileType, scope)) {
                    progressIndicator.checkCanceled()
                    val psiFile = psiManager.findFile(vf) as? RescriptFile ?: continue

                    for (child in psiFile.children) {
                        val elementType = child.node?.elementType ?: continue
                        if (elementType !in RescriptPsiUtils.NAVIGABLE_TYPES) continue

                        val text = child.text ?: continue
                        val typeAnnotation = extractTypeAnnotation(text)
                        if (typeAnnotation != null) {
                            val weight = matchSignature(queryTokens, tokenizeSignature(typeAnnotation))
                            if (weight > 0 && child is NavigationItem) {
                                consumer.process(FoundItemDescriptor(child, weight))
                            }
                        }
                    }
                }
            }
        }
    }

    override fun processSelectedItem(
        selected: Any,
        modifiers: Int,
        searchText: String,
    ): Boolean {
        if (selected is NavigationItem) {
            selected.navigate(true)
            return true
        }
        return false
    }

    override fun getElementsRenderer(): ListCellRenderer<in Any> = GotoFileCellRenderer(0)

    override fun getDataForItem(
        element: Any,
        dataId: String,
    ): Any? = null

    override fun dispose() {}

    companion object {
        // Pattern to detect type-like queries (contains -> or has type-like keywords)
        private val TYPE_QUERY_PATTERN = Regex("""->|=>|string|int|float|bool|option|array|result""")

        // Pattern to extract type annotations from declarations
        private val TYPE_ANNOTATION_PATTERN = Regex(""":\s*(.+?)\s*(?:=|$)""")

        /**
         * Checks if the search pattern looks like a type query.
         *
         * @param pattern the search pattern
         * @return true if it appears to be a type signature query
         */
        internal fun looksLikeTypeQuery(pattern: String): Boolean = TYPE_QUERY_PATTERN.containsMatchIn(pattern)

        /**
         * Tokenizes a type signature string into comparable tokens.
         *
         * Splits by delimiters (`->`, `=>`, `,`, `<`, `>`, spaces, parens)
         * and normalizes each token to lowercase.
         *
         * @param signature the type signature string
         * @return list of normalized type tokens
         */
        internal fun tokenizeSignature(signature: String): List<String> {
            val tokens = mutableListOf<String>()

            // First extract arrow operators as tokens
            val remaining = signature.trim()

            // Split on arrows and delimiters
            val parts =
                remaining
                    .split(Regex("""\s*(?:->|=>|[(),<>\[\]\s])\s*"""))
                    .filter { it.isNotBlank() }
                    .map { it.lowercase().trim() }

            tokens.addAll(parts)

            // Also add arrow direction info
            if (signature.contains("->") || signature.contains("=>")) {
                tokens.add("->")
            }

            return tokens
        }

        /**
         * Matches a query token list against a candidate token list.
         *
         * Returns a score > 0 if the query tokens are found in the candidate,
         * with higher scores for better matches.
         *
         * @param queryTokens the search query tokens
         * @param candidateTokens the candidate declaration tokens
         * @return match score (0 = no match, higher = better match)
         */
        internal fun matchSignature(
            queryTokens: List<String>,
            candidateTokens: List<String>,
        ): Int {
            if (queryTokens.isEmpty() || candidateTokens.isEmpty()) return 0

            var matchCount = 0
            for (qt in queryTokens) {
                if (qt == "->") continue // Skip arrow tokens in matching
                if (candidateTokens.any { it.contains(qt) || qt.contains(it) }) {
                    matchCount++
                }
            }

            val meaningfulQueryTokens = queryTokens.count { it != "->" }
            if (meaningfulQueryTokens == 0) return 0

            // Require at least half the query tokens to match
            if (matchCount < (meaningfulQueryTokens + 1) / 2) return 0

            return matchCount
        }

        /**
         * Extracts the type annotation from a declaration text.
         *
         * @param declarationText the declaration source text
         * @return the type annotation string, or null if not found
         */
        internal fun extractTypeAnnotation(declarationText: String): String? {
            val firstLine = declarationText.lines().firstOrNull() ?: return null
            val match = TYPE_ANNOTATION_PATTERN.find(firstLine) ?: return null
            return match.groupValues[1].trim()
        }

        /**
         * Ranks how well a match corresponds to the query.
         *
         * @param queryTokens the query tokens
         * @param candidateTokens the candidate tokens
         * @return rank score (higher = better)
         */
        internal fun rankMatch(
            queryTokens: List<String>,
            candidateTokens: List<String>,
        ): Int {
            val score = matchSignature(queryTokens, candidateTokens)
            if (score == 0) return 0

            // Bonus for exact token count match
            val queryMeaningful = queryTokens.count { it != "->" }
            val candidateMeaningful = candidateTokens.count { it != "->" }
            val sizeBonus = if (queryMeaningful == candidateMeaningful) 2 else 0

            return score + sizeBonus
        }
    }

    /**
     * Factory for creating [RescriptTypeSignatureSearchContributor] instances.
     */
    class Factory : SearchEverywhereContributorFactory<Any> {
        override fun createContributor(initEvent: AnActionEvent): SearchEverywhereContributor<Any> =
            RescriptTypeSignatureSearchContributor(initEvent)
    }
}
