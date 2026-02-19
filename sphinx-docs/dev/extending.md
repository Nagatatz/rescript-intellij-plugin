# Extending the Plugin

This guide explains how to add new features to the plugin, following established patterns.

## General Workflow

1. **Create a new Kotlin file** in the appropriate package under `src/main/kotlin/com/rescript/plugin/`
2. **Implement the IntelliJ Platform extension point** interface
3. **Register in `plugin.xml`** under the correct extension point
4. **Add tests** in `src/test/kotlin/com/rescript/plugin/`
5. **Build and test**: `./gradlew buildPlugin`

## Common Extension Point Patterns

### Adding a New Inspection

Inspections analyze code and report problems.

```kotlin
package com.rescript.plugin.analysis

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor

class RescriptMyInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        // Return a visitor that checks PSI elements
    }
}
```

Register in `plugin.xml`:

```xml
<localInspection
    language="ReScript"
    groupName="ReScript"
    displayName="My inspection"
    enabledByDefault="true"
    level="WARNING"
    implementationClass="com.rescript.plugin.analysis.RescriptMyInspection"/>
```

### Adding a New Intention Action

Intentions provide quick actions via `Alt+Enter`.

```kotlin
package com.rescript.plugin.intention

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

class RescriptMyIntention : IntentionAction {
    override fun getText(): String = "My action"
    override fun getFamilyName(): String = "ReScript"
    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean { ... }
    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) { ... }
    override fun startInWriteAction(): Boolean = true
}
```

Register in `plugin.xml`:

```xml
<intentionAction>
    <language>ReScript</language>
    <className>com.rescript.plugin.intention.RescriptMyIntention</className>
    <category>ReScript</category>
</intentionAction>
```

### Adding a New Action

Actions appear in menus and can have keyboard shortcuts.

```kotlin
package com.rescript.plugin.navigation

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class RescriptMyAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) { ... }
    override fun update(e: AnActionEvent) { ... }
}
```

Register in `plugin.xml`:

```xml
<action id="Rescript.MyAction"
        class="com.rescript.plugin.navigation.RescriptMyAction"
        text="My Action"
        description="Description of my action">
    <add-to-group group-id="EditorPopupMenu" anchor="last"/>
    <keyboard-shortcut keymap="$default" first-keystroke="alt shift M"/>
</action>
```

### Adding New Lexer Tokens

To add a new token type to the lexer:

1. **Edit `Rescript.flex`** — Add the token rule
2. **Edit `RescriptTokenTypes.kt`** — Define the `IElementType` constant and add it to the appropriate `TokenSet`
3. **Edit `RescriptSyntaxHighlighter.kt`** — Map the token to a `TextAttributesKey`
4. **Build** — `./gradlew buildPlugin` to regenerate the lexer

### Adding a Postfix Template

```kotlin
// Add to RescriptPostfixTemplateProvider.kt
class MyPostfixTemplate(provider: PostfixTemplateProvider) :
    PostfixTemplateWithExpressionSelector(
        "mytemplate",
        "expr.mytemplate",
        "Description",
        RescriptExpressionSelector(),
        provider
    ) {
    override fun expandForChooseExpression(expression: PsiElement, editor: Editor) {
        // Transform the expression
    }
}
```

## File Naming Conventions

- Classes: `Rescript<Feature><Type>.kt` (e.g., `RescriptFoldingBuilder.kt`)
- Tests: `Rescript<Feature><Type>Test.kt` (e.g., `RescriptFoldingBuilderTest.kt`)
- Package: Match the feature category (e.g., `folding/`, `navigation/`, `analysis/`)

## KDoc Requirements

All classes and non-trivial public methods must have KDoc comments in English. See the [Contributing Guide](contributing.md) for details.
