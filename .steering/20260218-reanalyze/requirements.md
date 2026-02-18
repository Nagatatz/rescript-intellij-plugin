# Requirements: reanalyze Integration

## Overview
Integrate the `reanalyze` tool (dead code analysis, unhandled exception analysis) into the IDE as editor warnings.

## Acceptance Criteria
- [ ] Auto-detect `rescript-tools.exe` from `node_modules/rescript/`
- [ ] Run `reanalyze -json` and parse JSON output
- [ ] Show dead code/exception warnings in the editor
- [ ] Silently disable if binary not found
