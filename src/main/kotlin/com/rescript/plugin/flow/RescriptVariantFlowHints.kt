package com.rescript.plugin.flow

/**
 * Source of the user-facing hint strings the variant flow tool window
 * shows when it has nothing to render.
 *
 * The panel uses an enum-driven message instead of an ad-hoc string so
 * the body of the text area, the bottom status label, and the unit tests
 * always agree on which scenario each message describes.
 */
internal object RescriptVariantFlowHints {
    /**
     * Why the panel has nothing to render. Drives both the long body
     * message and the short status label so they stay in sync.
     */
    internal enum class Reason {
        /** No editor at all is selected in the project. */
        NO_EDITOR,

        /** Some editor is active but it isn't a ReScript file. */
        NOT_RESCRIPT,

        /** Caret is in a ReScript file but not inside any `switch`. */
        NO_SWITCH,
    }

    /**
     * Multi-line help shown inside the text area when no diagram is
     * available. Walks the user through the steps required to get the
     * diagram to appear, so the tool window is self-explanatory the
     * first time someone opens it.
     */
    fun emptyStateMessage(reason: Reason): String =
        when (reason) {
            Reason.NO_EDITOR -> NO_EDITOR_MESSAGE
            Reason.NOT_RESCRIPT -> NOT_RESCRIPT_MESSAGE
            Reason.NO_SWITCH -> NO_SWITCH_MESSAGE
        }

    /**
     * Short one-line status shown in the panel's footer. Pairs with
     * [emptyStateMessage] for the same [Reason] so the long and short
     * messages cannot drift apart.
     */
    fun shortStatusLabel(reason: Reason): String =
        when (reason) {
            Reason.NO_EDITOR -> "Open a ReScript file to see its switch flow."
            Reason.NOT_RESCRIPT -> "Switch to a ReScript file."
            Reason.NO_SWITCH -> "No switch under caret."
        }

    private val NO_EDITOR_MESSAGE =
        """
        No ReScript file is currently open.

        How to use:
          1. Open a .res file.
          2. Place the caret on a `switch` expression.
          3. The decision tree appears here automatically.

        The toolbar also offers Copy Mermaid / Copy DOT exports for
        sharing the current diagram outside the IDE.
        """.trimIndent()

    private val NOT_RESCRIPT_MESSAGE =
        """
        The active editor is not a ReScript file.

        Switch flow diagrams are rendered for .res and .resi files.
        Switch to a ReScript file and place the caret on a `switch`
        expression to see its decision tree.
        """.trimIndent()

    private val NO_SWITCH_MESSAGE =
        """
        No `switch` expression at the caret.

        Move the caret onto (or inside) a `switch` keyword and the
        decision tree will refresh within ~200 ms. Nested switches up
        to depth 3 are expanded inline.
        """.trimIndent()
}
