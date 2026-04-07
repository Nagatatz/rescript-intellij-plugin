package com.rescript.plugin.paste

import com.intellij.codeInsight.editorActions.CopyPastePostProcessor
import com.intellij.codeInsight.editorActions.TextBlockTransferableData
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.RangeMarker
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.rescript.plugin.lang.psi.RescriptFile
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable

/**
 * Base class for paste processors that detect a specific source language in clipboard
 * content and convert it to ReScript syntax.
 *
 * Subclasses provide detection logic ([detectContent]), conversion logic ([convertContent]),
 * and transferable data wrapping ([createTransferableData], [getOriginalText]).
 * The shared workflow — clipboard extraction, RescriptFile guard, conversion comparison,
 * and WriteCommandAction replacement — is handled by this base class.
 *
 * @param T the transferable data type used by the subclass
 * @see RescriptPasteAsRescriptProcessor for JS/TS to ReScript conversion
 * @see RescriptPasteAsJsxProcessor for HTML to JSX conversion
 */
abstract class RescriptBasePasteProcessor<T : TextBlockTransferableData> : CopyPastePostProcessor<T>() {
    /**
     * Checks whether the clipboard text contains content in the source language.
     *
     * @param text the clipboard text
     * @return true if the text should be converted
     */
    protected abstract fun detectContent(text: String): Boolean

    /**
     * Converts the detected source content to ReScript syntax.
     *
     * @param text the source text to convert
     * @return the converted ReScript text
     */
    protected abstract fun convertContent(text: String): String

    /**
     * Wraps the clipboard text in a transferable data object for this processor.
     *
     * @param text the clipboard text
     * @return the transferable data object
     */
    protected abstract fun createTransferableData(text: String): T

    /**
     * Extracts the original source text from a transferable data object.
     *
     * @param data the transferable data
     * @return the original text
     */
    protected abstract fun getOriginalText(data: T): String

    /**
     * Returns the concrete class of the transferable data for `filterIsInstance`.
     */
    protected abstract val transferableDataClass: Class<T>

    override fun collectTransferableData(
        file: PsiFile,
        editor: Editor,
        startOffsets: IntArray,
        endOffsets: IntArray,
    ): List<T> = emptyList()

    override fun extractTransferableData(content: Transferable): List<T> {
        if (!content.isDataFlavorSupported(DataFlavor.stringFlavor)) return emptyList()
        val text = content.getTransferData(DataFlavor.stringFlavor) as? String ?: return emptyList()
        if (!detectContent(text)) return emptyList()
        return listOf(createTransferableData(text))
    }

    override fun processTransferableData(
        project: Project,
        editor: Editor,
        bounds: RangeMarker,
        caretOffset: Int,
        indented: Ref<in Boolean>,
        values: MutableList<out T>,
    ) {
        val file = PsiDocumentManager.getInstance(project).getPsiFile(editor.document)
        if (file !is RescriptFile) return

        val data = values.filterIsInstance(transferableDataClass).firstOrNull() ?: return
        val original = getOriginalText(data)
        val converted = convertContent(original)
        if (converted == original) return

        WriteCommandAction.runWriteCommandAction(project) {
            editor.document.replaceString(bounds.startOffset, bounds.endOffset, converted)
        }
    }
}
