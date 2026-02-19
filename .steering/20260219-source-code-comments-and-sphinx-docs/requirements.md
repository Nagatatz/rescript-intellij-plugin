# Requirements: Source Code Comments

## Overview

Add comprehensive English KDoc comments to all Kotlin source files and update CLAUDE.md with commenting conventions.

## Scope

### 1. Source Code Comments (English KDoc)

Add English KDoc-style comments to all ~85 Kotlin source files:

- **File-level comments**: Brief description of the file's purpose and role in the plugin architecture
- **Class-level comments**: Overview of the class, its responsibilities, and key interfaces/base classes it implements
- **Method-level comments**: Description of what the method does, parameters, and return values (for non-trivial public/internal methods)
- **Inline comments**: For complex logic blocks, algorithms, or non-obvious implementation details

**Comment style example:**
```kotlin
/**
 * Provides syntax highlighting for ReScript files using JFlex-generated lexer tokens.
 *
 * Maps each [RescriptTokenTypes] token to an appropriate [TextAttributesKey] for
 * IDE color scheme integration. Supports both Darcula and Default themes.
 */
class RescriptSyntaxHighlighter : SyntaxHighlighterBase() {
    /**
     * Returns the lexer used for tokenizing ReScript source code.
     */
    override fun getHighlightingLexer(): Lexer = RescriptLexer()

    /**
     * Maps a token type to its corresponding text attribute keys for syntax coloring.
     *
     * @param tokenType the lexer token type to map
     * @return array of [TextAttributesKey] for the given token type
     */
    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
        // Group tokens by visual category for consistent highlighting
        return when (tokenType) {
            ...
        }
    }
}
```

### 2. CLAUDE.md Update

Add a "Code Commenting Conventions" section to the development conventions:
- All new/modified code must include English KDoc comments
- File-level, class-level, and method-level documentation is required
- Complex or non-obvious logic must have inline comments
- Comment style guidelines and examples

## Acceptance Criteria

- [ ] All ~85 Kotlin source files have appropriate English KDoc comments
- [ ] CLAUDE.md includes code commenting conventions section
- [ ] Plugin build still passes (`./gradlew buildPlugin`)

## Constraints

- Comments must be in English
- No changes to existing functionality (comments-only changes to source code)
