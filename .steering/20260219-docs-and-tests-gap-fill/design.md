# Design: Documentation & Test Gap Fill

## Approach

### KDoc — Add missing comments to existing files only. No structural changes.

### Tests — Follow existing test patterns:
- Use JUnit 4 (`@Test`, `@Before`, `@After`)
- Temp directories with `Files.createTempDirectory()` for filesystem tests
- Direct method testing for pure-logic classes
- Test naming: backtick-style descriptive names

## Out of Scope (CLAUDE.md exceptions)
- UI components (Swing): settings editors, wizard steps, hierarchy browser
- LSP integration classes: server descriptors, client, rename handler
- Trivial singletons: Language, FileTypes, Icons
- Pure interfaces: RescriptLanguageServer
- External CLI: RescriptFormattingService
