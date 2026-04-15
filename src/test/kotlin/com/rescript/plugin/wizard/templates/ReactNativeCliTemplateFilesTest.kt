package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ReactNativeCliTemplateFilesTest {
    private val ctx = TemplateContext("mobileCli", PackageManager.PNPM)

    @Test
    fun `generate returns all required files`() {
        val files = ReactNativeCliTemplateFiles.generate(ctx)
        val required =
            listOf(
                "rescript.json",
                "package.json",
                "app.json",
                "index.js",
                "App.tsx",
                "metro.config.js",
                "babel.config.js",
                "src/App.res",
                "src/ReactNative.res",
                "src/NativeGreeting.res",
                "src/__tests__/App.test.mjs",
                "README.md",
                ".gitignore",
                ".editorconfig",
                ".github/workflows/ci.yml",
            )
        required.forEach { path ->
            assertTrue(files.containsKey(path), "missing $path")
        }
    }

    @Test
    fun `rescript json declares core and react bs-dependencies with jsx and gentype`() {
        val rj = ReactNativeCliTemplateFiles.generate(ctx)["rescript.json"]!!
        assertTrue(rj.contains("\"@rescript/core\""))
        assertTrue(rj.contains("\"@rescript/react\""))
        assertTrue(rj.contains("\"jsx\""))
        assertTrue(rj.contains("gentypeconfig"))
    }

    @Test
    fun `package json pins react-native and community CLI without expo`() {
        val pkg = ReactNativeCliTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"react-native\": \"${TemplateVersions.REACT_NATIVE}\""))
        assertTrue(pkg.contains("\"@react-native-community/cli\": \"${TemplateVersions.RN_COMMUNITY_CLI}\""))
        assertTrue(pkg.contains("\"@react-native/metro-config\": \"${TemplateVersions.RN_METRO_CONFIG}\""))
        assertTrue(pkg.contains("\"@react-native/babel-preset\": \"${TemplateVersions.RN_BABEL_PRESET}\""))
        assertFalse(pkg.contains("\"expo\""), "Community CLI template must not depend on expo")
    }

    @Test
    fun `package json scripts include RN Community CLI commands`() {
        val pkg = ReactNativeCliTemplateFiles.generate(ctx)["package.json"]!!
        listOf(
            "\"start\": \"react-native start\"",
            "\"android\": \"react-native run-android\"",
            "\"ios\": \"react-native run-ios\"",
            "\"test\": \"vitest run\"",
            "\"res:build\": \"rescript\"",
            "\"res:clean\": \"rescript clean\"",
            "\"res:dev\": \"rescript -w\"",
        ).forEach { snippet ->
            assertTrue(pkg.contains(snippet), "package.json missing $snippet")
        }
    }

    @Test
    fun `gitignore covers android and ios build outputs but not dot-expo`() {
        val gi = ReactNativeCliTemplateFiles.generate(ctx)[".gitignore"]!!
        listOf(
            "android/build/",
            "android/app/build/",
            "android/.gradle/",
            "android/local.properties",
            "ios/Pods/",
            "ios/build/",
            "ios/.xcode.env.local",
            "*.hbc",
            "*.keystore",
        ).forEach { pattern ->
            assertTrue(gi.contains(pattern), ".gitignore missing $pattern")
        }
        assertFalse(gi.contains(".expo/"), "Community CLI .gitignore must not include Expo entries")
    }

    @Test
    fun `App tsx re-exports the ReScript App module`() {
        val app = ReactNativeCliTemplateFiles.generate(ctx)["App.tsx"]!!
        assertTrue(app.contains("./src/App.gen"))
        assertTrue(app.contains("export default App"))
    }

    @Test
    fun `metro config extends resolver sourceExts with mjs`() {
        val metro = ReactNativeCliTemplateFiles.generate(ctx)["metro.config.js"]!!
        assertTrue(metro.contains("@react-native/metro-config"))
        assertTrue(metro.contains("sourceExts"))
        assertTrue(metro.contains("\"mjs\""))
    }

    @Test
    fun `babel config uses react-native preset`() {
        val babel = ReactNativeCliTemplateFiles.generate(ctx)["babel.config.js"]!!
        assertTrue(babel.contains("module:@react-native/babel-preset"))
    }

    @Test
    fun `index js registers the app component via AppRegistry`() {
        val index = ReactNativeCliTemplateFiles.generate(ctx)["index.js"]!!
        assertTrue(index.contains("AppRegistry.registerComponent"))
        assertTrue(index.contains("./app.json"))
    }

    @Test
    fun `app json uses project name for name and displayName`() {
        val appJson = ReactNativeCliTemplateFiles.generate(ctx)["app.json"]!!
        assertTrue(appJson.contains("\"name\": \"mobileCli\""))
        assertTrue(appJson.contains("\"displayName\": \"mobileCli\""))
    }

    @Test
    fun `NativeGreeting bindings use NativeModules scope without Kotlin scaffolding`() {
        val ng = ReactNativeCliTemplateFiles.generate(ctx)["src/NativeGreeting.res"]!!
        assertTrue(ng.contains("@module(\"react-native\")"))
        assertTrue(ng.contains("@scope(\"NativeModules\")"))
        assertTrue(ng.contains("let greet"))
        // Kotlin implementation must not be included in this file.
        assertFalse(ng.contains("ReactContextBaseJavaModule"))
        assertFalse(ng.contains("ReactPackage"))
    }

    @Test
    fun `README documents Community CLI, Android Studio, and native module guidance`() {
        val readme = ReactNativeCliTemplateFiles.generate(ctx)["README.md"]!!
        assertTrue(readme.contains("Community CLI"))
        assertTrue(readme.contains("Android Studio"))
        assertTrue(readme.contains("Native Modules"))
        assertTrue(readme.contains("Troubleshooting"))
        assertTrue(readme.contains("Fallback"))
    }

    @Test
    fun `App res demonstrates the shared ReactNative bindings`() {
        val app = ReactNativeCliTemplateFiles.generate(ctx)["src/App.res"]!!
        assertTrue(app.contains("React.useState"))
        assertTrue(app.contains("ReactNative.FlatList"))
        assertTrue(app.contains("ReactNative.TextInput"))
        assertTrue(app.contains("ReactNative.Button"))
    }

    @Test
    fun `default pnpm generate matches ctx-based generate`() {
        val byName = ReactNativeCliTemplateFiles.generate("mobileCli")
        val byCtx = ReactNativeCliTemplateFiles.generate(TemplateContext("mobileCli", PackageManager.PNPM))
        assertEquals(byCtx, byName)
    }
}
