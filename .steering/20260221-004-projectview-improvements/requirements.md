# Requirements: Project View improvements for .res.js files

## Background

ReScript compiles `.res` files into `.res.js` files. In the Project tool window, these compiled `.res.js` files appear alongside their source `.res` files and are highlighted in orange by VCS (as untracked/ignored files), making them visually distracting.

## User Stories

1. As a developer, I want compiled `.res.js` files to be displayed in gray so they don't visually compete with source `.res` files.
2. As a developer, I want `.res.js` files to be nested under their corresponding `.res` files so the project tree is cleaner and I can collapse generated files.

## Functional Requirements

### FR-1: File Nesting
- `.res.js` files shall be nested as children of their corresponding `.res` files in the Project tool window.
- `.resi.js` files shall be nested as children of their corresponding `.resi` files.
- Nesting shall be collapsible (standard IntelliJ file nesting behavior).

### FR-2: Gray Text Color
- `.res.js` and `.resi.js` files shall be displayed with gray text color in the Project tool window.
- The gray color shall override VCS coloring (orange for untracked files).
- The color shall be theme-aware (appropriate gray for both light and dark themes).

## Non-Functional Requirements

- No impact on IDE startup performance (lazy initialization).
- Compatible with IntelliJ Platform 2025.3+.

## Acceptance Criteria

- [ ] `.res.js` appears nested under `.res` in the Project tool window.
- [ ] `.resi.js` appears nested under `.resi` in the Project tool window.
- [ ] Nested compiled files are displayed in gray text.
- [ ] Gray color overrides VCS coloring.
- [ ] `./gradlew buildPlugin` succeeds.
- [ ] Unit tests pass.
