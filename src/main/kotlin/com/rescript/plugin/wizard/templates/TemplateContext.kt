package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ValidationLibrary

/**
 * Context passed to each template file generator.
 *
 * Bundles the project name, the selected package manager, and the validation library
 * choice so templates can render package-manager-specific commands (e.g. `pnpm dev` vs
 * `npm run dev`), metadata (the `packageManager` field in `package.json` enabling
 * Corepack), and emit server schema files for either `zod` or `sury`.
 *
 * @see TemplateVersions for the dependency versions used together with this context
 */
data class TemplateContext(
    val projectName: String,
    val packageManager: PackageManager,
    val validationLibrary: ValidationLibrary = ValidationLibrary.ZOD,
) {
    /**
     * Returns the Corepack-style spec string for the `packageManager` field in `package.json`.
     */
    fun packageManagerSpec(): String =
        when (packageManager) {
            PackageManager.NPM -> "npm@${TemplateVersions.NPM}"
            PackageManager.PNPM -> "pnpm@${TemplateVersions.PNPM}"
            PackageManager.YARN -> "yarn@${TemplateVersions.YARN}"
        }

    /**
     * Returns the command displayed in README instructions to install dependencies.
     */
    fun installCmd(): String =
        when (packageManager) {
            PackageManager.NPM -> "npm install"
            PackageManager.PNPM -> "pnpm install"
            PackageManager.YARN -> "yarn"
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
        }

    /**
     * Returns the lockfile name produced by the selected package manager.
     */
    fun lockfileName(): String =
        when (packageManager) {
            PackageManager.NPM -> "package-lock.json"
            PackageManager.PNPM -> "pnpm-lock.yaml"
            PackageManager.YARN -> "yarn.lock"
        }
}
