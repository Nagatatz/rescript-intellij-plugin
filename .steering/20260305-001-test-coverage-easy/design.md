# Design: Test Coverage — Easy Tests + Kover Config

## Kover Exclusions

Add wildcard patterns to `build.gradle.kts` kover block to exclude exempt classes:
- `*StartupActivity` — IDE lifecycle
- `*RunConfiguration*`, `*SettingsEditor*` — Run config UI
- `*Panel`, `*ToolWindowFactory` — Swing UI
- `*Configurable*`, `*CodeStyleSettingsProvider` — Settings UI
- `*WizardStep`, `*.wizard.templates.*` — Wizard UI
- LSP coupling: explicit class list
- Pure type defs: `RescriptFileTypes`, `RescriptIcons`, `RescriptLanguage`
- Other exempt: `RescriptFormattingService`, `RescriptSyntaxHighlighterFactory`, etc.

## New Test Files

All 4 tests use plain JUnit (no IDE fixtures) with `RescriptTestUtils` stubs:

1. **RescriptOperatorDocumentationTest** — Test OPERATOR_INFO map entries, generateOperatorDoc with stubPsiElement
2. **RescriptJsonDecoderGeneratorTest** — Test generateDecoder with Record/Variant/Unknown TypeShapes
3. **RescriptJsonEncoderGeneratorTest** — Test generateEncoder with Record/Variant/Unknown TypeShapes
4. **RescriptExternalDocUrlsTest** — Test MODULE_URL_MAP non-empty, URL patterns for Belt/Js

## minBound Raise

Change `minBound(50)` to `minBound(60)` after exclusions and new tests are in place.
