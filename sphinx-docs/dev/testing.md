---
myst:
  html_meta:
    "keywords": "testing, unit tests, test fixtures, coverage"
---

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

### Full and Managed populations

Kover produces two reports from the same unit `test` execution. `koverXmlReportFull` / `koverHtmlReportFull` have no explicit class or package exclusions and write `build/reports/kover/full.xml` / `full-html/`. `koverXmlReport` / `koverHtmlReport` retain the Managed report and its existing 87% ratchet. Neither percentage describes compiler semantics.

CI publishes covered/missed/total executable lines, JVM classes and represented source files for both populations, together with their excluded difference. Main Kotlin/Java source inventory is shown separately: generated/type-only code and Kover bytecode/source mapping make source-file counts different from executable-line denominators. Never label a Managed percentage as overall coverage.

Managed exclusions are exact outer class names plus their nested JVM classes in `config/quality/coverage-exclusions.tsv`, with a reason for each class. Package-wide exclusions are forbidden; new classes enter the denominator by default. Legacy exclusions awaiting a behavior fixture are explicitly labeled as unverified, rather than claimed to be untestable. `integrationTest`, `integrationIdeTest` and `uiTest` are excluded from unit instrumentation and never implicitly started by reporting.

### Operation behavior and mutation evidence

`TransformationBehaviorFixture` applies the registered SDK action or public handler, checks exact Document text, commits and checks PSI text, and requires one undo to restore the complete source. An optional observation compiles and executes before, after and undo snapshots only in the dedicated CLI task; committed PSI alone is not a syntax or semantic proof. Unsupported operations can be checked for unchanged source and observation. Callers supply a project content root and preserve standard SDK writable preparation.

`config/quality/fixtures.json` distinguishes API/reflection presence contracts from Document/undo and CLI/runtime behavior for issues #99–105. CI counts only actual JUnit XML cases, keeps failed/error/skipped cases separate, and reports fixtures absent from a stack or not run as such. Other tests remain unclassified rather than automatically called behavior tests. Fixture case counts are not program counts.

CI uses clean/rerun task execution and records `QUALITY_RESULTS_AFTER` before the unit or CLI command. The summary rejects JUnit XML whose file timestamp precedes that execution boundary. Source existence alone does not detect older results for unchanged source, and timestamps alone cannot authenticate copied/cached XML; the clean/rerun CI command is the fresh-execution basis. Local reports without a boundary must not be described as current-run execution evidence.

PIT uses the single target inventory `config/quality/pit-targets.json`: only explicitly declared pure JVM paths/regex/workspace-glob utilities are targets because the PIT minion lacks the injected IntelliJ SDK. CI reads PIT line coverage from its actual HTML project summary and checks its class/mutation denominators against XML. Line coverage is never inferred from mutation counts. CI shows configured and actually mutated classes, generated mutation count, completed status counts, mutation score (detected / generated), test strength (detected / mutations with coverage), and assertion kill ratio (KILLED / generated). PIT treats timeout, non-viable/error and equivalent outcomes as detected; status counts expose these separately. Zero mutations or incomplete/unknown outcomes fail reporting. The constants-only paths/regex objects have no default-mutator candidates; executable workspace-glob branches supply actual mutations. Standard JVM bytecode mutators are used without a Kotlin-specific plugin, so these counts are not a source-level Kotlin mutation guarantee. These scores do not claim mutation coverage for IDE or compiler fixtures.


The Kover [variant/filter documentation](https://kotlin.github.io/kotlinx-kover/gradle-plugin/#filtering-reports) defines report populations; PIT's [statistics implementation](https://github.com/hcoles/pitest/blob/master/pitest-entry/src/main/java/org/pitest/mutationtest/statistics/MutationStatistics.java) and [status definitions](https://github.com/hcoles/pitest/blob/master/pitest/src/main/java/org/pitest/mutationtest/DetectionStatus.java) define score and detection semantics.

```bash
# Generate both unit populations without integration installs.
./gradlew test koverVerify koverXmlReport koverHtmlReport koverXmlReportFull koverHtmlReportFull --no-parallel --max-workers=1
python3 tools/quality/report.py coverage --full build/reports/kover/full.xml --managed build/reports/kover/report.xml

# Pure JVM mutation task, then report actual XML outcomes.
./gradlew pitest --no-parallel --max-workers=1
python3 tools/quality/report.py pit
```

## UI Tests (Remote-Robot)

The project includes UI tests powered by [IntelliJ Remote-Robot](https://github.com/JetBrains/intellij-ui-test-robot) for end-to-end IDE testing and automated Marketplace screenshot capture.

### Prerequisites

- macOS: Grant accessibility permissions to Java (System Settings > Privacy & Security > Accessibility)
- The sample project at `src/uiTest/testData/sample-project/` must have its npm dependencies installed:

```bash
cd src/uiTest/testData/sample-project
npm install
```

### Running UI Tests

UI tests require a two-step process — the IDE must be running before tests can connect:

```bash
# Terminal 1: Start the IDE with Remote-Robot server (port 8082)
./gradlew runIdeForUiTests

# Terminal 2: Open the sample project in the IDE, then run the tests
./gradlew uiTest
```

### Screenshot Output

The `MarketplaceScreenshotTest` class captures 11 screenshots demonstrating key plugin features. Screenshots are saved to `build/screenshots/`:

| Screenshot | Feature |
|-----------|---------|
| `01-syntax-highlighting.png` | ReScript code colorization |
| `02-code-completion.png` | LSP completion popup |
| `03-error-lens.png` | Inline error/warning display |
| `04-inlay-hints.png` | Type inference hints |
| `05-structure-view.png` | File symbol tree |
| `06-code-vision.png` | Function type annotations |
| `07-jsx-support.png` | JSX syntax highlighting |
| `08-project-view.png` | .resi nesting and compiled JS |
| `09-quick-fix-intention.png` | Alt+Enter intention menu |
| `10-hover-documentation.png` | Type info and documentation |
| `11-repl.png` | Interactive REPL tool window |

### Test Structure

UI tests are in a separate source set (`src/uiTest/`) and do not run with `./gradlew test`:

```
src/uiTest/kotlin/com/rescript/plugin/uitest/
├── UiTestBase.kt                          # Base class (connection, screenshots)
├── fixtures/
│   └── IdeFixtures.kt                     # IDE component fixtures
└── screenshot/
    └── MarketplaceScreenshotTest.kt       # Marketplace screenshot capture
```

### Notes

- UI tests are **not included in CI** (requires a display)
- The `uiTest` source set is excluded from Kover coverage
- Screenshots use Darcula theme for Marketplace appeal

## CI Testing

The CI pipeline runs tests automatically on every push and PR:

1. `./gradlew test koverXmlReport koverHtmlReport`
2. Coverage is reported on PRs via the Kover report action
3. Test results are uploaded as artifacts

## IDE-backed CLI regression tests

IDE-backed CLI regressions in `src/integrationTest` run through `integrationIdeTest`, registered with the official `intellijPlatformTesting.testIde` API. It preserves the injected SDK/runtime classpath, creates a dedicated sandbox and disables the bundled Ultimate plugin for the flattened test classpath. Select one suite with `./gradlew integrationIdeTest --tests '*RescriptReplIntegrationTest' --no-parallel --max-workers=1`. The separate `integrationTest` task runs only wizard template suites. Kover reports do not implicitly launch integration or UI tasks.
