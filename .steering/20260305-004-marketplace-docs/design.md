# Design: Documentation & Marketplace

## 1. plugin.xml Description Expansion

### Current State

10 features listed as a flat `<ul>` list.

### Target

~25 features organized into 6 categories with `<h3>` headings:

1. **Language Support** — Syntax highlighting, semantic tokens, code folding, brace matching, comments, breadcrumbs
2. **Code Intelligence** — Completion, navigation, hover, references, inlay hints, refactoring (extract variable/function, inline, change signature)
3. **Code Analysis** — Error Lens, inspections (duplicate open, signature sync, dead code), format check
4. **Editing** — Intention actions, surround, postfix completion, live templates, generate actions, unwrap/remove
5. **Build & Run** — Run configuration, test runner, debugger, REPL, worksheet, scratch files
6. **Project Integration** — Project wizard (12 templates), LSP auto-install, project view nesting, .d.ts binding generation

Keep the Requirements section (Node.js + LSP) at the bottom.

## 2. README Badge

Add after the CI badge:

```markdown
[![JetBrains Marketplace](https://img.shields.io/jetbrains/plugin/v/com.rescript.plugin.svg)](https://plugins.jetbrains.com/plugin/XXXXX-rescript)
```

Since the exact Marketplace plugin ID number may not be known yet (under review), use the plugin ID `com.rescript.plugin` in the shield URL.

## 3. Changelog Fix

Replace `## Unreleased` with versioned sections:

```markdown
## 0.1.3

- Add plugin icon for JetBrains Marketplace display
- Fix plugin verifier deprecated API warnings

## 0.1.2

(initial Marketplace submission)
- All 109 features implemented
```

## 4. Change-Notes Verification

Current: plugin.xml has `0.1.3` change-notes. gradle.properties has `pluginVersion = 0.1.3`. These match — no change needed.

## No New Files

This unit modifies only existing files. No new test files needed (documentation-only changes).
