# Requirements: Skills Frontmatter Enhancement

## Date: 2026-02-20

## Overview

Enhance all skill SKILL.md frontmatter (YAML) with `allowed-tools`, `disable-model-invocation`, and `context` fields to improve security and execution control.

## User Stories

- As a developer, I want each skill to declare only the tools it actually needs, so that the principle of least privilege is enforced.
- As a developer, I want side-effect-heavy skills to require explicit model invocation, so that Git operations and file writes are controlled.
- As a developer, I want independent analysis skills to run in a forked context, so that they don't pollute the main conversation.

## Requirements

### Functional Requirements

1. Add `allowed-tools` to all skills that lack it, or update existing lists to match the minimum required set.
2. Add `disable-model-invocation: true` to skills with side effects (Git operations, file writes in steering/qodana contexts).
3. Add `context: fork` to skills that should run in isolated contexts.

### Target Configuration

| Skill | allowed-tools | disable-model-invocation | context |
|-------|--------------|-------------------------|---------|
| steering | Read, Glob, Grep, Write, Edit, Bash | true | - |
| git-workflow | Read, Glob, Grep, Bash | true | - |
| fix-qodana | Read, Glob, Grep, Write, Edit, Bash | true | - |
| review-docs | Read, Glob, Grep, WebFetch, WebSearch | - | - |
| implementation-validator | Read, Glob, Grep, Bash | - | fork |
| development-guidelines | Read, Glob, Grep, Write, Edit | - | - |
| add-feature | Read, Glob, Grep, Write, Edit, Bash | - | - |
| prd-writing | Read, Glob, Grep, Write, Edit | - | - |

### Constraints

- Preserve all existing frontmatter fields (description, model, etc.)
- Only modify the YAML frontmatter section; do not change skill body content
- Ensure proper `---` YAML delimiters on all files
