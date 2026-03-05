# Requirements: JUnit 4 → JUnit 5 Migration

## Goal

Migrate the test suite from JUnit 4.13.2 to JUnit 5 (Jupiter).

## Scope

- 202 test files using JUnit 4 `@Test`/`Assert.*` → JUnit 5 Jupiter
- 14 files using `BasePlatformTestCase` (JUnit 3 style) → remain on JUnit 4 via vintage-engine
- `build.gradle.kts` dependency update

## Acceptance Criteria

- [ ] All 217 tests pass with `./gradlew test`
- [ ] `./gradlew clean buildPlugin` succeeds
- [ ] No JUnit 4 imports remain in non-BasePlatformTestCase test files
- [ ] BasePlatformTestCase tests continue to work via vintage-engine
