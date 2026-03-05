# Requirements: Documentation & Marketplace

## Background

The plugin.xml `<description>` only lists 10 features, which undersells the plugin's 109+ implemented features. The sphinx-docs changelog uses "Unreleased" heading despite v0.1.3 being the current version. The README lacks a Marketplace badge.

## Goals

1. Expand plugin.xml description to showcase ~25 key features organized by category
2. Add JetBrains Marketplace badge to README
3. Fix changelog to reflect actual released versions
4. Verify plugin.xml change-notes match gradle.properties version

## Scope

### In Scope

- `src/main/resources/META-INF/plugin.xml` — description section expansion
- `README.md` — Add Marketplace badge
- `sphinx-docs/user/changelog.md` — Fix version headings, remove "Unreleased"
- Verify change-notes / gradle.properties consistency

### Out of Scope

- sphinx-docs/user/features/advanced.md expansion (REPL, Worksheet, Scratch) — defer to separate unit if needed
- Screenshots or animated GIFs
- New sphinx-docs pages

## Acceptance Criteria

- [ ] plugin.xml description lists ~25 features in 5-6 organized categories
- [ ] README.md has JetBrains Marketplace badge
- [ ] sphinx-docs changelog uses proper version headings (not "Unreleased")
- [ ] plugin.xml change-notes version matches gradle.properties pluginVersion
- [ ] `./gradlew clean buildPlugin` passes
