package com.rescript.plugin.codevision

import com.intellij.codeInsight.hints.VcsCodeVisionLanguageContext
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.rescript.plugin.lang.psi.RescriptPsiUtils
import java.awt.event.MouseEvent

/**
 * Enables VCS Code Vision (author info, last change date) above ReScript declarations.
 *
 * Displays "Last changed by X, N days ago" annotations above `let`, `type`,
 * `module`, `external`, and `exception` declarations when VCS integration is active.
 *
 * Uses unstable `VcsCodeVisionLanguageContext` API — review on platform upgrade.
 *
 * @see RescriptCodeVisionProvider for type annotation Code Vision
 */
@Suppress("UnstableApiUsage")
class RescriptVcsCodeVisionContext : VcsCodeVisionLanguageContext {
    override fun isAccepted(element: PsiElement): Boolean {
        val elementType = element.node?.elementType ?: return false
        return elementType in RescriptPsiUtils.NAVIGABLE_TYPES
    }

    override fun handleClick(
        mouseEvent: MouseEvent,
        editor: Editor,
        element: PsiElement,
    ) {
        // Default VCS click behavior (show annotations) is handled by the platform
    }
}
