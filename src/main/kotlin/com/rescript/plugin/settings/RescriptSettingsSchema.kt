package com.rescript.plugin.settings

import com.rescript.plugin.errorlens.RescriptErrorLensSeverity

/**
 * One row of the settings form: either a descriptor-backed field or a visual separator.
 */
sealed class SchemaEntry {
    /**
     * A settings field wrapping a [RescriptSettingDescriptor] together with the
     * surrounding [FormBuilder][com.intellij.util.ui.FormBuilder] decorations.
     *
     * @param descriptor the descriptor rendering and persisting the value
     * @param label if non-null, the component is added via `addLabeledComponent`;
     *              otherwise via `addComponent`
     * @param tooltip tooltip text appended after the component (null for no tooltip)
     */
    data class Field<T>(
        val descriptor: RescriptSettingDescriptor<T>,
        val label: String? = null,
        val tooltip: String? = null,
    ) : SchemaEntry()

    /** Horizontal separator between logical groups of fields. */
    data object Separator : SchemaEntry()
}

/**
 * Ordered list of descriptors and separators that drives the ReScript settings UI.
 *
 * The order here must mirror the legacy hand-written `createComponent` layout
 * one-for-one; changing positions moves UI affordances and should be treated as
 * a visible change. Labels, tooltip strings, and default values are copied
 * verbatim from the pre-refactor code to preserve behaviour.
 */
object RescriptSettingsSchema {
    private const val DEFAULT_LOG_LEVEL = "info"
    private const val DEFAULT_ERROR_LENS_SEVERITY = "WARNING"
    private val LOG_LEVELS = arrayOf("error", "warn", "info", "log")

    val entries: List<SchemaEntry> by lazy {
        listOf(
            SchemaEntry.Field(
                descriptor =
                    PathDescriptor(
                        id = "lspServerPath",
                        kind = PathKind.File,
                        title = "Language Server Path",
                        description = "Select the rescript-language-server executable or cli.js",
                        getter = { it.lspServerPath },
                        setter = { s, v -> s.lspServerPath = v },
                    ),
                label = "Language server path:",
                tooltip = "Leave empty to auto-detect from node_modules or PATH.",
            ),
            SchemaEntry.Field(
                descriptor =
                    PathDescriptor(
                        id = "nodePath",
                        kind = PathKind.File,
                        title = "Node.js Interpreter Path",
                        description = "Select the Node.js interpreter",
                        getter = { it.nodePath },
                        setter = { s, v -> s.nodePath = v },
                    ),
                label = "Node.js interpreter path:",
                tooltip = "Leave empty to use \"node\" from PATH.",
            ),
            SchemaEntry.Separator,
            SchemaEntry.Field(
                descriptor =
                    BoolDescriptor(
                        id = "incrementalTypecheckingEnabled",
                        title = "Enable incremental type checking",
                        default = true,
                        getter = { it.incrementalTypecheckingEnabled },
                        setter = { s, v -> s.incrementalTypecheckingEnabled = v },
                    ),
                tooltip =
                    "When enabled, the LSP server uses incremental type checking for faster feedback. " +
                        "Requires LSP server restart.",
            ),
            SchemaEntry.Field(
                descriptor =
                    BoolDescriptor(
                        id = "incrementalTypecheckingAcrossFiles",
                        title = "Cross-file incremental type checking (experimental)",
                        default = false,
                        getter = { it.incrementalTypecheckingAcrossFiles },
                        setter = { s, v -> s.incrementalTypecheckingAcrossFiles = v },
                    ),
                tooltip =
                    "Enable cross-file incremental type checking. Experimental feature. " +
                        "Requires LSP server restart.",
            ),
            SchemaEntry.Separator,
            SchemaEntry.Field(
                descriptor =
                    BoolDescriptor(
                        id = "errorLensEnabled",
                        title = "Enable Error Lens (inline diagnostic display)",
                        default = true,
                        getter = { it.errorLensEnabled },
                        setter = { s, v -> s.errorLensEnabled = v },
                    ),
                tooltip = "Show diagnostic messages inline at the end of editor lines. Requires reopening files.",
            ),
            SchemaEntry.Field(
                descriptor =
                    ComboDescriptor(
                        id = "errorLensMinSeverity",
                        options = RescriptErrorLensSeverity.SEVERITY_NAMES.toTypedArray(),
                        default = DEFAULT_ERROR_LENS_SEVERITY,
                        getter = { it.errorLensMinSeverity },
                        setter = { s, v -> s.errorLensMinSeverity = v },
                    ),
                label = "Minimum severity:",
                tooltip = "Only show diagnostics at or above this severity level.",
            ),
            SchemaEntry.Separator,
            SchemaEntry.Field(
                descriptor =
                    BoolDescriptor(
                        id = "removeUnusedOpensEnabled",
                        title = "Remove unused open statements (requires LSP)",
                        default = true,
                        getter = { it.removeUnusedOpensEnabled },
                        setter = { s, v -> s.removeUnusedOpensEnabled = v },
                    ),
                tooltip =
                    "When enabled, Optimize Imports also removes unused open statements detected " +
                        "by the LSP server.",
            ),
            SchemaEntry.Field(
                descriptor =
                    BoolDescriptor(
                        id = "formatCheckEnabled",
                        title = "Enable format check (highlight unformatted code)",
                        default = false,
                        getter = { it.formatCheckEnabled },
                        setter = { s, v -> s.formatCheckEnabled = v },
                    ),
                tooltip = "When enabled, highlights files that are not formatted according to rescript format.",
            ),
            SchemaEntry.Separator,
            SchemaEntry.Field(
                descriptor =
                    BoolDescriptor(
                        id = "signatureHelpEnabled",
                        title = "Enable signature help",
                        default = true,
                        getter = { it.signatureHelpEnabled },
                        setter = { s, v -> s.signatureHelpEnabled = v },
                    ),
                tooltip = "Enable function call signature assistance from the LSP server.",
            ),
            SchemaEntry.Field(
                descriptor =
                    BoolDescriptor(
                        id = "signatureHelpForConstructorPayloads",
                        title = "Signature help for constructor payloads",
                        default = true,
                        getter = { it.signatureHelpForConstructorPayloads },
                        setter = { s, v -> s.signatureHelpForConstructorPayloads = v },
                    ),
                tooltip = "Show signature help for variant constructor payloads.",
            ),
            SchemaEntry.Field(
                descriptor =
                    BoolDescriptor(
                        id = "cacheProjectConfigEnabled",
                        title = "Enable project config caching",
                        default = true,
                        getter = { it.cacheProjectConfigEnabled },
                        setter = { s, v -> s.cacheProjectConfigEnabled = v },
                    ),
                tooltip = "Cache project configuration for faster LSP startup.",
            ),
            SchemaEntry.Field(
                descriptor =
                    BoolDescriptor(
                        id = "inlayHintsEnabled",
                        title = "Enable inlay hints (experimental)",
                        default = false,
                        getter = { it.inlayHintsEnabled },
                        setter = { s, v -> s.inlayHintsEnabled = v },
                    ),
                tooltip = "Show LSP-provided inlay hints in the editor. Experimental feature.",
            ),
            SchemaEntry.Field(
                descriptor =
                    IntSpinnerDescriptor(
                        id = "inlayHintsMaxLength",
                        default = 25,
                        min = 0,
                        max = 200,
                        step = 1,
                        getter = { it.inlayHintsMaxLength },
                        setter = { s, v -> s.inlayHintsMaxLength = v },
                    ),
                label = "Inlay hints max length:",
                tooltip = "Maximum character length for inlay hint labels (0 = unlimited).",
            ),
            SchemaEntry.Field(
                descriptor =
                    BoolDescriptor(
                        id = "compileStatusEnabled",
                        title = "Enable compile status notifications",
                        default = true,
                        getter = { it.compileStatusEnabled },
                        setter = { s, v -> s.compileStatusEnabled = v },
                    ),
                tooltip = "Receive compile status notifications from the LSP server.",
            ),
            SchemaEntry.Field(
                descriptor =
                    BoolDescriptor(
                        id = "reanalyzeServerEnabled",
                        title = "Enable reanalyze server mode (requires ReScript \u2265 12.1.0)",
                        default = true,
                        getter = { it.reanalyzeServerEnabled },
                        setter = { s, v -> s.reanalyzeServerEnabled = v },
                    ),
                tooltip =
                    "Start a reanalyze server daemon for faster dead code analysis. Requires ReScript 12.1.0+.",
            ),
            SchemaEntry.Separator,
            SchemaEntry.Field(
                descriptor =
                    PathDescriptor(
                        id = "rescriptBinaryPath",
                        kind = PathKind.File,
                        title = "ReScript Binary Path",
                        description = "Select the ReScript compiler binary",
                        getter = { it.rescriptBinaryPath },
                        setter = { s, v -> s.rescriptBinaryPath = v },
                    ),
                label = "ReScript binary path:",
                tooltip = "Leave empty to auto-detect. Path to the ReScript compiler binary.",
            ),
            SchemaEntry.Field(
                descriptor =
                    PathDescriptor(
                        id = "platformPath",
                        kind = PathKind.Folder,
                        title = "Platform Path",
                        description = "Select the ReScript platform directory",
                        getter = { it.platformPath },
                        setter = { s, v -> s.platformPath = v },
                    ),
                label = "Platform path:",
                tooltip = "Leave empty to auto-detect. Path to the ReScript platform directory.",
            ),
            SchemaEntry.Field(
                descriptor =
                    PathDescriptor(
                        id = "runtimePath",
                        kind = PathKind.Folder,
                        title = "Runtime Path",
                        description = "Select the ReScript runtime directory",
                        getter = { it.runtimePath },
                        setter = { s, v -> s.runtimePath = v },
                    ),
                label = "Runtime path:",
                tooltip = "Leave empty to auto-detect. Path to the ReScript runtime directory.",
            ),
            SchemaEntry.Field(
                descriptor =
                    ComboDescriptor(
                        id = "logLevel",
                        options = LOG_LEVELS,
                        default = DEFAULT_LOG_LEVEL,
                        getter = { it.logLevel },
                        setter = { s, v -> s.logLevel = v },
                    ),
                label = "Log level:",
                tooltip = "LSP server log verbosity level.",
            ),
        )
    }

    /** Descriptor ids that represent user-editable filesystem paths. */
    val pathDescriptorIds: Set<String> =
        setOf("lspServerPath", "nodePath", "rescriptBinaryPath", "platformPath", "runtimePath")
}
