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
import com.rescript.plugin.util.RescriptOffsetUtils
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
                        val parsed = RescriptDeclarationSignatureExtractor.parseDeclaration(text) ?: continue
                        val candidateAst = RescriptTypeParser.parse(parsed.signatureText) ?: continue
                        val score = RescriptTypeUnifier.match(queryAst, candidateAst)
                        if (score == RescriptTypeUnifier.MatchScore.MISMATCH) continue

                        val declarationOffset = child.textRange.startOffset + parsed.nameOffset
                        val line = RescriptOffsetUtils.lineNumberAt(psiFile.text, declarationOffset)
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
