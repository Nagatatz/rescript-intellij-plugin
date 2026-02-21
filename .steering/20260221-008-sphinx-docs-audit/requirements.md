# Requirements: Sphinx Documentation Audit Fix

## Background

A documentation consistency audit revealed that while `CLAUDE.md`, `README.md`, `docs/product-requirements.md`, and `docs/functional-design.md` are fully up to date, the `sphinx-docs/` user-facing documentation has multiple gaps where implemented features are not documented.

## Scope

Documentation-only changes to `sphinx-docs/` files. No code changes.

## Requirements

### R1: Feature Page Completeness (18 features)

Add documentation for the following implemented features to the appropriate sphinx-docs pages:

1. **Unwrap/Remove** → `code-editing.md`
2. **JSX auto-close tag** → `code-editing.md`
3. **Enter handler** (comment continuation) → `code-editing.md`
4. **Smart join lines** → `code-editing.md`
5. **Error Lens** → `code-analysis.md`
6. **Highlight Usages** (keyword pairs) → `code-editing.md`
7. **Run Anything** → `run-build.md`
8. **Expression Type** → `advanced.md`
9. **Debugger integration** → `run-build.md`
10. **LSP auto-install** → `installation.md`
11. **TODO indexing** → `advanced.md` (update existing brief mention)
12. **Generate doc comment** → `code-editing.md`
13. **Goto Super** → `navigation.md`
14. **Go to Test** → `navigation.md`
15. **Context Info** → `navigation.md`
16. **External Documentation** → `navigation.md`
17. `.promise` / `.await` postfix templates → `code-completion.md`
18. **Paste as JSX** details → `code-editing.md` (expand existing)

### R2: Keyboard Shortcuts Page (10 shortcuts)

Add missing shortcuts to `keyboard-shortcuts.md`:

- `Ctrl+Shift+Delete` — Unwrap/Remove
- `Ctrl+U` — Goto Super
- `Ctrl+Shift+T` — Go to Test
- `Ctrl+Ctrl` — Run Anything
- `Ctrl+Shift+P` — Expression Type
- `Alt+Shift+D` — Debug Compiled JS
- `Alt+Shift+Cmd+Left/Right` — Move Element (already in code-editing.md, add to shortcuts page)
- `Ctrl+Shift+[` / `]` — Code Block Selection (already in code-editing.md, add to shortcuts page)
- `Shift+F1` — External Documentation
- Context Info description (no dedicated shortcut)

### R3: Live Template Count Unification

Update sphinx-docs from "15" to "21" wherever the live template count is mentioned:
- `changelog.md`
- `configuration.md`
- `code-completion.md` (add 6 missing FFI/component templates to the table)

### R4: Postfix Template Completeness

Add `.promise` and `.await` to the postfix template table and detailed usage in `code-completion.md`.

### R5: Changelog Update

Add B-priority and later feature additions to `changelog.md`.

### R6: Japanese .po Files — OUT OF SCOPE

Deferred to a separate task due to large volume of translation work.

## Acceptance Criteria

- All 18 missing features are documented in appropriate sphinx-docs pages
- All 10 missing keyboard shortcuts are in keyboard-shortcuts.md
- Live template count reads "21" consistently across all sphinx-docs
- All 9 postfix templates are documented
- Changelog reflects all implemented features
- Sphinx build succeeds without errors
