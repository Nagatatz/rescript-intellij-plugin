---
myst:
  html_meta:
    "keywords": "extending, adding features, extension points, plugin development"
---

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

### Adding a Code Vision Provider

Code Vision providers display inline annotations (e.g., type signatures) above code elements.

```kotlin
package com.rescript.plugin.codevision

import com.intellij.codeInsight.hints.codeVision.DaemonBoundCodeVisionProvider
import com.intellij.codeInsight.hints.codeVision.CodeVisionAnchorKind
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile

class RescriptMyCodeVisionProvider : DaemonBoundCodeVisionProvider {
    override val id: String = "rescript.myVision"
    override val name: String = "My Vision"
    override val defaultAnchor: CodeVisionAnchorKind = CodeVisionAnchorKind.Top

    override fun computeForEditor(
        editor: Editor,
        file: PsiFile,
    ): List<Pair<TextRange, CodeVisionEntry>> {
        // Compute entries to display above code elements
        return listOf(
            Pair(range, TextCodeVisionEntry("display text", id))
        )
    }
}
```

Register in `plugin.xml`:

```xml
<codeInsight.daemonBoundCodeVisionProvider
    implementation="com.rescript.plugin.codevision.RescriptMyCodeVisionProvider"/>
```

### Adding a Tool Window

Tool windows provide persistent UI panels in the IDE sidebar or bottom bar.

```kotlin
package com.rescript.plugin.typeinfo

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

class RescriptMyToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = MyPanel(project)
        val content = toolWindow.contentManager.factory
            .createContent(panel, null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project): Boolean = true
}
```

Register in `plugin.xml`:

```xml
<toolWindow id="My Tool Window"
            anchor="bottom"
            secondary="true"
            factoryClass="com.rescript.plugin.typeinfo.RescriptMyToolWindowFactory"
            icon="/icons/rescript-file.svg"/>
```

### Adding a PSI Stub Index

Stub indexes enable fast symbol lookup without parsing entire files. The plugin uses `StringStubIndexExtension` for name-based indexes and `FileBasedIndexExtension` for content-based indexes.

**Name index** (lookup symbols by name):

```kotlin
package com.rescript.plugin.indexing

import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey

class RescriptMyIndex : StringStubIndexExtension<RescriptDeclarationPsiElement>() {
    companion object {
        val KEY: StubIndexKey<String, RescriptDeclarationPsiElement> =
            StubIndexKey.createIndexKey("rescript.my.index")
    }

    override fun getKey() = KEY
    override fun getVersion(): Int = 1
}
```

Register in `plugin.xml`:

```xml
<stubIndex implementation="com.rescript.plugin.indexing.RescriptMyIndex"/>
```

**File-based index** (index file content for queries):

```kotlin
class RescriptMyFileIndex : FileBasedIndexExtension<String, Void>() {
    companion object {
        val NAME: ID<String, Void> = ID.create("rescript.my.file.index")
    }

    override fun getName() = NAME
    override fun getIndexer(): DataIndexer<String, Void, FileContent> {
        return DataIndexer { inputData ->
            // Scan file content and return key-value map
            mapOf("key" to null)
        }
    }
    override fun getKeyDescriptor() = EnumeratorStringDescriptor()
    override fun getValueExternalizer() = VoidDataExternalizer()
    override fun getVersion(): Int = 1
    override fun getInputFilter() = FileBasedIndex.InputFilter { file ->
        file.fileType == RescriptFileType.INSTANCE
    }
    override fun dependsOnFileContent(): Boolean = true
}
```

Register in `plugin.xml`:

```xml
<fileBasedIndex implementation="com.rescript.plugin.indexing.RescriptMyFileIndex"/>
```

### Adding LSP Custom Requests

Extend the LSP server interface with custom request methods using `@JsonRequest` annotations.

```kotlin
package com.rescript.plugin.lsp

import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest
import org.eclipse.lsp4j.services.LanguageServer
import java.util.concurrent.CompletableFuture

interface RescriptLanguageServer : LanguageServer {
    @JsonRequest("textDocument/createInterface")
    fun createInterface(params: TextDocumentIdentifier): CompletableFuture<TextDocumentIdentifier>

    @JsonRequest("textDocument/openCompiled")
    fun openCompiled(params: TextDocumentIdentifier): CompletableFuture<TextDocumentIdentifier>
}
```

Custom requests are defined as interface methods, not registered in `plugin.xml`. The interface is used when obtaining the LSP server proxy via `LspServerDescriptor`:

```kotlin
val server = lspServerDescriptor.getServer() as? RescriptLanguageServer
val result = server?.createInterface(params)?.get()
```

### Adding a Paste Processor

Paste processors transform clipboard content when pasting into the editor.

```kotlin
package com.rescript.plugin.paste

import com.intellij.codeInsight.editorActions.CopyPastePostProcessor
import com.intellij.codeInsight.editorActions.TextBlockTransferableData
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import java.awt.datatransfer.Transferable

class RescriptMyPasteProcessor : CopyPastePostProcessor<TextBlockTransferableData>() {
    override fun collectTransferableData(
        file: PsiFile, editor: Editor,
        startOffsets: IntArray, endOffsets: IntArray,
    ): List<TextBlockTransferableData> = emptyList()

    override fun extractTransferableData(
        content: Transferable,
    ): List<TextBlockTransferableData> {
        // Check clipboard content and return data if applicable
    }

    override fun processTransferableData(
        project: Project, editor: Editor,
        bounds: RangeMarker, caretOffset: Int,
        indented: Ref<in Boolean>,
        values: MutableList<out TextBlockTransferableData>,
    ) {
        // Transform the pasted content
        WriteCommandAction.runWriteCommandAction(project) {
            editor.document.replaceString(bounds.startOffset, bounds.endOffset, transformed)
        }
    }
}
```

Register in `plugin.xml`:

```xml
<copyPastePostProcessor
    implementation="com.rescript.plugin.paste.RescriptMyPasteProcessor"/>
```

## File Naming Conventions

- Classes: `Rescript<Feature><Type>.kt` (e.g., `RescriptFoldingBuilder.kt`)
- Tests: `Rescript<Feature><Type>Test.kt` (e.g., `RescriptFoldingBuilderTest.kt`)
- Package: Match the feature category (e.g., `folding/`, `navigation/`, `analysis/`)

## KDoc Requirements

All classes and non-trivial public methods must have KDoc comments in English. See the [Contributing Guide](contributing.md) for details.
