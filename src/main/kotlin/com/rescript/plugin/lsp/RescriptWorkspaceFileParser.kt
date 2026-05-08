package com.rescript.plugin.lsp

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import java.nio.file.Files
import java.nio.file.Path

/**
 * Reads `pnpm-workspace.yaml` and `package.json#workspaces` to extract the
 * list of workspace package globs declared by the user.
 *
 * The pnpm parser is intentionally minimal: it understands the
 * `packages:` list in the canonical format produced by `pnpm init` and
 * does not attempt to be a full YAML implementation. The npm/yarn/bun
 * parser handles both the array form (`"workspaces": []`) and the yarn
 * classic object form (`"workspaces": { "packages": [] }`).
 *
 * @see RescriptGlobExpander for how returned globs are expanded
 * @see RescriptWorkspaceDiscovery
 */
object RescriptWorkspaceFileParser {
    /**
     * Reads workspace globs from any of the supported workspace files at [base].
     *
     * `pnpm-workspace.yaml` takes precedence; if it does not exist (or yields no
     * globs) the parser falls back to `package.json#workspaces`. The order
     * mirrors how pnpm itself resolves the workspace declaration.
     *
     * @param base the project base directory to read workspace files from
     * @return the list of workspace glob patterns, or an empty list when none
     */
    fun readGlobs(base: Path): List<String> {
        val pnpm = readPnpmWorkspaces(base.resolve("pnpm-workspace.yaml"))
        if (pnpm.isNotEmpty()) return pnpm
        return readPackageJsonWorkspaces(base.resolve("package.json"))
    }

    /**
     * Parses a `pnpm-workspace.yaml` file looking for a top-level `packages:` list.
     *
     * Lines are scanned sequentially; once `packages:` is found the parser
     * collects following indented `- "..."` entries until the indentation
     * resets to column zero or another top-level key starts.
     *
     * @param file path to `pnpm-workspace.yaml`
     * @return the list of glob entries, or an empty list when the file is
     *         missing, unreadable, or does not declare `packages:`
     */
    fun readPnpmWorkspaces(file: Path): List<String> {
        if (!Files.isRegularFile(file)) return emptyList()
        val lines =
            try {
                Files.readAllLines(file)
            } catch (_: Exception) {
                return emptyList()
            }
        val result = mutableListOf<String>()
        var inPackages = false
        for (raw in lines) {
            val line = raw.replace("\t", "  ")
            val withoutComment = stripComment(line)
            if (!inPackages) {
                if (PACKAGES_HEADER.matches(withoutComment)) {
                    inPackages = true
                }
                continue
            }
            // Stop when we hit a non-empty, non-indented line that isn't a list item
            if (withoutComment.isBlank()) continue
            val leadingSpaces = withoutComment.takeWhile { it == ' ' }.length
            if (leadingSpaces == 0) {
                // New top-level key — section ended
                if (!withoutComment.trimStart().startsWith('-')) break
            }
            val trimmed = withoutComment.trim()
            if (!trimmed.startsWith('-')) {
                // Non-list line inside packages section ends collection
                if (leadingSpaces == 0) break
                continue
            }
            val item =
                trimmed
                    .removePrefix("-")
                    .trim()
                    .let { stripQuotes(it) }
            if (item.isNotEmpty()) result.add(item)
        }
        return result
    }

    /**
     * Reads the `workspaces` field from a `package.json`.
     *
     * Accepts both the npm/yarn berry array form and the yarn classic object
     * form: a top-level `workspaces` array of glob strings, or a nested
     * `workspaces.packages` array of glob strings.
     *
     * @param file path to `package.json`
     * @return the list of glob entries, or an empty list when the field is
     *         missing or malformed
     */
    fun readPackageJsonWorkspaces(file: Path): List<String> {
        if (!Files.isRegularFile(file)) return emptyList()
        val text =
            try {
                Files.readString(file)
            } catch (_: Exception) {
                return emptyList()
            }
        val root =
            try {
                JsonParser.parseString(text)
            } catch (_: Exception) {
                return emptyList()
            }
        if (!root.isJsonObject) return emptyList()
        val workspaces = (root as JsonObject).get("workspaces") ?: return emptyList()
        return when {
            workspaces.isJsonArray -> {
                readArray(workspaces as JsonArray)
            }

            workspaces.isJsonObject -> {
                val packages = (workspaces as JsonObject).get("packages")
                if (packages != null && packages.isJsonArray) {
                    readArray(packages as JsonArray)
                } else {
                    emptyList()
                }
            }

            else -> {
                emptyList()
            }
        }
    }

    private fun readArray(array: JsonArray): List<String> =
        array.mapNotNull { element ->
            if (element.isJsonPrimitive && (element as JsonPrimitive).isString) {
                element.asString.takeIf { it.isNotBlank() }
            } else {
                null
            }
        }

    private fun stripComment(line: String): String {
        // Naive comment stripper: a `#` outside quotes ends the line.
        var inSingle = false
        var inDouble = false
        for ((idx, ch) in line.withIndex()) {
            when {
                ch == '\'' && !inDouble -> inSingle = !inSingle
                ch == '"' && !inSingle -> inDouble = !inDouble
                ch == '#' && !inSingle && !inDouble -> return line.substring(0, idx)
            }
        }
        return line
    }

    private fun stripQuotes(value: String): String {
        if (value.length >= 2) {
            val first = value.first()
            val last = value.last()
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length - 1)
            }
        }
        return value
    }

    private val PACKAGES_HEADER = Regex("^\\s*packages\\s*:\\s*$")
}
