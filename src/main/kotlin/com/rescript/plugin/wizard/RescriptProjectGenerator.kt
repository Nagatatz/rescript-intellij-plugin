package com.rescript.plugin.wizard

import com.rescript.plugin.wizard.templates.TemplateContext

/**
 * Supported package managers for ReScript project initialization.
 */
enum class PackageManager(
    val command: String,
) {
    NPM("npm"),
    PNPM("pnpm"),
    YARN("yarn"),
    BUN("bun"),
    ;

    /**
     * Returns the CLI command (lowercase) so default Swing renderers show
     * "npm" / "pnpm" / "yarn" / "bun" in the Wizard ComboBox instead of the Kotlin enum name.
     */
    override fun toString(): String = command
}

/**
 * Generates starter project files for a new ReScript project.
 *
 * Delegates file generation to [ProjectTemplate.generateFiles], which dispatches
 * to the appropriate template object in the `templates` package.
 *
 * @see ProjectTemplate for the available templates
 * @see ProjectFileBuilders for shared file generation utilities
 * @see TemplateContext for the per-project context carrying the selected package manager
 */
object RescriptProjectGenerator {
    /**
     * Generates all project files for the given template and context.
     *
     * @param template the project template to use
     * @param ctx the template context (project name + selected package manager)
     * @return map of relative file paths to content strings
     */
    fun generateFiles(
        template: ProjectTemplate,
        ctx: TemplateContext,
    ): Map<String, String> = template.generateFiles(ctx)

    /**
     * Back-compatible overload that defaults to pnpm when no context is supplied.
     *
     * @param template the project template to use
     * @param projectName the name of the project
     */
    fun generateFiles(
        template: ProjectTemplate,
        projectName: String,
    ): Map<String, String> = template.generateFiles(TemplateContext(projectName, PackageManager.PNPM))
}
