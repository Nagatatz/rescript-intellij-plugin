# Requirements: Error Lens

## Overview

Display LSP diagnostic messages (errors, warnings, info) inline at the end of editor lines, similar to the VS Code Error Lens extension.

## User Stories

- As a developer, I want to see error/warning messages directly in the editor line so I can understand issues without hovering.
- As a developer, I want to control the minimum severity level displayed.
- As a developer, I want to enable/disable Error Lens from settings.

## Acceptance Criteria

- Diagnostic messages appear as colored text at the end of the affected line
- Colors correspond to severity: red for errors, yellow/orange for warnings, gray for info
- Multiple diagnostics on the same line show highest severity with "(+N more)" suffix
- Settings UI allows enabling/disabling and setting minimum severity
- Inlays are properly cleaned up when diagnostics are removed
- Only active for .res/.resi files

## Constraints

- Must use IntelliJ Platform 2025.3+ APIs
- Must work with LSP-provided diagnostics via MarkupModel
