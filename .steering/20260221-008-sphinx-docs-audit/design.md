# Design: Sphinx Documentation Audit Fix

## Approach

Pure documentation update — edit existing sphinx-docs markdown files to add missing feature descriptions. No new pages created; content is added to existing pages following established structure and style.

## File Change Map

### code-editing.md
Add sections:
- **Unwrap/Remove** — Ctrl+Shift+Delete, remove wrapping Some/Ok/Error/if/switch/try/braces
- **JSX Auto-Close Tag** — Auto-insert closing JSX tag on `>` keystroke
- **Enter Handler** — Auto-continue doc comments (`/** */`) and line comments (`//`)
- **Smart Join Lines** — Join let bindings, pipe chains, arrow expressions
- **Highlight Usages** — Highlight matching switch/if/try keyword pairs
- **Generate Doc Comment** — Add to intention actions table
- **Paste as JSX** — Expand existing brief mention with HTML attribute conversion details

### code-analysis.md
Add section:
- **Error Lens** — Inline diagnostic display at end of line

### navigation.md
Add sections:
- **Goto Super** — .res → .resi declaration jump (Ctrl+U)
- **Go to Test** — Implementation ↔ test file navigation (Ctrl+Shift+T)
- **Context Info** — Sticky declaration header when scrolling
- **External Documentation** — Belt/Js module docs (Shift+F1)

### run-build.md
Add sections:
- **Run Anything** — Ctrl+Ctrl for ReScript CLI commands
- **Debugger Integration** — Debug compiled JS (Alt+Shift+D)

### advanced.md
Add sections:
- **Expression Type** — Show type at cursor (Ctrl+Shift+P)
- **LSP Auto-Install** — One-click language server installation notification
- Update existing **TODO Indexing** and **Open Statement Index** with more detail

### installation.md
Add section:
- **Automatic LSP Installation** — Notification bar with install button

### code-completion.md
- Add `.promise` and `.await` to postfix template table
- Add detailed usage sections for `.promise` and `.await`
- Add 6 FFI/component live templates to the table (`@module`, `@val`, `@send`, `@get`, `@set`, `comp`)
- Update live template count from 15 to 21

### keyboard-shortcuts.md
- Add 10 missing shortcuts distributed across existing sections (Navigation, Editing, Running)

### configuration.md
- Update live template count from 15 to 21

### changelog.md
- Update live template count from 15 to 21
- Add missing features to the feature list

## Style Guidelines

- Follow existing documentation style (heading levels, code block formatting)
- Use `:::` admonition syntax consistently
- Include keyboard shortcut callouts in format `Ctrl+X` / `Cmd+X`
- Provide before/after code examples where applicable
