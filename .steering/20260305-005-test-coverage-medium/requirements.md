# Requirements: Test Coverage — Medium Tests

## Background

87 classes lack tests, of which 34 are non-exempt. The project has 213 tests but limited IDE fixture-based tests (12). This unit adds medium-difficulty tests requiring IDE fixtures or integration-level testing.

## Goals

1. Add tests for PSI elements (RescriptStringLiteral)
2. Add tests for DtsParserProcess.extractScript()
3. Add tests for RescriptGenerateActionUtil
4. Add test fixture data files for reuse

## Scope

### In Scope

- `RescriptStringLiteral` — injection host behavior (isValidHost, updateText, literalTextEscaper)
- `DtsParserProcess` — extractScript() resource loading, caching, error handling
- `RescriptGenerateActionUtil` — findEnclosingDeclaration, isInsideDeclaration with fixture
- Test data files for PSI tests

### Out of Scope

- Hierarchy tests (RescriptModuleHierarchyTreeStructure, call hierarchy) — requires complex PSI tree setup, defer
- RescriptUnwrappers — editor document mutation tests are brittle, existing descriptor tests are sufficient
- Stub serialization tests — IntelliJ's stub framework is well-tested internally

## Acceptance Criteria

- [ ] RescriptStringLiteralTest with 3+ test cases
- [ ] DtsParserProcessTest with 3+ test cases for extractScript()
- [ ] RescriptGenerateActionUtilTest with fixture-based tests
- [ ] Test data fixture files created
- [ ] `./gradlew test` passes
- [ ] `./gradlew clean buildPlugin` passes
