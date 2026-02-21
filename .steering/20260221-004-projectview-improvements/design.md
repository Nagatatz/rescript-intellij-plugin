# Design: Project View improvements for .res.js files

## Architecture

Two lightweight extension point implementations in a new `projectview/` package.

## Component Design

### 1. RescriptFileNestingProvider

- **Extension Point**: `com.intellij.projectViewNestingRulesProvider`
- **Interface**: `ProjectViewNestingRulesProvider`
- **Responsibility**: Register suffix-based nesting rules so `.res.js` files appear as children of `.res` files.

```kotlin
class RescriptFileNestingProvider : ProjectViewNestingRulesProvider {
    override fun addFileNestingRules(consumer: ProjectViewNestingRulesProvider.Consumer) {
        consumer.addNestingRule(".res", ".res.js")
        consumer.addNestingRule(".resi", ".resi.js")
    }
}
```

### 2. RescriptCompiledJsNodeDecorator

- **Extension Point**: `com.intellij.projectViewNodeDecorator`
- **Interface**: `ProjectViewNodeDecorator`
- **Responsibility**: Override the text color of `.res.js` / `.resi.js` nodes to gray.

```kotlin
class RescriptCompiledJsNodeDecorator : ProjectViewNodeDecorator {
    override fun decorate(node: ProjectViewNode<*>, data: PresentationData) {
        val virtualFile = node.virtualFile ?: return
        val name = virtualFile.name
        if (name.endsWith(".res.js") || name.endsWith(".resi.js")) {
            data.setForcedTextForeground(GRAY_COLOR)
        }
    }
}
```

- Uses `JBColor` for theme-aware gray color.
- `setForcedTextForeground` overrides VCS coloring.

## Extension Registration (plugin.xml)

```xml
<!-- File nesting (.res.js under .res) -->
<projectViewNestingRulesProvider
    implementation="com.rescript.plugin.projectview.RescriptFileNestingProvider"/>

<!-- Gray out compiled .res.js files -->
<projectViewNodeDecorator
    implementation="com.rescript.plugin.projectview.RescriptCompiledJsNodeDecorator"/>
```

## File Layout

```
src/main/kotlin/com/rescript/plugin/projectview/
├── RescriptFileNestingProvider.kt
└── RescriptCompiledJsNodeDecorator.kt

src/test/kotlin/com/rescript/plugin/projectview/
├── RescriptFileNestingProviderTest.kt
└── RescriptCompiledJsNodeDecoratorTest.kt
```

## Testing Strategy

- **RescriptFileNestingProviderTest**: Verify that `addFileNestingRules` registers the expected `.res` → `.res.js` and `.resi` → `.resi.js` rules.
- **RescriptCompiledJsNodeDecoratorTest**: Verify that `decorate` sets gray color only for `.res.js` / `.resi.js` files and does not affect other files.
