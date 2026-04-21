package com.rescript.plugin.wizard

/**
 * Supported HTTP input validation libraries that the Project Wizard can wire into
 * server-side templates.
 *
 * - [ZOD] emits TypeScript/JavaScript-first schemas via the `zod` npm package, with
 *   thin ReScript bindings. Best for projects that share schemas with JS/TS code.
 * - [SURY] uses the ReScript-native `sury` library, giving tighter integration with
 *   ReScript's type system at the cost of a smaller JS ecosystem.
 *
 * Selection is surfaced in [RescriptProjectWizardStep] and carried through to each
 * template generator via `TemplateContext.validationLibrary`.
 */
enum class ValidationLibrary(
    val displayName: String,
    val npmPackage: String,
) {
    ZOD("zod", "zod"),
    SURY("sury", "sury"),
    ;

    /**
     * Resource variant key used to look up bundled schema files under
     * `src/main/resources/templates/<template>/variants/<key>/...`.
     */
    fun variantKey(): String = name.lowercase()

    /**
     * Returns the user-visible library name (lowercase) so default Swing renderers
     * show "zod" / "sury" in the Wizard ComboBox instead of the Kotlin enum name.
     */
    override fun toString(): String = displayName
}
