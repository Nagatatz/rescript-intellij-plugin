package com.rescript.plugin.wizard

/**
 * Supported package managers for ReScript project initialization.
 */
enum class PackageManager(
    val command: String,
) {
    NPM("npm"),
    PNPM("pnpm"),
    YARN("yarn"),
}

/**
 * Generates starter project files for a new ReScript project.
 *
 * Delegates file generation to [ProjectTemplate.generateFiles], which dispatches
 * to the appropriate template object in the `templates` package.
 *
 * @see ProjectTemplate for the available templates
 * @see ProjectFileBuilders for shared file generation utilities
 */
object RescriptProjectGenerator {
    /**
     * Generates all project files for the given template.
     *
     * @param template the project template to use
     * @param projectName the name of the project
     * @return map of relative file paths to content strings
     */
    fun generateFiles(
        template: ProjectTemplate,
        projectName: String,
    ): Map<String, String> = template.generateFiles(projectName)
}
