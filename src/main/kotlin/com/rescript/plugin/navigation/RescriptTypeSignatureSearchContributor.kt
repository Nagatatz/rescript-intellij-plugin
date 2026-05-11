package com.rescript.plugin.navigation

import com.intellij.ide.actions.searcheverywhere.FoundItemDescriptor
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributor
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributorFactory
import com.intellij.ide.actions.searcheverywhere.WeightedSearchEverywhereContributor
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import com.rescript.plugin.RescriptFileType
import com.rescript.plugin.RescriptInterfaceFileType
import com.rescript.plugin.lang.psi.RescriptFile
import com.rescript.plugin.lang.psi.RescriptPsiUtils
import javax.swing.ListCellRenderer

/**
 * Hoogle-style Search Everywhere contributor for ReScript type
 * signatures. Users open Search Everywhere, switch to the
 * "ReScript Types" tab, and type a structural query like
 * `(int, string) => result<int, string>` or `=> option<'a>` to find
 * declarations whose explicit type annotation matches.
 *
 * The contributor parses both the user query and each candidate's
 * `: T` annotation through [RescriptTypeParser] and ranks the pair
 * via [RescriptTypeUnifier]; only matches above [MatchScore.MISMATCH]
 * reach the result list. Results render as `name: signature
 * (relative/path:line)` so the user sees the full match before
 * navigating, and the row is a [RescriptTypeSignatureSearchHit] —
 * not a `NavigationItem` — so navigation lands on the binding
 * name rather than the start of the file.
 */
class RescriptTypeSignatureSearchContributor(
    event: AnActionEvent,
) : WeightedSearchEverywhereContributor<RescriptTypeSignatureSearchHit> {
    private val myProject: Project? = event.getData(CommonDataKeys.PROJECT)

    override fun getSearchProviderId(): String = "RescriptTypeSignatureSearch"

    override fun getGroupName(): String = "ReScript Types"

    override fun getSortWeight(): Int = 160

    override fun isShownInSeparateTab(): Boolean = true

    override fun showInFindResults(): Boolean = false

    override fun fetchWeightedElements(
        pattern: String,
        progressIndicator: ProgressIndicator,
        consumer: Processor<in FoundItemDescriptor<RescriptTypeSignatureSearchHit>>,
    ) {
        val queryAst = RescriptTypeParser.parse(pattern.trim()) ?: return
        val project = myProject ?: return
        val scope = GlobalSearchScope.projectScope(project)
        val basePath = project.basePath
        val psiManager = PsiManager.getInstance(project)

        ApplicationManager.getApplication().runReadAction {
            for (fileType in listOf(RescriptFileType, RescriptInterfaceFileType)) {
                for (vf in FileTypeIndex.getFiles(fileType, scope)) {
                    progressIndicator.checkCanceled()
                    val psiFile = psiManager.findFile(vf) as? RescriptFile ?: continue

                    for (child in psiFile.children) {
                        val elementType = child.node?.elementType ?: continue
                        if (elementType !in RescriptPsiUtils.NAVIGABLE_TYPES) continue

                        val text = child.text ?: continue
                        val parsed = parseDeclaration(text) ?: continue
                        val candidateAst = RescriptTypeParser.parse(parsed.signatureText) ?: continue
                        val score = RescriptTypeUnifier.match(queryAst, candidateAst)
                        if (score == RescriptTypeUnifier.MatchScore.MISMATCH) continue

                        val declarationOffset = child.textRange.startOffset + parsed.nameOffset
                        val line = lineNumberAt(psiFile.text, declarationOffset)
                        val relativePath = relativeOf(basePath, vf)
                        val hit =
                            RescriptTypeSignatureSearchHit(
                                name = parsed.name,
                                signatureDisplay = parsed.signatureText,
                                file = vf,
                                declarationOffset = declarationOffset,
                                line = line,
                                relativePath = relativePath,
                            )
                        consumer.process(FoundItemDescriptor(hit, score.weight))
                    }
                }
            }
        }
    }

    override fun processSelectedItem(
        selected: RescriptTypeSignatureSearchHit,
        modifiers: Int,
        searchText: String,
    ): Boolean {
        val project = myProject ?: return false
        OpenFileDescriptor(project, selected.file, selected.declarationOffset).navigate(true)
        return true
    }

    override fun getElementsRenderer(): ListCellRenderer<in RescriptTypeSignatureSearchHit> =
        RescriptTypeSignatureCellRenderer()

    override fun getDataForItem(
        element: RescriptTypeSignatureSearchHit,
        dataId: String,
    ): Any? = null

    override fun dispose() {}

    /**
     * Factory that the platform instantiates from the
     * `<searchEverywhereContributor>` extension; produces one
     * contributor per Search Everywhere session.
     */
    class Factory : SearchEverywhereContributorFactory<RescriptTypeSignatureSearchHit> {
        override fun createContributor(
            initEvent: AnActionEvent,
        ): SearchEverywhereContributor<RescriptTypeSignatureSearchHit> =
            RescriptTypeSignatureSearchContributor(initEvent)
    }

    companion object {
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
        internal data class ParsedDeclaration(
            val name: String,
            val nameOffset: Int,
            val signatureText: String,
        )

        /**
         * Pulls `let name: signature = …` / `external name: signature = …`
         * apart so the contributor can feed [signatureText] into the
         * type parser. Returns `null` when no `:` annotation is present
         * (the candidate has only an inferred type) or the form doesn't
         * fit the simple grammar.
         */
        internal fun parseDeclaration(declarationText: String): ParsedDeclaration? {
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
         * Matches the leading `let|external|type [rec] name:` portion;
         * the signature is parsed separately so we can stop at the
         * first **binding** `=` rather than the `=` that's part of `=>`.
         */
        private val DECLARATION_HEADER =
            Regex(
                """^(?:let|external|type)\s+(?:rec\s+)?(\w[\w']*)\s*:""",
                RegexOption.MULTILINE,
            )

        /**
         * Finds the offset of the first depth-0 binding `=` in [text]
         * (i.e. an `=` that is not part of `=>` or `==` and is not
         * inside parens / angles / braces / brackets). Returns -1 if
         * no such `=` exists.
         */
        internal fun findBindingEquals(text: String): Int {
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

        internal fun lineNumberAt(
            source: String,
            offset: Int,
        ): Int {
            if (offset <= 0) return 1
            var line = 1
            val end = minOf(offset, source.length)
            for (i in 0 until end) if (source[i] == '\n') line++
            return line
        }

        internal fun relativeOf(
            basePath: String?,
            file: VirtualFile,
        ): String {
            val full = file.path
            if (basePath != null && full.startsWith(basePath)) {
                return full.removePrefix(basePath).removePrefix("/")
            }
            return full
        }
    }
}
