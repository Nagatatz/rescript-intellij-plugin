package com.rescript.plugin.interop

import com.intellij.openapi.vfs.VirtualFile

/**
 * Coarse classification of a JS interop call site, used by the
 * Risk Map tool window to colour and group entries.
 */
enum class InteropKind {
    /** `%raw` / `%%raw` — arbitrary JS embedded as a string. */
    RAW,

    /** `external name : type = "jsName"` JS function declaration. */
    EXTERNAL,

    /** `Obj.magic` cast. Bypasses the type system. */
    OBJ_MAGIC,

    /** `@bs.send` / `@bs.module` / `@module` / `@send` decorator on its own line. */
    BS_ATTR,

    /** Catch-all for matched lines that don't fit the heuristic. */
    UNKNOWN,
}

/**
 * Risk severity attached to an [InteropEntry]. The Risk Map sorts
 * entries by descending severity (HIGH → MEDIUM → LOW).
 */
enum class RiskLevel { HIGH, MEDIUM, LOW }

/**
 * One entry in the Risk Map's list view: a single JS interop site
 * located at a file/offset, classified by [kind] and ranked by
 * [risk].
 *
 * @property file the source file containing the interop site
 * @property offset zero-based offset of the line's first character
 * @property lineNumber 1-based line number, used for display and
 *   navigation
 * @property previewLine the trimmed source line text
 * @property kind the matched interop construct
 * @property risk the heuristic severity ranking
 */
data class InteropEntry(
    val file: VirtualFile,
    val offset: Int,
    val lineNumber: Int,
    val previewLine: String,
    val kind: InteropKind,
    val risk: RiskLevel,
)
