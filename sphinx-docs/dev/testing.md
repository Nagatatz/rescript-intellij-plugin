# Testing Guide

## Running Tests

```bash
# Run all tests
./gradlew test

# Run tests with coverage report
./gradlew test koverHtmlReport

# View coverage report
open build/reports/kover/html/index.html
```

## Test Structure

Tests are located in `src/test/kotlin/com/rescript/plugin/` and mirror the source code package structure:

```
src/test/kotlin/com/rescript/plugin/
├── highlight/
│   ├── RescriptSyntaxHighlighterTest.kt
│   └── RescriptBraceMatcherTest.kt
├── lang/
│   ├── RescriptLexerTest.kt
│   └── RescriptParserTest.kt
├── folding/
│   └── RescriptFoldingBuilderTest.kt
├── ...
```

## Writing Tests

### Test Conventions

- **File naming:** `<TargetClass>Test.kt`
- **Location:** Same package as the class being tested
- **Framework:** JUnit 5 with IntelliJ test fixtures

### IntelliJ Test Fixtures

Most tests extend IntelliJ Platform test base classes:

```kotlin
class RescriptMyFeatureTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String = "src/test/testData/myfeature"

    fun testBasicCase() {
        // Configure test fixture with a ReScript file
        myFixture.configureByText("Test.res", "let x = 1")
        // Invoke the feature and assert results
    }
}
```

### Test Data Files

Test data files go in `src/test/testData/<feature>/`:

```
src/test/testData/
├── folding/
│   ├── module.res
│   └── comments.res
├── highlighting/
│   └── keywords.res
└── ...
```

### Lexer Tests

Test that the lexer produces correct token sequences:

```kotlin
fun testKeywords() {
    val lexer = RescriptLexer()
    lexer.start("let x = 1")
    assertEquals(RescriptTokenTypes.LET, lexer.tokenType)
    lexer.advance()
    // ... continue checking tokens
}
```

### Parser Tests

Test that the parser produces correct PSI structures:

```kotlin
fun testLetDeclaration() {
    myFixture.configureByText("Test.res", "let x = 1")
    val file = myFixture.file
    val declarations = PsiTreeUtil.findChildrenOfType(file, RescriptLetDeclaration::class.java)
    assertEquals(1, declarations.size)
}
```

## Integration Tests

Integration tests use IntelliJ Platform's test infrastructure to verify features in a realistic IDE environment, including PSI trees, editor fixtures, and file-based test data.

### Unit Tests vs Integration Tests

| Aspect | Unit Tests | Integration Tests |
|--------|-----------|------------------|
| Base class | None or plain JUnit | `BasePlatformTestCase` |
| Speed | Fast (no IDE bootstrap) | Slower (IDE platform initialized) |
| Scope | Single class / function | Feature end-to-end in IDE context |
| Use when | Testing pure logic (lexer tokens, utility functions) | Testing features that interact with PSI, editor, or fixtures |

### BasePlatformTestCase Pattern

Integration tests extend `BasePlatformTestCase`, which bootstraps a lightweight IDE environment with an editor, PSI infrastructure, and project model:

```kotlin
class RescriptMyFeatureIntegrationTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String = "src/test/testData/myfeature"

    fun testFeatureWithInlineCode() {
        // Configure editor with inline ReScript code
        myFixture.configureByText("Test.res", """
            let x = 1
            module M = {
              let y = 2
            }
        """.trimIndent())

        // Invoke the feature and assert results
        // e.g., myFixture.testStructureView { ... }
    }

    fun testFeatureWithTestDataFile() {
        // Load a test data file from getTestDataPath()
        myFixture.configureByFile("Example.res")
        // Assert against the loaded file
    }
}
```

Key methods:

- `myFixture.configureByText(filename, content)` — Create an in-memory file and open it in the editor
- `myFixture.configureByFile(filename)` — Load a file from the `testDataPath` directory
- `myFixture.testStructureView { view -> ... }` — Open the structure view and assert its contents
- `myFixture.testHighlighting(...)` — Run highlighting and check annotations

### Integration Test Classes

The project includes the following integration test categories:

| Test Class | Feature | Example Assertion |
|-----------|---------|------------------|
| `RescriptHighlightingIntegrationTest` | Syntax highlighting | Token colors match expected attributes |
| `RescriptFoldingBuilderIntegrationTest` | Code folding | Fold regions match `<fold>` markers in test data |
| `RescriptStructureViewIntegrationTest` | Structure view | Tree contains expected declarations |
| `RescriptIndentIntegrationTest` | Auto-indentation | Indent levels after Enter key |
| `RescriptParserIntegrationTest` | Parser + PSI tree | PSI tree matches expected structure |
| `RescriptLexerIntegrationTest` | Lexer token stream | Full-file token sequences match expectations |

### testData Directory

Test data files are organized by feature under `src/test/testData/`:

```
src/test/testData/
├── folding/           # .res files with <fold> markers
├── highlighting/      # .res files for highlighting tests
├── structure/         # .res files for structure view tests
├── indent/            # .res files for indentation tests
├── parser/            # .res files for parser tests
└── lexer/             # .res files for lexer tests
```

Each test class points to its data directory via `getTestDataPath()`. Test data files are plain `.res` files, sometimes with special markers (e.g., `<fold text="...">`) for framework-assisted assertions.

## Test Coverage

The project uses [Kover](https://github.com/Kotlin/kotlinx-kover) for code coverage.

```bash
# Generate HTML report
./gradlew koverHtmlReport

# Generate XML report (used by CI)
./gradlew koverXmlReport
```

Coverage reports are generated at `build/reports/kover/html/`.

### Coverage Requirements

- New code should have test coverage
- CI reports coverage on pull requests
- Exceptions: UI components (Swing), LSP integration classes that require a running server

## CI Testing

The CI pipeline runs tests automatically on every push and PR:

1. `./gradlew test koverXmlReport koverHtmlReport`
2. Coverage is reported on PRs via the Kover report action
3. Test results are uploaded as artifacts
