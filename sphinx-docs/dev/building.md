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

## Sequential Compatibility Verification

`recommended()` tracks current releases, and the default verifier matrix also includes the exact minimum IDE, 2026.1.4. To reproduce one release at a time, pass `verificationIde`; this replaces the default matrix for that invocation without changing the compile target or compatibility floor.

```bash
./gradlew verifyPlugin -PverificationIde=2026.1.4 -PverificationMaxHeap=2g --no-parallel --max-workers=1
./gradlew verifyPlugin -PverificationIde=2026.2.3 -PverificationMaxHeap=2g --no-parallel --max-workers=1
./gradlew verifyPlugin -PverificationIde=263.6259.32 -PverificationMaxHeap=2g --no-parallel --max-workers=1
```

Run these commands sequentially. `verificationMaxHeap` caps the separate verifier JVM heap; the Gradle and Kotlin compiler processes require additional memory. Check the latest stable and EAP metadata before choosing versions. For another product, add `-PverificationIdeType=WS` (WebStorm), `PY` (PyCharm), or `PS` (PhpStorm), using a version available for that product. A successful verifier result checks binary compatibility; it does not replace an IDE smoke test of highlighting, completion, diagnostics, navigation, code actions, or language-server restart.

For cold IDEs, prefer the read-only Monthly Plugin Verify workflow on the dedicated verification branch. Dispatch one `ide_version` and optional `ide_type` per run; a single-IDE dispatch skips the unrelated template audit. Inputs pass through environment variables, validation, and a quoted argument array. The workflow checks free disk space before IDE resolution and caps the verifier heap at 2 GiB with one Gradle worker.

```bash
gh workflow run monthly-verify.yml --ref chore/ide-compatibility-115 -f ide_version=2026.1.4 -f ide_type=IU
# After the first run completes:
gh workflow run monthly-verify.yml --ref chore/ide-compatibility-115 -f ide_version=263.6259.32 -f ide_type=IU
```

Locally, verify the already cached stable IDE first when disk space is limited. Obtain the local heavy-task slot before running Gradle, and recheck disk space before any new download. CI verification does not launch a local IDE or publish a release.

Keep the compile target at the adopted stable release until a separately reviewed update is needed. Record IDE build, product, OS, plugin commit, verifier version, API warnings, and smoke results in the compatibility issue. See the [verification policy](https://github.com/Nagatatz/rescript-intellij-plugin/blob/main/docs/ide-compatibility.md) for the product and OS matrix.

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
platformVersion  = 2026.2.3  # the platform the plugin is compiled against
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
