# Changelog

## Unreleased

Initial release of the ReScript IntelliJ Plugin.

### Features

- Full syntax highlighting via JFlex lexer
- LSP integration with `@rescript/language-server`
- Semantic highlighting via LSP semantic tokens
- Code completion, Go to Definition, Hover, Find Usages
- Code folding (modules, declarations, comments, custom regions)
- Brace matching and smart quotes
- Structure view
- Code formatting via `rescript format` CLI
- Run configurations (Build, Build Watch, Clean)
- Test runner integration (Jest/Vitest)
- Code inspections (duplicate open, empty module, missing config)
- Dead code analysis via reanalyze
- Import optimization
- Intention actions (Wrap with Some/Ok/Error, Add @genType)
- Surround with (if/switch/try/block)
- Postfix completion (.switch, .pipe, .log, etc.)
- Live templates (15 snippets)
- File templates (Module, Interface, Component)
- Go to Symbol, Go to Related, File switching (.res/.resi)
- Create interface file, Open compiled JavaScript
- Code Lens (type annotations)
- Compiled JS Preview tool window
- Module Hierarchy view
- Build status in status bar
- Signature Help
- Statement up/down mover
- Smart Enter
- Qualified name copy
- Console output file path links
- Editor notification bar (LSP not found)
- JSON Schema for rescript.json
- JavaScript injection in %raw()
- Markdown code fence highlighting
- Paste as JSON.t
- Breadcrumb navigation
- Rename refactoring
- TODO indexing
- Spellchecking
- Project Wizard
- Code Generation (Switch Arms, Module Type)
- .d.ts to ReScript binding generation
