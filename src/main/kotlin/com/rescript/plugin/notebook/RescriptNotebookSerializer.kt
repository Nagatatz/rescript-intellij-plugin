package com.rescript.plugin.notebook

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException

/**
 * Converts between [NotebookDocument] and the JSON text persisted on
 * disk. Gson is used because it is already a transitive dependency of
 * the plugin (see `RescriptReanalyzeAnnotator`).
 *
 * The on-disk schema is intentionally permissive: missing fields fall
 * back to defaults so older notebooks keep loading after the schema
 * grows new optional properties.
 */
object RescriptNotebookSerializer {
    private val GSON = GsonBuilder().setPrettyPrinting().create()

    /**
     * Returns a pretty-printed JSON representation of [doc] with a
     * trailing newline so the file matches the editor's "newline at
     * end of file" convention.
     */
    fun toJson(doc: NotebookDocument): String {
        val obj = JsonObject()
        obj.addProperty("version", doc.version)
        val cellsArray = com.google.gson.JsonArray()
        for (cell in doc.cells) {
            val cellObj = JsonObject()
            cellObj.addProperty("code", cell.code)
            cellObj.addProperty("lastOutput", cell.lastOutput)
            cellsArray.add(cellObj)
        }
        obj.add("cells", cellsArray)
        return GSON.toJson(obj) + "\n"
    }

    /**
     * Parses [text] into a [NotebookDocument].
     *
     * Empty / whitespace-only input returns [NotebookDocument.empty]
     * so brand-new files don't appear corrupt. Any other parse failure
     * is reported as [IllegalStateException] for the editor to handle
     * (typically by falling back to a raw text view).
     *
     * @throws IllegalStateException when [text] is non-blank but not
     *   valid JSON, or when the top-level value is not an object
     */
    fun fromJson(text: String): NotebookDocument {
        if (text.isBlank()) return NotebookDocument.empty()
        val root: JsonElement =
            try {
                JsonParser.parseString(text)
            } catch (e: JsonSyntaxException) {
                throw IllegalStateException("Invalid notebook JSON: ${e.message}", e)
            }
        if (!root.isJsonObject) {
            throw IllegalStateException("Notebook JSON must be an object")
        }
        val obj = root.asJsonObject
        val version =
            obj
                .getAsJsonPrimitive("version")
                ?.takeIf { it.isNumber }
                ?.asInt ?: 1
        val cellsArray = obj.getAsJsonArray("cells") ?: com.google.gson.JsonArray()
        val cells =
            cellsArray.mapNotNull { element ->
                if (!element.isJsonObject) return@mapNotNull null
                val cellObj = element.asJsonObject
                NotebookCell(
                    code =
                        cellObj.getAsJsonPrimitive("code")?.asString
                            ?: "",
                    lastOutput =
                        cellObj.getAsJsonPrimitive("lastOutput")?.asString
                            ?: "",
                )
            }
        return NotebookDocument(version = version, cells = cells)
    }
}
