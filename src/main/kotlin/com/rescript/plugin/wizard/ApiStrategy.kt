package com.rescript.plugin.wizard

/**
 * Supported API strategies that the Project Wizard can wire into full-stack templates.
 *
 * - [REST] emits handwritten REST endpoints on Hono with a fetch-based client.
 *   This is the default and keeps zero extra infrastructure.
 * - [GRAPHQL] mounts graphql-yoga on Hono (matching the stack used by the standalone
 *   HONO_GRAPHQL template) and swaps the client for rescript-relay with a codegen step.
 *
 * Selection is surfaced in [RescriptProjectWizardStep] and carried through each template
 * generator via `TemplateContext.apiStrategy`. Currently only FULL_STACK acts on the
 * selection; other templates ignore it and always emit their default shape.
 *
 * @see ValidationLibrary for the validation-library variant dimension (orthogonal)
 */
enum class ApiStrategy(
    val displayName: String,
) {
    REST("REST"),
    GRAPHQL("GraphQL"),
    ;

    /**
     * Resource variant key used to look up bundled template files under
     * `src/main/resources/templates/<template>/api/<key>/...`.
     */
    fun variantKey(): String = name.lowercase()

    /**
     * Returns the user-visible strategy name so default Swing renderers show
     * "REST" / "GraphQL" in the Wizard ComboBox instead of the Kotlin enum name.
     */
    override fun toString(): String = displayName
}
