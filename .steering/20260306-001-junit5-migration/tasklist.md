# Tasklist: JUnit 4 → JUnit 5 Migration

## Tasks

- [x] Update `build.gradle.kts` dependencies
- [x] Replace JUnit 4 imports with JUnit 5 in 202 test files (excluding BasePlatformTestCase files)
- [x] Replace `@Before`/`@After` annotations with `@BeforeEach`/`@AfterEach`
- [x] Migrate `@Rule TemporaryFolder` to `@TempDir` (3 files)
- [x] Fix assertion argument order (message-first → message-last) in 3 files
- [x] Verify all tests pass: `./gradlew test`
- [x] Verify build: `./gradlew clean buildPlugin`
- [x] Commit changes
- [x] Merge to main
