package com.rescript.plugin.paste

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.ide.CopyPasteManager
import com.rescript.plugin.lang.psi.RescriptFile
import java.awt.datatransfer.DataFlavor

/**
 * Editor action that pastes clipboard JSON content as ReScript `JSON.t` type expressions.
 *
 * Converts JSON values to their ReScript typed equivalents:
 * - `null` -> `JSON.Null`
 * - `true` -> `JSON.Boolean(true)`
 * - `42` -> `JSON.Number(42.)`
 * - `"hello"` -> `JSON.String("hello")`
 * - `[...]` -> `JSON.Array([...])`
 * - `{...}` -> `JSON.Object(dict{...})`
 *
 * Only available when editing a ReScript file and the clipboard contains valid JSON.
 */
class RescriptPasteAsJsonAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val project = e.project ?: return

        val clipboardText =
            CopyPasteManager
                .getInstance()
                .contents
                ?.getTransferData(DataFlavor.stringFlavor) as? String
                ?: return

        val jsonText = clipboardText.trim()
        if (!isLikelyJson(jsonText)) return

        val rescriptCode =
            try {
                convertJsonToRescript(
                    com.google.gson.JsonParser
                        .parseString(jsonText),
                )
            } catch (e: Exception) {
                LOG.debug("Failed to parse clipboard content as JSON", e)
                return
            }

        WriteCommandAction.runWriteCommandAction(project) {
            val offset = editor.caretModel.offset
            editor.document.insertString(offset, rescriptCode)
            editor.caretModel.moveToOffset(offset + rescriptCode.length)
        }
    }

    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.PSI_FILE)
        e.presentation.isEnabledAndVisible = file is RescriptFile
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    companion object {
        private val LOG = logger<RescriptPasteAsJsonAction>()

        fun isLikelyJson(text: String): Boolean {
            val trimmed = text.trimStart()
            return trimmed.startsWith("{") || trimmed.startsWith("[")
        }

        fun convertJsonToRescript(element: com.google.gson.JsonElement): String =
            when {
                element.isJsonNull -> "JSON.Null"
                element.isJsonPrimitive -> {
                    val prim = element.asJsonPrimitive
                    when {
                        prim.isBoolean -> "JSON.Boolean(${prim.asBoolean})"
                        prim.isNumber -> {
                            val num = prim.asDouble
                            val formatted =
                                if (num == num.toLong().toDouble()) {
                                    "${num.toLong()}."
                                } else {
                                    num.toString()
                                }
                            "JSON.Number($formatted)"
                        }
                        prim.isString -> "JSON.String(\"${escapeString(prim.asString)}\")"
                        else -> "JSON.Null"
                    }
                }
                element.isJsonArray -> {
                    val items = element.asJsonArray.joinToString(", ") { convertJsonToRescript(it) }
                    "JSON.Array([$items])"
                }
                element.isJsonObject -> {
                    val entries =
                        element.asJsonObject.entrySet().joinToString(", ") { (key, value) ->
                            "\"$key\": ${convertJsonToRescript(value)}"
                        }
                    "JSON.Object(dict{$entries})"
                }
                else -> "JSON.Null"
            }

        private fun escapeString(s: String): String =
            s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\t", "\\t")
    }
}
