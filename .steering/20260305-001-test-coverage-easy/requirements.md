# Requirements: Test Coverage — Easy Tests + Kover Config

## Goal

Improve test coverage metrics by:
1. Adding Kover exclusion filters for 53 exempt classes (UI, LSP coupling, lifecycle, etc.)
2. Creating 4 new plain JUnit test files for untested utility/generator classes
3. Raising Kover `minBound` from 50 to 60

## Acceptance Criteria

- [ ] Kover excludes all exempt classes via wildcard patterns
- [ ] `RescriptOperatorDocumentationTest.kt` covers all operators in OPERATOR_INFO map
- [ ] `RescriptJsonDecoderGeneratorTest.kt` covers generateDecoder for Record/Variant/Unknown
- [ ] `RescriptJsonEncoderGeneratorTest.kt` covers generateEncoder for Record/Variant/Unknown
- [ ] `RescriptExternalDocUrlsTest.kt` covers MODULE_URL_MAP completeness and URL construction
- [ ] `./gradlew clean buildPlugin` passes
- [ ] `./gradlew test` passes (all tests)
- [ ] `./gradlew koverVerify` passes with minBound(60)
