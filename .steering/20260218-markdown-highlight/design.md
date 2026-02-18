# Design: Markdown ReScript Highlighting

## New Files
- `src/main/kotlin/com/rescript/plugin/injection/RescriptMarkdownCodeFenceProvider.kt`
- `src/main/resources/META-INF/rescript-markdown.xml`

## Changed Files
- `src/main/resources/META-INF/plugin.xml` (optional dependency)
- `build.gradle.kts` (bundledPlugin)

## Architecture
- `CodeFenceLanguageProvider` from Markdown plugin
- Optional dependency pattern (same as rescript-js-injection.xml)
