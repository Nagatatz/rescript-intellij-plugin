# Project Structure

## Top-Level Layout

```
rescript-intellij-plugin/
├── src/
│   ├── main/
│   │   ├── kotlin/com/rescript/plugin/   # Kotlin source code
│   │   ├── java/com/rescript/plugin/lang/ # JFlex lexer definition
│   │   └── resources/                     # Plugin resources
│   └── test/                              # Test code
├── sphinx-docs/                           # Documentation (Sphinx)
├── docs/                                  # Internal design documents
├── .steering/                             # Steering workflow documents
├── .github/workflows/                     # CI/CD workflows
├── build.gradle.kts                       # Gradle build script
├── gradle.properties                      # Version and platform config
├── settings.gradle.kts                    # Project settings
└── CLAUDE.md                              # Development conventions
```

## Source Code Organization

All plugin code is under `src/main/kotlin/com/rescript/plugin/`:

| Package | Purpose | Key Files |
|---------|---------|-----------|
| `(root)` | Core definitions | `RescriptLanguage.kt`, `RescriptFileTypes.kt`, `RescriptIcons.kt` |
| `lang/` | Lexer, parser, tokens | `RescriptLexer.kt`, `RescriptParser.kt`, `RescriptTokenTypes.kt` |
| `lang/psi/` | PSI element classes | `RescriptPsi.kt`, `RescriptStringLiteral.kt` |
| `highlight/` | Syntax highlighting | `RescriptSyntaxHighlighter.kt`, `RescriptColorSettingsPage.kt` |
| `lsp/` | LSP integration | `RescriptLspServerDescriptor.kt`, `RescriptSemanticTokensSupport.kt` |
| `run/` | Run configurations | `RescriptRunConfiguration.kt`, `RescriptCliDetector.kt` |
| `test/` | Test runner | `RescriptTestRunConfiguration.kt`, `RescriptTestFrameworkDetector.kt` |
| `structure/` | Structure view | `RescriptStructureViewFactory.kt` |
| `folding/` | Code folding | `RescriptFoldingBuilder.kt`, `RescriptCustomFoldingProvider.kt` |
| `editor/` | Editor enhancements | `RescriptSmartEnterProcessor.kt`, `RescriptStatementUpDownMover.kt` |
| `completion/` | Completion features | `RescriptPostfixTemplateProvider.kt` |
| `navigation/` | Navigation features | `RescriptSymbolContributor.kt`, `RescriptSwitchFileAction.kt` |
| `analysis/` | Code analysis | `RescriptReanalyzeAnnotator.kt`, `RescriptUnusedCodeInspection.kt` |
| `formatter/` | Formatting | `RescriptFormattingService.kt` |
| `codestyle/` | Code style | `RescriptCodeStyleSettingsProvider.kt` |
| `config/` | Configuration | `RescriptJsonIconProvider.kt`, `RescriptJsonSchemaProviderFactory.kt` |
| `settings/` | Plugin settings | `RescriptProjectSettings.kt`, `RescriptConfigurable.kt` |
| `intention/` | Intention actions | `RescriptWrapWithIntention.kt`, `RescriptAddGenTypeIntention.kt` |
| `surround/` | Surround with | `RescriptSurroundDescriptor.kt` |
| `imports/` | Import management | `RescriptImportOptimizer.kt` |
| `injection/` | Language injection | `RescriptRawJsInjector.kt`, `RescriptMarkdownCodeFenceProvider.kt` |
| `spellcheck/` | Spellchecking | `RescriptSpellcheckingStrategy.kt` |
| `indexing/` | Indexing | `RescriptTodoIndexer.kt` |
| `template/` | File creation | `RescriptCreateFileAction.kt` |
| `statusbar/` | Status bar | `RescriptCompilerStatusWidgetFactory.kt` |
| `codevision/` | Code Lens | `RescriptCodeVisionProvider.kt` |
| `preview/` | JS preview | `RescriptCompiledJsPreviewPanel.kt` |
| `hierarchy/` | Module hierarchy | `RescriptModuleHierarchyProvider.kt` |
| `paste/` | Paste actions | `RescriptPasteAsJsonAction.kt` |
| `generate/` | Code generation | `RescriptGenerateSwitchAction.kt`, `RescriptGenerateModuleTypeAction.kt` |
| `wizard/` | Project wizard | `RescriptModuleBuilder.kt`, `RescriptProjectWizardStep.kt` |
| `commenter/` | Comment toggle | `RescriptCommenter.kt` |

## Resources

```
src/main/resources/
├── META-INF/
│   └── plugin.xml              # Plugin descriptor (all extension points)
├── colorSchemes/
│   ├── RescriptDarcula.xml     # Dark theme colors
│   └── RescriptDefault.xml     # Light theme colors
├── liveTemplates/
│   └── ReScript.xml            # 15 live templates
├── fileTemplates/internal/
│   ├── ReScript Module.res.ft
│   ├── ReScript Interface.resi.ft
│   └── ReScript Component.res.ft
├── schemas/
│   └── rescript.schema.json    # JSON Schema for rescript.json
└── icons/                      # SVG icons
```

## Key Entry Points

### plugin.xml

`src/main/resources/META-INF/plugin.xml` is the central registry for all plugin features. Every extension point implementation is registered here. When adding a new feature, you'll almost always need to add an entry to this file.

### Rescript.flex

`src/main/java/com/rescript/plugin/lang/Rescript.flex` defines the lexer rules. Changes here require running `./gradlew buildPlugin` to regenerate `RescriptFlexLexer.java`.

### RescriptTokenTypes.kt

`src/main/kotlin/com/rescript/plugin/lang/RescriptTokenTypes.kt` defines all token types and token sets. When adding new tokens to the lexer, you must also add them here.
