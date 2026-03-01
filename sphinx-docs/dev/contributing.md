# Contributing Guide

Thank you for your interest in contributing to the ReScript IntelliJ Plugin!

## Getting Started

1. Fork the repository on GitHub
2. Clone your fork:
   ```bash
   git clone https://github.com/<your-username>/rescript-intellij-plugin.git
   ```
3. Set up your [development environment](setup.md)
4. Create a feature branch:
   ```bash
   git checkout -b feature/my-feature
   ```

## Development Workflow

### Branch Naming

| Prefix | Use |
|--------|-----|
| `feature/` | New features |
| `fix/` | Bug fixes |
| `refactor/` | Code refactoring |
| `docs/` | Documentation |
| `test/` | Test additions |
| `chore/` | Configuration, dependencies |

### Commit Messages

Use emoji prefixes for commit messages:

| Emoji | Use |
|-------|-----|
| ✨ | New feature |
| 🐛 | Bug fix |
| ♻️ | Refactoring |
| 📝 | Documentation |
| 🎨 | UI/style improvement |
| ⚡ | Performance improvement |
| 🔧 | Configuration change |
| ✅ | Test addition/fix |
| 🗑️ | Code deletion |

**Format:** `<emoji> <verb> <concise description>`

**Examples:**
- `✨ Add JSX token support to lexer`
- `🐛 Fix nested comment parsing`
- `📝 Update architecture documentation`

## Code Conventions

### Language

- All source code is written in **Kotlin**
- Lexer definition is in **JFlex** (generates Java)

### Package Structure

All classes go under `com.rescript.plugin.*`, organized by feature:

```
com.rescript.plugin.highlight/   # Syntax highlighting
com.rescript.plugin.lsp/         # LSP integration
com.rescript.plugin.navigation/  # Navigation features
...
```

### Code Style

- ktlint is enforced via CI (`./gradlew ktlintCheck`)
- Auto-fix: `./gradlew ktlintFormat`
- IntelliJ's built-in Kotlin formatter generally matches ktlint

### KDoc Comments

All classes and significant methods must have KDoc comments in **English**:

```kotlin
/**
 * Provides code folding for ReScript files.
 *
 * Recognizes multi-line declarations, block comments,
 * and custom //#region markers.
 *
 * @see RescriptCustomFoldingProvider for region-based folding
 */
class RescriptFoldingBuilder : CustomFoldingBuilder() {
    /**
     * Builds fold regions for the given PSI element.
     *
     * @param root the root PSI element to scan
     * @param descriptors list to add fold descriptors to
     * @param document the document being folded
     */
    override fun buildLanguageFoldRegions(...) { ... }
}
```

### Testing

- Every code change must have corresponding tests
- Test file naming: `<ClassName>Test.kt`
- Tests mirror the source package structure
- See the [Testing Guide](testing.md) for details

## AI-Assisted Development

This project uses structured workflows for AI-assisted development with Claude Code. The following configuration files define the development process:

- **`.claude/rules/steering-workflow.md`** — Steering workflow requiring `requirements.md`, `design.md`, and `tasklist.md` before implementation
- **`.claude/rules/definition-of-done.md`** — 5-phase Definition of Done (Planning → Implementation → Pre-commit → Pre-merge → Post-merge)
- **`.claude/rules/git-conventions.md`** — Git worktree isolation for feature branches, emoji commit prefixes, and branch naming conventions

Steering documents are stored in `.steering/[YYYYMMDD]-[NNN]-[title]/` directories and committed alongside code changes.

## Submitting Changes

1. Ensure all tests pass:
   ```bash
   ./gradlew test
   ```

2. Ensure code style checks pass:
   ```bash
   ./gradlew ktlintCheck
   ```

3. Build the plugin:
   ```bash
   ./gradlew buildPlugin
   ```

4. Push your branch and create a Pull Request

### PR Guidelines

- Keep PRs focused on a single feature or fix
- Write a clear description of what changed and why
- Include screenshots for UI changes
- Reference related issues if applicable

## Questions?

- Open an [issue](https://github.com/Nagatatz/rescript-intellij-plugin/issues) on GitHub
- Check existing issues and discussions
