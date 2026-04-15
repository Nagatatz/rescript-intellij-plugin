package com.rescript.plugin.wizard.templates

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TemplateVersionsTest {
    @Test
    fun `all semver constants are well-formed`() {
        val semverLike = Regex("""^([~^]?\d+(\.\d+){0,2}|>=\d+(\.\d+){0,2})$""")
        val constants =
            listOf(
                "RESCRIPT" to TemplateVersions.RESCRIPT,
                "RESCRIPT_CORE" to TemplateVersions.RESCRIPT_CORE,
                "RESCRIPT_REACT" to TemplateVersions.RESCRIPT_REACT,
                "VITE_PLUS" to TemplateVersions.VITE_PLUS,
                "VITE_PLUS_CORE" to TemplateVersions.VITE_PLUS_CORE,
                "VITEJS_PLUGIN_REACT" to TemplateVersions.VITEJS_PLUGIN_REACT,
                "VITEST" to TemplateVersions.VITEST,
                "REACT" to TemplateVersions.REACT,
                "REACT_DOM" to TemplateVersions.REACT_DOM,
                "REACT_TYPES" to TemplateVersions.REACT_TYPES,
                "HONO" to TemplateVersions.HONO,
                "HONO_NODE_SERVER" to TemplateVersions.HONO_NODE_SERVER,
                "NODE_TYPES" to TemplateVersions.NODE_TYPES,
                "NEXTJS" to TemplateVersions.NEXTJS,
                "ELECTRON" to TemplateVersions.ELECTRON,
                "ELECTRON_BUILDER" to TemplateVersions.ELECTRON_BUILDER,
                "EXPO" to TemplateVersions.EXPO,
                "REACT_NATIVE" to TemplateVersions.REACT_NATIVE,
                "WRANGLER" to TemplateVersions.WRANGLER,
                "CF_WORKERS_TYPES" to TemplateVersions.CF_WORKERS_TYPES,
                "AWS_LAMBDA_TYPES" to TemplateVersions.AWS_LAMBDA_TYPES,
                "ESBUILD" to TemplateVersions.ESBUILD,
                "CONCURRENTLY" to TemplateVersions.CONCURRENTLY,
                "TYPESCRIPT" to TemplateVersions.TYPESCRIPT,
                "NODE_ENGINE" to TemplateVersions.NODE_ENGINE,
            )
        for ((name, value) in constants) {
            assertTrue(semverLike.matches(value), "$name='$value' should be a valid version range")
        }
    }

    @Test
    fun `package manager Corepack versions are bare semver`() {
        val bareSemver = Regex("""^\d+\.\d+\.\d+$""")
        assertTrue(bareSemver.matches(TemplateVersions.PNPM), "PNPM should be a bare semver string for Corepack")
        assertTrue(bareSemver.matches(TemplateVersions.NPM), "NPM should be a bare semver string for Corepack")
        assertTrue(bareSemver.matches(TemplateVersions.YARN), "YARN should be a bare semver string for Corepack")
    }
}
