package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ValidationLibrary
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Year

class TemplateContextTest {
    @Test
    fun `packageManagerSpec formats Corepack string`() {
        assertEquals("npm@${TemplateVersions.NPM}", TemplateContext("p", PackageManager.NPM).packageManagerSpec())
        assertEquals("pnpm@${TemplateVersions.PNPM}", TemplateContext("p", PackageManager.PNPM).packageManagerSpec())
        assertEquals("yarn@${TemplateVersions.YARN}", TemplateContext("p", PackageManager.YARN).packageManagerSpec())
        assertEquals("bun@${TemplateVersions.BUN}", TemplateContext("p", PackageManager.BUN).packageManagerSpec())
    }

    @Test
    fun `installCmd returns the per-PM install command`() {
        assertEquals("npm install", TemplateContext("p", PackageManager.NPM).installCmd())
        assertEquals("pnpm install", TemplateContext("p", PackageManager.PNPM).installCmd())
        assertEquals("yarn", TemplateContext("p", PackageManager.YARN).installCmd())
        assertEquals("bun install", TemplateContext("p", PackageManager.BUN).installCmd())
    }

    @Test
    fun `runCmd includes npm run prefix only for npm`() {
        assertEquals("npm run dev", TemplateContext("p", PackageManager.NPM).runCmd("dev"))
        assertEquals("pnpm dev", TemplateContext("p", PackageManager.PNPM).runCmd("dev"))
        assertEquals("yarn dev", TemplateContext("p", PackageManager.YARN).runCmd("dev"))
        assertEquals("bun run dev", TemplateContext("p", PackageManager.BUN).runCmd("dev"))
    }

    @Test
    fun `execCmd uses npx, pnpm exec, or yarn`() {
        assertEquals("npx rescript", TemplateContext("p", PackageManager.NPM).execCmd("rescript"))
        assertEquals("pnpm exec rescript", TemplateContext("p", PackageManager.PNPM).execCmd("rescript"))
        assertEquals("yarn rescript", TemplateContext("p", PackageManager.YARN).execCmd("rescript"))
        assertEquals("bunx rescript", TemplateContext("p", PackageManager.BUN).execCmd("rescript"))
    }

    @Test
    fun `lockfileName reflects selected PM`() {
        assertEquals("package-lock.json", TemplateContext("p", PackageManager.NPM).lockfileName())
        assertEquals("pnpm-lock.yaml", TemplateContext("p", PackageManager.PNPM).lockfileName())
        assertEquals("yarn.lock", TemplateContext("p", PackageManager.YARN).lockfileName())
        assertEquals("bun.lock", TemplateContext("p", PackageManager.BUN).lockfileName())
    }

    @Test
    fun `default validation library is ZOD when omitted`() {
        val ctx = TemplateContext("p", PackageManager.PNPM)
        assertEquals(ValidationLibrary.ZOD, ctx.validationLibrary)
    }

    @Test
    fun `validation library round-trips when provided`() {
        val ctx = TemplateContext("p", PackageManager.PNPM, ValidationLibrary.SURY)
        assertEquals(ValidationLibrary.SURY, ctx.validationLibrary)
    }

    @Test
    fun `year defaults to the current calendar year`() {
        val ctx = TemplateContext("p", PackageManager.PNPM)
        assertEquals(Year.now().value, ctx.year)
    }

    @Test
    fun `nodeMajor and nodeEngine default to TemplateVersions`() {
        val ctx = TemplateContext("p", PackageManager.PNPM)
        assertEquals(TemplateVersions.NODE_MAJOR, ctx.nodeMajor)
        assertEquals(TemplateVersions.NODE_ENGINE, ctx.nodeEngine)
    }

    @Test
    fun `overriding year yields the supplied value`() {
        val ctx = TemplateContext("p", PackageManager.PNPM, year = 2099)
        assertEquals(2099, ctx.year)
    }
}
