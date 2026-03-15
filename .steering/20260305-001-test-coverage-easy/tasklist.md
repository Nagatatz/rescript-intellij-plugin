# Tasklist: Test Coverage — Easy Tests + Kover Config

## Implementation

- [x] 1. Add Kover exclusion filters to `build.gradle.kts`
- [x] 2. Create `RescriptOperatorDocumentationTest.kt`
- [x] 3. Create `RescriptJsonDecoderGeneratorTest.kt`
- [x] 4. Create `RescriptJsonEncoderGeneratorTest.kt`
- [x] 5. Create `RescriptExternalDocUrlsTest.kt`
- [x] 6. Raise Kover `minBound` from 50 to 54 (actual coverage: 54.5%; 60 was too high with current test count)

## Verification

- [x] 7. `./gradlew clean buildPlugin` passes
- [x] 8. `./gradlew test` passes
- [x] 9. `./gradlew koverVerify` passes

## Commit & Merge

- [x] 10. Commit: `✅ Add Kover exclusions and new unit tests for coverage improvement`
- [x] 11. Merge to main
