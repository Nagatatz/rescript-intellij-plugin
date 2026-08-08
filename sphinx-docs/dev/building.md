---
myst:
  html_meta:
    "keywords": "build, gradle, runIde, plugin development"
---

# Building & Running

## Build Commands

| Command | Description |
|---------|-------------|
| `./gradlew buildPlugin` | Build the plugin (includes lexer generation, compilation, packaging) |
| `./gradlew clean buildPlugin` | Clean build from scratch |
| `./gradlew runIde` | Launch a development IDE instance with the plugin loaded |
| `./gradlew test` | Run all tests |
| `./gradlew ktlintCheck` | Run ktlint code style checks |
| `./gradlew ktlintFormat` | Auto-fix ktlint issues |
| `./gradlew koverHtmlReport` | Generate HTML code coverage report |
| `./gradlew verifyPluginStructure` | Verify plugin descriptor and structure |
| `./gradlew verifyPluginProjectConfiguration` | Verify project configuration |
| `./gradlew verifyPlugin` | Verify binary compatibility |

:::{note}
`./gradlew runIde` automatically removes stale `rescript-intellij-plugin-<old>.jar` files from the sandbox during `prepareSandbox`. This prevents `PluginException` failures caused by the IDE loading an outdated plugin jar after a `pluginVersion` bump. Use `./gradlew clean runIde` only when a full sandbox reset is required.
:::

## JFlex Lexer Generation

The JFlex lexer (`RescriptFlexLexer.java`) is auto-generated from `Rescript.flex` during the build:

- **Source:** `src/main/java/com/rescript/plugin/lang/Rescript.flex`
- **Generated:** `src/main/java/com/rescript/plugin/lang/RescriptFlexLexer.java`
- **Gradle task:** `generateRescriptLexer`

The `generateRescriptLexer` task is a dependency of `compileJava` and `compileKotlin`, so you never need to run it manually. The generated file is listed in `.gitignore`.

:::{warning}
Never edit `RescriptFlexLexer.java` directly. Always modify `Rescript.flex` and let the build regenerate the Java file.
:::

## Plugin Packaging

After `./gradlew buildPlugin`, the packaged plugin is at:

```
build/distributions/rescript-intellij-plugin-<version>.zip
```

This `.zip` can be installed in any compatible JetBrains IDE via **Settings** → **Plugins** → **Install Plugin from Disk**.

## Gradle Configuration

Key configuration files:

| File | Purpose |
|------|---------|
| `build.gradle.kts` | Build script (plugins, dependencies, tasks) |
| `gradle.properties` | Version numbers, platform settings |
| `settings.gradle.kts` | Project name and repository configuration |

### Platform Version

The target IntelliJ Platform version is configured in `gradle.properties`:

```properties
pluginSinceBuild = 261.26222   # IntelliJ 2026.1.4+
platformVersion  = 2026.2.0.1  # the platform the plugin is compiled against
```

The floor is set by bytecode, not by API: building against 2026.2 emits Java 25 class files, and 2026.1 is the first release whose bundled JBR is 25. Keep `pluginSinceBuild` a full build number — `261.4` would also admit 2026.1 through 2026.1.3, which the LSP client API is missing from.

Note: `pluginUntilBuild` is intentionally not set, allowing the plugin to be compatible with all future platform versions.

## CI Pipeline

The GitHub Actions CI pipeline (`.github/workflows/ci.yml`) runs on every push and PR:

1. actionlint — Validate GitHub Actions workflow files
2. ktlint — Code style checks
3. Build — Compile and package
4. Test — Run tests with coverage (Kover)
5. Verify — Plugin structure and binary compatibility (push only)
