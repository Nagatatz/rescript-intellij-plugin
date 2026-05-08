package com.rescript.plugin.interop

/**
 * Classifies a single line of ReScript source into an [InteropKind]
 * and [RiskLevel], returning `null` when the line does not look like
 * a JS interop construct.
 *
 * The heuristic operates on raw text (no PSI access) so the same
 * function can power the scanner and the unit tests without an IDE
 * fixture.
 */
object RescriptInteropClassifier {
    private val BS_ATTR_TOKENS =
        listOf("@bs.send", "@bs.module", "@bs.scope", "@bs.get", "@bs.set", "@module", "@send", "@scope", "@val")

    /**
     * Returns `(kind, risk)` for the line, or `null` if the line does
     * not match any tracked interop construct.
     */
    fun classify(line: String): Pair<InteropKind, RiskLevel>? {
        val trimmed = line.trimStart()
        return when {
            trimmed.contains("Obj.magic") -> {
                InteropKind.OBJ_MAGIC to RiskLevel.HIGH
            }

            trimmed.contains("%raw") || trimmed.contains("%%raw") -> {
                InteropKind.RAW to RiskLevel.HIGH
            }

            // `external` is checked before `@bs.*` so a line like
            // `@bs.send external foo: ...` is reported as EXTERNAL
            // (with MEDIUM risk for the bs decorator) rather than the
            // less specific BS_ATTR-only category.
            line.contains("external ") || line.contains("external\t") -> {
                if (BS_ATTR_TOKENS.any { line.contains(it) }) {
                    InteropKind.EXTERNAL to RiskLevel.MEDIUM
                } else {
                    InteropKind.EXTERNAL to RiskLevel.LOW
                }
            }

            BS_ATTR_TOKENS.any { trimmed.startsWith(it) } -> {
                InteropKind.BS_ATTR to RiskLevel.LOW
            }

            else -> {
                null
            }
        }
    }
}
