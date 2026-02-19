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
