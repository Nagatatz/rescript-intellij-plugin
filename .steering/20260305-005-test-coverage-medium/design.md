# Design: Test Coverage — Medium Tests

## 1. RescriptStringLiteralTest (BasePlatformTestCase)

Uses `myFixture.configureByText()` to create a file with string literals, then finds `RescriptStringLiteral` PSI elements in the tree.

### Test Cases

| Test | Description |
|------|-------------|
| `testIsValidHost` | Verify `isValidHost()` always returns true |
| `testLiteralTextEscaperDecode` | Verify decode copies text verbatim |
| `testLiteralTextEscaperOffsetInHost` | Verify offset mapping is identity + rangeStart |
| `testLiteralTextEscaperIsOneLine` | Verify `isOneLine()` returns false |

### Pattern

```kotlin
class RescriptStringLiteralTest : BasePlatformTestCase() {
    private fun findStringLiteral(): RescriptStringLiteral {
        val file = myFixture.configureByText("Test.res", "let s = \"hello\"")
        // Walk PSI tree to find RescriptStringLiteral leaf
    }
}
```

## 2. DtsParserProcessTest (JUnit)

Tests `extractScript()` — resource extraction, caching, and file existence.

### Test Cases

| Test | Description |
|------|-------------|
| `testExtractScriptReturnsTempFile` | extractScript() returns a Path that exists |
| `testExtractScriptFileContainsContent` | Extracted file has non-empty content |
| `testExtractScriptCachesResult` | Second call returns same Path |

### Notes

- `extractScript()` uses `getResourceAsStream(SCRIPT_RESOURCE)` which loads from classpath
- The script `/scripts/dts-to-json.js` is in `src/main/resources/scripts/`
- Need to reset `cachedScriptPath` via reflection between tests since it's `@Volatile private`

## 3. RescriptGenerateActionUtilTest (BasePlatformTestCase)

Tests `findEnclosingDeclaration()` and `isInsideDeclaration()` using fixture.

### Test Cases

| Test | Description |
|------|-------------|
| `testFindEnclosingLetDeclaration` | Caret inside let binding, finds LET_DECLARATION |
| `testFindEnclosingReturnsNullOutsideDeclaration` | Caret at whitespace, returns null |
| `testIsInsideTypeDeclaration` | Caret inside type decl, returns true |
| `testIsInsideDeclarationReturnsFalseForNonRescriptFile` | Non-.res file returns false |

### Pattern

```kotlin
class RescriptGenerateActionUtilTest : BasePlatformTestCase() {
    fun testFindEnclosingLetDeclaration() {
        myFixture.configureByText("Test.res", "let x<caret> = 1")
        // Create AnActionEvent from fixture, call findEnclosingDeclaration
    }
}
```

Note: Creating `AnActionEvent` from fixture requires `TestActionEvent` or the fixture's action context. If too complex, fall back to testing via PSI tree traversal directly rather than mocking AnActionEvent.

## No New Production Files

This unit only adds test files and test data. No production code changes.
