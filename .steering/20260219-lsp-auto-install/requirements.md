# Requirements: LSP Auto-Install Promotion

## Overview

When users install the ReScript plugin but don't have `@rescript/language-server` installed, LSP features (completion, diagnostics, go-to-definition, etc.) don't work. Currently only a passive warning bar appears when opening `.res` files, requiring manual `npm install`.

## User Stories

1. **As a new ReScript plugin user**, I want to be prompted to install `@rescript/language-server` automatically so I can get full IDE support without manual terminal commands.
2. **As a monorepo user**, I want the plugin to detect my package manager (npm/yarn/pnpm) so the install command is correct for my project.
3. **As a user who already configured a custom LSP path**, I want to not be bothered by install prompts.

## Acceptance Criteria

- [ ] Editor notification bar shows an "Install with {pm}" button alongside existing "Configure..." and "Dismiss" buttons
- [ ] Project startup shows a balloon notification when LSP is missing in a ReScript project
- [ ] Package manager is auto-detected from lock files (pnpm-lock.yaml > yarn.lock > package-lock.json)
- [ ] Install runs in background with progress indicator
- [ ] After successful install, LSP auto-starts and notifications disappear
- [ ] Failed install shows error notification with stderr content
- [ ] Startup balloon can be dismissed per session
- [ ] No prompts when custom LSP path is configured or LSP is already available

## Constraints

- Must use IntelliJ Platform APIs (ProgressManager, GeneralCommandLine, Notification API)
- Must support monorepo layouts (search parent directories for lock files)
- Defaults to npm if no lock file is found
