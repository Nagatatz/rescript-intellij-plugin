# Requirements: Debugger Integration

## Overview

Add Node.js debugger integration for compiled JavaScript from ReScript files. Since ReScript does not generate source maps, the plugin provides a "Debug Compiled JS" action and a dedicated Run Configuration that runs `node --inspect-brk` on the compiled JavaScript output.

## User Stories

1. As a ReScript developer, I want to debug the compiled JavaScript output of my `.res` file so that I can inspect runtime behavior.
2. As a ReScript developer, I want a reusable Run Configuration for debugging so that I can save debug settings.

## Acceptance Criteria

- [ ] "Debug Compiled JavaScript" action available in GoTo menu with `Alt+Shift+D` shortcut
- [ ] Action resolves `.res` file to compiled `.js` file and launches `node --inspect-brk`
- [ ] Action is disabled for non-ReScript files
- [ ] Dedicated "ReScript Debug" Run Configuration type available in Run/Debug configurations
- [ ] Run Configuration persists source file path, node executable, additional arguments, and working directory
- [ ] Settings editor with file chooser for source file, node path, args, and working directory
- [ ] Optional plugin dependencies declared for JavaScriptDebugger and NodeJS (future integration)
- [ ] Unit tests for action behavior and configuration type/factory

## Constraints

- No direct dependency on JavaScriptDebugger or NodeJS plugin APIs (CE compatibility)
- Uses `GeneralCommandLine` and standard IntelliJ execution APIs only
- Reuses existing `findCompiledJsFile` from `RescriptOpenCompiledJsAction`
