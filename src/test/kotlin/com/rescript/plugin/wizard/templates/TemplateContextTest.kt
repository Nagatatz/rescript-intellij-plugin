package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TemplateContextTest {
    @Test
    fun `packageManagerSpec formats Corepack string`() {
        assertEquals("npm@${TemplateVersions.NPM}", TemplateContext("p", PackageManager.NPM).packageManagerSpec())
        assertEquals("pnpm@${TemplateVersions.PNPM}", TemplateContext("p", PackageManager.PNPM).packageManagerSpec())
        assertEquals("yarn@${TemplateVersions.YARN}", TemplateContext("p", PackageManager.YARN).packageManagerSpec())
    }

    @Test
    fun `installCmd returns the per-PM install command`() {
        assertEquals("npm install", TemplateContext("p", PackageManager.NPM).installCmd())
        assertEquals("pnpm install", TemplateContext("p", PackageManager.PNPM).installCmd())
        assertEquals("yarn", TemplateContext("p", PackageManager.YARN).installCmd())
    }

    @Test
    fun `runCmd includes npm run prefix only for npm`() {
        assertEquals("npm run dev", TemplateContext("p", PackageManager.NPM).runCmd("dev"))
        assertEquals("pnpm dev", TemplateContext("p", PackageManager.PNPM).runCmd("dev"))
        assertEquals("yarn dev", TemplateContext("p", PackageManager.YARN).runCmd("dev"))
    }

    @Test
    fun `execCmd uses npx, pnpm exec, or yarn`() {
        assertEquals("npx rescript", TemplateContext("p", PackageManager.NPM).execCmd("rescript"))
        assertEquals("pnpm exec rescript", TemplateContext("p", PackageManager.PNPM).execCmd("rescript"))
        assertEquals("yarn rescript", TemplateContext("p", PackageManager.YARN).execCmd("rescript"))
    }

    @Test
    fun `lockfileName reflects selected PM`() {
        assertEquals("package-lock.json", TemplateContext("p", PackageManager.NPM).lockfileName())
        assertEquals("pnpm-lock.yaml", TemplateContext("p", PackageManager.PNPM).lockfileName())
        assertEquals("yarn.lock", TemplateContext("p", PackageManager.YARN).lockfileName())
    }
}
