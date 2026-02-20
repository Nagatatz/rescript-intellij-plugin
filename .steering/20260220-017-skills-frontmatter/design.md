# Design: Skills Frontmatter Enhancement

## Date: 2026-02-20

## Approach

Edit the YAML frontmatter in each skill's SKILL.md file. No new files or structural changes required.

## Changes Per Skill

### 1. steering/SKILL.md
- **Current**: `allowed-tools: Read, Write, Edit, Glob, Bash`
- **Change**: Add `Grep`, reorder to standard order, add `disable-model-invocation: true`
- **Target**: `allowed-tools: Read, Glob, Grep, Write, Edit, Bash` + `disable-model-invocation: true`

### 2. git-workflow/SKILL.md
- **Current**: `allowed-tools: Bash, Read, Glob, Grep`
- **Change**: Reorder to standard order, add `disable-model-invocation: true`
- **Target**: `allowed-tools: Read, Glob, Grep, Bash` + `disable-model-invocation: true`

### 3. fix-qodana/SKILL.md
- **Current**: `allowed-tools: Bash, Read, Glob, Grep, WebFetch`
- **Change**: Replace WebFetch with Write/Edit, reorder, add `disable-model-invocation: true`
- **Target**: `allowed-tools: Read, Glob, Grep, Write, Edit, Bash` + `disable-model-invocation: true`

### 4. review-docs/SKILL.md
- **Current**: `allowed-tools: Read, Glob, Grep`
- **Change**: Add WebFetch and WebSearch
- **Target**: `allowed-tools: Read, Glob, Grep, WebFetch, WebSearch`

### 5. implementation-validator/SKILL.md
- **Current**: `allowed-tools: Bash, Read, Glob, Grep`
- **Change**: Reorder, add `context: fork`
- **Target**: `allowed-tools: Read, Glob, Grep, Bash` + `context: fork`

### 6. development-guidelines/SKILL.md
- **Current**: `allowed-tools: Read, Write, Edit`
- **Change**: Add Glob and Grep
- **Target**: `allowed-tools: Read, Glob, Grep, Write, Edit`

### 7. add-feature/SKILL.md
- **Current**: No `allowed-tools` field
- **Change**: Add `allowed-tools` field
- **Target**: `allowed-tools: Read, Glob, Grep, Write, Edit, Bash`

### 8. prd-writing/SKILL.md
- **Current**: `allowed-tools: Read, Write` (missing `---` delimiters)
- **Change**: Add `---` delimiters, add Glob/Grep/Edit
- **Target**: `allowed-tools: Read, Glob, Grep, Write, Edit` (with proper `---` delimiters)

## Impact Analysis

- No code changes; only skill metadata changes
- No build impact
- No test impact (frontmatter changes only)
