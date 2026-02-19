# Design: Debugger Integration

## Architecture

### Package Structure

All new classes in `com.rescript.plugin.debug`:

```
debug/
├── RescriptDebugCompiledJsAction.kt       # AnAction: "Debug Compiled JavaScript"
├── RescriptDebugConfigurationType.kt      # ConfigurationTypeBase
├── RescriptDebugConfigurationFactory.kt   # ConfigurationFactory
├── RescriptDebugRunConfiguration.kt       # RunConfigurationBase
├── RescriptDebugRunConfigurationOptions.kt # RunConfigurationOptions
└── RescriptDebugSettingsEditor.kt         # SettingsEditor UI
```

### Key Design Decisions

1. **No JavaScriptDebugger/NodeJS API dependency**: Uses `GeneralCommandLine` to run `node --inspect-brk`. Optional plugin dependencies declared in plugin.xml for future integration only.

2. **Compiled JS resolution**: Reuses `RescriptOpenCompiledJsAction.findCompiledJsFile()` (internal companion method) to locate the compiled JavaScript file.

3. **Run Configuration pattern**: Follows existing `run/` package patterns (RescriptRunConfiguration, etc.) for consistency.

### Action Flow

```
User triggers "Debug Compiled JS" (Alt+Shift+D)
  -> Get current .res file from editor
  -> findCompiledJsFile() resolves to .js in lib/js/
  -> Build GeneralCommandLine: node --inspect-brk <js-path>
  -> Execute via CommandLineState / ProcessHandlerFactory
  -> Output shown in Run tool window
```

### Run Configuration Flow

```
User creates "ReScript Debug" configuration
  -> Settings editor: source file, node path, args, working dir
  -> getState() resolves .res -> .js, builds command line
  -> Execution via standard IntelliJ run infrastructure
```

## Modified Files

- `src/main/resources/META-INF/plugin.xml` - Register configurationType, action, optional depends
- `src/main/resources/META-INF/rescript-debug.xml` - Placeholder for JavaScriptDebugger extensions
- `src/main/resources/META-INF/rescript-nodejs.xml` - Placeholder for NodeJS extensions
