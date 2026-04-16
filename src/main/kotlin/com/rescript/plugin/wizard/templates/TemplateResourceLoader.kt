package com.rescript.plugin.wizard.templates

/**
 * Loads wizard template file content from classpath resources under `templates/`
 * and substitutes `{{key}}` placeholders.
 *
 * Keeps large static template content (README sections, sample `.res` sources,
 * `drizzle.config.ts`, etc.) out of Kotlin source by sourcing it from
 * `src/main/resources/templates/<template>/<relative-path>`. Dynamic fragments
 * (e.g. package-manager-specific commands) are passed as a `vars` map and
 * substituted at load time.
 *
 * Placeholder syntax: `{{key}}`. Mustache-style double braces are used instead
 * of `${...}` to avoid collision with JavaScript/TypeScript/ReScript template
 * literal syntax in extracted files.
 */
internal object TemplateResourceLoader {
    // Matches {{identifier}} where identifier is [A-Za-z][A-Za-z0-9_]*.
    private val PLACEHOLDER = Regex("""\{\{([a-zA-Z][a-zA-Z0-9_]*)}}""")

    /**
     * Loads the resource at `templates/$path` and substitutes `{{key}}` occurrences
     * using [vars].
     *
     * @param path relative path under `src/main/resources/templates/`
     * @param vars map of placeholder keys to substitution values
     * @param strict when true (default), throws if any `{{key}}` remains unsubstituted
     * @return the loaded (and optionally substituted) text content
     * @throws IllegalStateException if the resource is missing, or if [strict] is true
     *     and one or more placeholders were not provided in [vars]
     */
    fun load(
        path: String,
        vars: Map<String, String> = emptyMap(),
        strict: Boolean = true,
    ): String {
        val resourcePath = "templates/$path"
        val stream =
            TemplateResourceLoader::class.java.classLoader
                .getResourceAsStream(resourcePath)
                ?: error("Template resource not found: $resourcePath")
        val raw = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        return PLACEHOLDER.replace(raw) { match ->
            val key = match.groupValues[1]
            vars[key] ?: if (strict) {
                error("Unsubstituted placeholder {{$key}} in $resourcePath")
            } else {
                match.value
            }
        }
    }
}
