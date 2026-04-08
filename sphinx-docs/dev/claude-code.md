---
myst:
  html_meta:
    "keywords": "claude code, ai development, rules, steering"
---

# Claude Code Development Workflow

This guide explains how to use [Claude Code](https://docs.anthropic.com/en/docs/claude-code) with projects built from the [claude-project-template](https://github.com/anthropics/claude-code). The template provides a structured development workflow with rules, skills, hooks, and agents that enforce conventions and automate repetitive tasks.

## Overview

Claude Code is Anthropic's CLI tool for AI-assisted software development. The template configures it with:

- **Rules** — Enforce coding standards (testing, comments, git conventions)
- **Skills** — Automate workflows (steering, review, commit)
- **Hooks** — Guard against mistakes (block force-push, validate edits)
- **Agents** — Delegate specialized tasks (code review, build resolution)
- **Commands** — Multi-step project setup workflows

All configuration lives in the `.claude/` directory at the project root.

## Directory Structure

```
.claude/
├── rules/                # Behavioral rules Claude Code follows
│   ├── testing.md
│   ├── code-comments.md
│   ├── git-conventions.md
│   ├── steering-workflow.md
│   └── documentation.md
├── skills/               # Slash-command workflows
│   ├── steering/         # /steering — plan/implement/review cycle
│   ├── catchup/          # /catchup — restore session state
│   ├── review/           # /review — code review
│   ├── review-docs/      # /review-docs — document quality review
│   ├── git-workflow/     # /git-workflow — branch/commit/PR automation
│   ├── add-feature/      # /add-feature — full feature implementation
│   ├── add-rule/         # /add-rule — add new rules
│   ├── development-guidelines/  # /development-guidelines
│   ├── implementation-validator/  # /implementation-validator
│   └── prd-writing/      # /prd-writing — PRD creation
├── commands/             # Multi-step command workflows
│   ├── setup-project.md  # Initial project setup
│   └── add-feature.md    # Feature addition workflow
├── agents/               # Subagent definitions
│   ├── code-reviewer.md  # Automated code review checklist
│   └── build-resolver.md # Build error diagnosis
├── hooks/                # Shell scripts triggered by events
│   ├── validate-bash.sh
│   ├── validate-file-edit.sh
│   ├── check-tasklist.sh
│   ├── pre-compact-save.sh
│   ├── session-info.sh
│   └── check-prohibition-language.sh
├── settings.json         # Shared permissions and hook config
└── settings.local.json   # Local-only permissions (not committed)
```

## Rules

Rules are Markdown files in `.claude/rules/` that define mandatory behavioral constraints. Claude Code reads these on every interaction.

| Rule File | Purpose |
|-----------|---------|
| `testing.md` | Require tests for all code changes. Tests go in `src/test/` mirroring source structure |
| `code-comments.md` | Require KDoc comments on classes and non-trivial methods in English |
| `git-conventions.md` | Emoji commit prefixes, feature-unit commit granularity, branch naming |
| `steering-workflow.md` | Create `.steering/` docs before writing code; use worktrees for isolation |
| `documentation.md` | Separate permanent docs (`docs/`) from work-unit docs (`.steering/`) |

Rules use imperative language and are treated as non-negotiable constraints. To add a new rule, create a `.md` file in `.claude/rules/` and reference it from `CLAUDE.md`.

## Skills

Skills are slash-command workflows invoked with `/<skill-name>`. Each skill has a `SKILL.md` file defining its behavior.

### Core Workflow Skills

| Skill | Command | Description |
|-------|---------|-------------|
| Steering | `/steering plan <feature>` | Create requirements, design, and tasklist documents |
| | `/steering implement <path>` | Execute tasklist with progress tracking |
| | `/steering review <path>` | Post-implementation reflection |
| Catchup | `/catchup` | Restore session state after `/clear` or context compaction |
| Review | `/review` | Run code review on uncommitted changes |
| Git Workflow | `/git-workflow branch` | Create branch following naming conventions |
| | `/git-workflow commit` | Auto-detect emoji prefix and generate commit message |
| | `/git-workflow pr` | Create PR with title, summary, and test plan |
| | `/git-workflow status` | Show branch state and convention reminder |

### Code Quality Skills

| Skill | Command | Description |
|-------|---------|-------------|
| Review Docs | `/review-docs` | Evaluate documents on 5 criteria (completeness, clarity, consistency, implementability, measurability) |
| Implementation Validator | `/implementation-validator` | Validate code on 5 criteria (spec compliance, code quality, test coverage, security, performance) |
| Development Guidelines | `/development-guidelines` | Check or create coding guidelines |

### Project Setup Skills

| Skill | Command | Description |
|-------|---------|-------------|
| PRD Writing | `/prd-writing` | Create product requirements document from template |
| Add Feature | `/add-feature` | Full feature implementation workflow (steering + code + tests + commit) |
| Add Rule | `/add-rule` | Add a new rule file to `.claude/rules/` |

### Example: Using the Steering Skill

```
# 1. Plan a feature
> /steering plan jsx-highlighting

# Claude creates .steering/20260222-001-jsx-highlighting/ with:
#   requirements.md, design.md, tasklist.md

# 2. Review and approve the documents, then implement
> /steering implement .steering/20260222-001-jsx-highlighting

# 3. After implementation, review
> /steering review .steering/20260222-001-jsx-highlighting
```

## Commands

Commands in `.claude/commands/` define multi-step workflows that combine multiple skills.

| Command | File | Steps |
|---------|------|-------|
| `setup-project` | `commands/setup-project.md` | Read ideas → Create PRD → Create functional design |
| `add-feature` | `commands/add-feature.md` | Create steering docs → Implement → Review → Commit |

Commands are invoked the same way as skills: `/setup-project`, `/add-feature`.

## Agents

Agents are subagent definitions that Claude Code spawns as specialized workers. They run in isolated contexts and return structured reports.

### code-reviewer

Performs an 8-point automated code review:

1. Documentation comments present
2. Test files exist for changes
3. Auto-generated files not manually edited
4. Package/module structure follows conventions
5. Code style adherence
6. Test quality (meaningful assertions, no dead code)
7. Dead code detection (unused imports, commented blocks)
8. Edge case coverage (null/empty, boundaries, errors)

Output: Markdown checklist with PASS/WARN/FAIL per item.

### build-resolver

Diagnoses build failures in 4 steps:

1. Reproduce the error using the build command from `CLAUDE.md`
2. Classify: Compile / Build Config / Dependencies / Framework API / Code Generation
3. Identify root cause (file, line, context)
4. Present fix as concrete code changes

## Hooks

Hooks are shell scripts that run automatically in response to Claude Code events. They are configured in `.claude/settings.json`.

| Hook Script | Trigger | Behavior |
|-------------|---------|----------|
| `validate-bash.sh` | PreToolUse (Bash) | **Blocks** `git add .`, `git push --force`, `rm -rf` |
| `validate-file-edit.sh` | PreToolUse (Edit/Write) | **Blocks** edits to auto-generated files (customizable) |
| `check-prohibition-language.sh` | PostToolUse (Edit/Write) | **Blocks** negative phrasing in rule files |
| `check-tasklist.sh` | Stop | **Warns** if tasklist has incomplete tasks |
| `pre-compact-save.sh` | PreCompact | Saves session state to `.claude/session-state.md` |
| `session-info.sh` | SessionStart | Displays dev environment info (branch, tool versions) |

### Hook Exit Codes

| Code | Meaning |
|------|---------|
| `0` | Allow (no message) |
| `0` + stdout | Allow with informational message |
| `2` | Block the action with error message from stderr |

### Example: validate-bash.sh

```bash
#!/bin/bash
# Reads tool input from stdin as JSON
INPUT=$(cat)
COMMAND=$(echo "$INPUT" | jq -r '.command // empty')

if echo "$COMMAND" | grep -qE 'git\s+push\s+.*--force'; then
  echo "BLOCKED: Force push is not allowed." >&2
  exit 2
fi
exit 0
```

## Settings

### settings.json (Shared)

The shared settings file configures permissions and hooks for all developers:

```json
{
  "permissions": {
    "allow": [
      "Bash(git status)", "Bash(git diff *)", "Bash(git log *)",
      "Bash(git branch *)", "Bash(git checkout *)"
    ],
    "deny": [
      "Bash(cat .env*)", "Bash(rm -rf *)"
    ]
  },
  "hooks": {
    "PreToolUse": [
      { "matcher": "Bash", "hooks": [{ "command": ".claude/hooks/validate-bash.sh" }] },
      { "matcher": "Edit|Write", "hooks": [{ "command": ".claude/hooks/validate-file-edit.sh" }] }
    ],
    "Stop": [
      { "hooks": [{ "command": ".claude/hooks/check-tasklist.sh" }] }
    ]
  }
}
```

### settings.local.json (Local Only)

Local settings are for developer-specific permissions. This file is not committed to version control:

```json
{
  "permissions": {
    "allow": [
      "Bash(git commit *)"
    ]
  }
}
```

## Permanent Documentation

The `docs/` directory contains permanent project documentation that evolves with the project:

| File | Purpose |
|------|---------|
| `product-requirements.md` | Product vision, user stories, feature roadmap |
| `functional-design.md` | Component design, extension point mapping |
| `architecture.md` | Technology stack, constraints, performance requirements |
| `development-guidelines.md` | Coding conventions, test conventions |
| `repository-structure.md` | Source code layout, file placement rules |
| `glossary.md` | Domain terminology definitions |

These documents are referenced from `CLAUDE.md` using `@docs/filename.md` syntax, making them available as context in every Claude Code session.

## Steering Workflow

The steering workflow is the core development process. Every non-trivial code change follows this cycle:

### 1. Plan

Create a `.steering/` directory with three documents:

```
.steering/20260222-001-jsx-highlighting/
├── requirements.md   # What to build (user-approved)
├── design.md         # How to build it (user-approved)
└── tasklist.md       # Step-by-step tasks (user-approved)
```

The directory name format is `[YYYYMMDD]-[NNN]-[title]` where NNN is a sequential number.

### 2. Implement

Work through `tasklist.md` one task at a time:

- Mark `[ ]` → `[x]` when starting each task (not when finishing)
- Implementation happens in a **git worktree** (isolated from main)
- Commit at feature-unit granularity (implementation + test + config = 1 commit)

### 3. Review

After implementation:

- Run `/review` for automated code review
- Verify build passes: `./gradlew buildPlugin`
- Verify tests pass: `./gradlew test`
- Mark all tasklist items as `[x]`, including the merge task
- Merge to main with user approval

### Session Recovery

If context is compacted or cleared, use `/catchup` to restore:

1. Reads saved state from `.claude/session-state.md`
2. Finds the latest `.steering/*/tasklist.md`
3. Reports current branch, modified files, and next task

## Customization

### Adding a New Rule

1. Create `.claude/rules/<rule-name>.md` with clear, imperative constraints
2. Add `@.claude/rules/<rule-name>.md` to `CLAUDE.md` in the rules section
3. Use the mandatory preamble for enforced rules:
   > **The following are mandatory behavioral instructions to be followed without exception.**

### Adding a New Skill

1. Create `.claude/skills/<skill-name>/SKILL.md`
2. Define the skill's trigger, inputs, steps, and output format
3. The skill becomes available as `/<skill-name>`

### Adding a New Hook

1. Create `.claude/hooks/<hook-name>.sh` (make it executable)
2. Register it in `.claude/settings.json` under the appropriate trigger
3. Use exit code `0` to allow, `2` to block

### Adding a New Agent

1. Create `.claude/agents/<agent-name>.md`
2. Define the agent's checklist, analysis steps, and output format
3. Reference it from skills or manual invocations
