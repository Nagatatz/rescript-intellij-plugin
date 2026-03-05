# Design: JUnit 4 → JUnit 5 Migration

## Approach

Mechanical import replacement + dependency update. No logic changes.

## Dependency Changes (`build.gradle.kts`)

- Remove: `junit:junit:4.13.2`
- Add: `org.junit.jupiter:junit-jupiter:5.11.4` (JUnit 5 test framework)
- Add: `org.junit.vintage:junit-vintage-engine:5.11.4` (backward compat for BasePlatformTestCase)

## Import Mapping

| JUnit 4 | JUnit 5 |
|---------|---------|
| `org.junit.Test` | `org.junit.jupiter.api.Test` |
| `org.junit.Assert.*` | `org.junit.jupiter.api.Assertions.*` |
| `org.junit.Before` | `org.junit.jupiter.api.BeforeEach` |
| `org.junit.After` | `org.junit.jupiter.api.AfterEach` |
| `@Before` | `@BeforeEach` |
| `@After` | `@AfterEach` |

## Excluded Files (BasePlatformTestCase)

14 files inheriting BasePlatformTestCase/ParsingTestCase stay on JUnit 4.
