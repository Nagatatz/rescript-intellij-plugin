package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.ApiStrategy
import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ValidationLibrary
import java.time.Year

/**
 * Context passed to each template file generator.
 *
 * Bundles the project name, the selected package manager, the validation library
 * choice, and the API strategy so templates can render package-manager-specific
 * commands (e.g. `pnpm dev` vs `npm run dev`), metadata (the `packageManager` field
 * in `package.json` enabling Corepack), emit server schema files for either `zod`
 * or `sury`, and choose between a REST (Hono routes + fetch client) or GraphQL
 * (graphql-yoga + rescript-relay) shape for full-stack templates.
 *
 * Also carries shared-but-non-primary metadata (the LICENSE copyright `year`, and the
 * Node.js `nodeMajor` / `nodeEngine` strings) so individual templates do not each
 * hardcode `TemplateVersions` constants or the current year. Defaults wire to the
 * current year (via [Year.now]) and to [TemplateVersions]; tests override them
 * explicitly to stay deterministic.
 *
 * @see TemplateVersions for the dependency versions used together with this context
 */
data class TemplateContext(
    val projectName: String,
    val packageManager: PackageManager,
    val validationLibrary: ValidationLibrary = ValidationLibrary.ZOD,
    val apiStrategy: ApiStrategy = ApiStrategy.REST,
    val year: Int = Year.now().value,
    val nodeMajor: String = TemplateVersions.NODE_MAJOR,
    val nodeEngine: String = TemplateVersions.NODE_ENGINE,
) {
    /**
     * Returns the Corepack-style spec string for the `packageManager` field in `package.json`.
     */
    fun packageManagerSpec(): String =
        when (packageManager) {
            PackageManager.NPM -> "npm@${TemplateVersions.NPM}"
            PackageManager.PNPM -> "pnpm@${TemplateVersions.PNPM}"
            PackageManager.YARN -> "yarn@${TemplateVersions.YARN}"
            PackageManager.BUN -> "bun@${TemplateVersions.BUN}"
        }

    /**
     * Returns the command displayed in README instructions to install dependencies.
     */
    fun installCmd(): String =
        when (packageManager) {
            PackageManager.NPM -> "npm install"
            PackageManager.PNPM -> "pnpm install"
            PackageManager.YARN -> "yarn"
            PackageManager.BUN -> "bun install"
        }

    /**
     * Returns the command to invoke an npm script (e.g. `pnpm dev`, `npm run dev`).
     *
     * @param script the script name defined in `package.json`
     */
    fun runCmd(script: String): String =
        when (packageManager) {
            PackageManager.NPM -> "npm run $script"
            PackageManager.PNPM -> "pnpm $script"
            PackageManager.YARN -> "yarn $script"
            PackageManager.BUN -> "bun run $script"
        }

    /**
     * Returns the command to execute a one-off binary from `node_modules`
     * (e.g. `pnpm exec rescript`, `npx rescript`).
     *
     * @param binary the CLI binary name (without arguments)
     */
    fun execCmd(binary: String): String =
        when (packageManager) {
            PackageManager.NPM -> "npx $binary"
            PackageManager.PNPM -> "pnpm exec $binary"
            PackageManager.YARN -> "yarn $binary"
            PackageManager.BUN -> "bunx $binary"
        }

    /**
     * Returns the lockfile name produced by the selected package manager.
     */
    fun lockfileName(): String =
        when (packageManager) {
            PackageManager.NPM -> "package-lock.json"
            PackageManager.PNPM -> "pnpm-lock.yaml"
            PackageManager.YARN -> "yarn.lock"
            PackageManager.BUN -> "bun.lock"
        }
}
