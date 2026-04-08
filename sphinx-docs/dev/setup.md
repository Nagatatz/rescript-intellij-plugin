---
myst:
  html_meta:
    "keywords": "development setup, prerequisites, IDE configuration"
---

# Development Setup

## Prerequisites

| Requirement | Version |
|-------------|---------|
| JDK | 21 or later |
| IntelliJ IDEA | 2025.3+ (Ultimate or Community) |
| Git | Any recent version |
| Node.js | For testing LSP features |

## Clone the Repository

```bash
git clone https://github.com/Nagatatz/rescript-intellij-plugin.git
cd rescript-intellij-plugin
```

## Open in IntelliJ IDEA

1. Open IntelliJ IDEA
2. **File** → **Open** → Select the cloned repository
3. IntelliJ will detect the Gradle project and import it automatically
4. Wait for the Gradle sync to complete (this downloads dependencies)

## Verify the Setup

Run the build to ensure everything is configured correctly:

```bash
./gradlew buildPlugin
```

This will:
1. Generate the JFlex lexer (`RescriptFlexLexer.java`) from `Rescript.flex`
2. Compile all Kotlin source code
3. Run ktlint checks
4. Package the plugin

## Run a Development IDE Instance

```bash
./gradlew runIde
```

This launches a fresh IntelliJ IDEA instance with the plugin loaded. You can open a ReScript project in this instance to test your changes.

## Project JDK Configuration

If IntelliJ doesn't automatically detect your JDK:

1. **File** → **Project Structure** → **Project**
2. Set **SDK** to JDK 21+
3. Set **Language level** to 21

## Recommended IDE Plugins

For development, consider installing these plugins in your development IDE:

- **Kotlin** — Should be bundled
- **Gradle** — Should be bundled
- **Grammar-Kit** — Helpful for working with JFlex/BNF grammars
