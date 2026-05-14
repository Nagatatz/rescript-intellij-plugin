package com.rescript.plugin.impact

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.JBColor
import java.awt.Color

/**
 * Identifies a single ReScript `type` declaration that the impact
 * preview is currently inspecting.
 *
 * @property name fully-qualified name (e.g. `User.t`) used in display
 * @property localName the bare name (`t`) used by reference search
 * @property declarationFile the file containing the declaration
 * @property declarationOffset offset of the declared identifier inside
 *   [declarationFile]; used to suppress the declaration site itself
 *   from the reference list
 */
data class TypeTarget(
    val name: String,
    val localName: String,
    val declarationFile: VirtualFile,
    val declarationOffset: Int,
)

/**
 * Coarse classification of how a reference uses the target type. The
 * classifier emits this enum so the panel can group / annotate
 * references; the categories are deliberately simple because the
 * heuristic is token-based and cannot perfectly distinguish all
 * ReScript constructs.
 */
enum class TypeRefKind {
    /** Type annotation (`: t`, `: User.t`, etc.) */
    TYPE_REF,

    /** Variant or polymorphic-variant constructor invocation (`Some(x)`) */
    CONSTRUCTOR,

    /** Pattern in a `switch` arm or destructure */
    PATTERN,

    /** Field access (`x.t`) or module member dot reference */
    FIELD_ACCESS,

    /** Catch-all when no token-level rule fires */
    UNKNOWN,
}

/**
 * One entry in the impact tool window's reference list.
 *
 * @property file the source file containing the reference
 * @property offset zero-based character offset of the reference token
 * @property lineNumber 1-based line number, used for display and
 *   for [com.intellij.openapi.fileEditor.OpenFileDescriptor] navigation
 * @property previewLine source line text, trimmed and capped for the
 *   list cell renderer
 * @property kind classification produced by [RescriptReferenceClassifier]
 */
data class ReferenceEntry(
    val file: VirtualFile,
    val offset: Int,
    val lineNumber: Int,
    val previewLine: String,
    val kind: TypeRefKind,
)

/**
 * Returns a [JBColor] for each [TypeRefKind], used by the Type Impact
 * panel to bold-tint the `[kind]` prefix in every reference row. The
 * mapping is exhaustive (every enum value has a colour) so the panel
 * can read it without an exhaustive `when` and so unit tests can
 * assert palette completeness and uniqueness.
 */
internal fun colorForKind(kind: TypeRefKind): JBColor =
    when (kind) {
        TypeRefKind.TYPE_REF -> JBColor(Color(0x3E72C2), Color(0x6B9CE6))
        TypeRefKind.CONSTRUCTOR -> JBColor(Color(0x8C56C2), Color(0xB07AE0))
        TypeRefKind.PATTERN -> JBColor(Color(0x3E9E3E), Color(0x68C268))
        TypeRefKind.FIELD_ACCESS -> JBColor(Color(0xC79A2B), Color(0xE6BC55))
        TypeRefKind.UNKNOWN -> JBColor(Color(0x9A9A9A), Color(0x6F6F6F))
    }
