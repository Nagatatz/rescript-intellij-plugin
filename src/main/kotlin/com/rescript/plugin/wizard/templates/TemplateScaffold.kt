package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.ValidationLibrary

/**
 * Shared frame for the wizard template generators.
 *
 * Every `*TemplateFiles` object used to repeat the same three blocks:
 * the common tail of housekeeping files (`.nvmrc`, `LICENSE`,
 * dependabot, README, `.gitignore`, `.editorconfig`, CI workflow), a
 * run of [TemplateResourceLoader.load] calls whose target key equals
 * the resource path, and a zod/sury switch inside a private
 * `xxxDependencies` helper. This object is the single source for those
 * blocks; genuinely template-specific content (README sections,
 * database/API-strategy branching, extra config files) stays in each
 * template class.
 *
 * All helpers are byte-equivalence-safe: they reproduce exactly the
 * strings the per-template code produced before, which the golden
 * characterization tests (`TemplateGoldenTest`) enforce.
 *
 * @see CommonFiles for the individual file builders this frame wraps
 */
internal object TemplateScaffold {
    /**
     * Builds the seven-file common tail every template ships:
     * `.nvmrc`, `LICENSE`, `.github/dependabot.yml`, `README.md`,
     * `.gitignore`, `.editorconfig`, and `.github/workflows/ci.yml`.
     *
     * @param ctx wizard selections driving licence holder, node pin, CI
     * @param readme fully rendered README (templates keep building it
     *   via [CommonFiles.readme] because sections are template-specific)
     * @param gitignoreExtra template-specific ignore lines appended to
     *   the shared base list
     * @param ciHasBuild adds a build step to the CI workflow
     * @param ciHasTest adds a test step to the CI workflow
     * @param ciSetupBun installs Bun in CI (res-x)
     * @return insertion-ordered map of the seven tail entries
     */
    fun commonTail(
        ctx: TemplateContext,
        readme: String,
        gitignoreExtra: List<String> = emptyList(),
        ciHasBuild: Boolean = false,
        ciHasTest: Boolean = false,
        ciSetupBun: Boolean = false,
    ): Map<String, String> =
        linkedMapOf(
            ".nvmrc" to CommonFiles.nvmrc(ctx),
            "LICENSE" to CommonFiles.mitLicense(ctx, holder = ctx.projectName),
            ".github/dependabot.yml" to CommonFiles.dependabotYaml(),
            "README.md" to readme,
            ".gitignore" to CommonFiles.gitignore(gitignoreExtra),
            ".editorconfig" to CommonFiles.editorconfig(),
            ".github/workflows/ci.yml" to
                CommonFiles.ciWorkflow(
                    ctx,
                    hasBuild = ciHasBuild,
                    hasTest = ciHasTest,
                    setupBun = ciSetupBun,
                ),
        )

    /**
     * Loads a run of static resources whose target file key equals the
     * path under [resourceRoot] — the dominant pattern in every
     * template (`"src/App.res" to load("<root>/src/App.res")`).
     *
     * @param resourceRoot template directory under `templates/`
     * @param keys target file keys, also used as resource sub-paths
     * @param vars `{{placeholder}}` substitutions applied to every file
     * @return insertion-ordered map of key to loaded content
     */
    fun resourceFiles(
        resourceRoot: String,
        keys: List<String>,
        vars: Map<String, String> = emptyMap(),
    ): Map<String, String> {
        val files = LinkedHashMap<String, String>(keys.size)
        for (key in keys) {
            files[key] = TemplateResourceLoader.load("$resourceRoot/$key", vars)
        }
        return files
    }

    /**
     * Resolves the validation-library variant of a file: loads
     * `<resourceRoot>/variants/<zod|sury>/<sourcePath>` and pairs it
     * with [targetPath].
     *
     * @param ctx supplies the selected [ValidationLibrary]
     * @param resourceRoot template directory under `templates/`
     * @param targetPath file key in the generated project
     * @param sourcePath path inside the variant directory; defaults to
     *   [targetPath]
     * @return `targetPath to content` ready to add to the file map
     */
    fun validationVariant(
        ctx: TemplateContext,
        resourceRoot: String,
        targetPath: String = "src/Validation.res",
        sourcePath: String = targetPath,
    ): Pair<String, String> {
        val variantKey = ctx.validationLibrary.variantKey()
        return targetPath to TemplateResourceLoader.load("$resourceRoot/variants/$variantKey/$sourcePath")
    }

    /**
     * Returns the npm dependency entry for the selected validation
     * library, replacing the `when (validationLibrary)` switch that
     * each template's private `xxxDependencies` helper duplicated.
     * Callers insert it at the same map position the switch used so
     * `package.json` stays byte-identical.
     */
    fun validationDependency(ctx: TemplateContext): Pair<String, String> =
        when (ctx.validationLibrary) {
            ValidationLibrary.ZOD -> "zod" to TemplateVersions.ZOD
            ValidationLibrary.SURY -> "sury" to TemplateVersions.SURY
        }

    /**
     * Builds the standard runtime dependency block — the ReScript
     * compiler trio followed by the validation library — used verbatim
     * by the simpler templates. Templates with extra runtime
     * dependencies append to the returned map (insertion order is
     * preserved) or fall back to [validationDependency].
     */
    fun standardDependencies(ctx: TemplateContext): LinkedHashMap<String, String> {
        val deps = linkedMapOf<String, String>()
        deps["rescript"] = TemplateVersions.RESCRIPT
        deps["@rescript/core"] = TemplateVersions.RESCRIPT_CORE
        deps["@rescript/runtime"] = TemplateVersions.RESCRIPT_RUNTIME
        val (name, version) = validationDependency(ctx)
        deps[name] = version
        return deps
    }
}
