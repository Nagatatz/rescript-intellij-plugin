# Task List: .d.ts to ReScript Binding Generation

## Phase 1: Data Model + Type Mapper + Code Generator (pure Kotlin)
- [x] Create `DtsJsonModel.kt` — data classes + Gson type adapters
- [x] Create `DtsTypeMapper.kt` — TS→ReScript type mapping
- [x] Create `DtsToRescriptConverter.kt` — JSON model → ReScript code generation
- [x] Create `DtsJsonModelTest.kt` — JSON deserialization tests
- [x] Create `DtsTypeMapperTest.kt` — type mapping tests
- [x] Create `DtsToRescriptConverterTest.kt` — code generation tests

## Phase 2: Node.js Integration
- [x] Create `dts-to-json.js` — bundled TypeScript parser script
- [x] Create `DtsNodeDetector.kt` — Node.js + typescript detection
- [x] Create `DtsParserProcess.kt` — process execution
- [x] Create `DtsNodeDetectorTest.kt` — detection tests

## Phase 3: Action + Registration
- [x] Create `DtsGenerateBindingAction.kt` — AnAction
- [x] Register action in `plugin.xml`

## Phase 4: Verification
- [x] Build verification (`./gradlew buildPlugin`)
- [x] Test verification (`./gradlew test`)

## Phase 5: Documentation
- [x] Update `CLAUDE.md` — add `binding/` to project structure
- [x] Update `docs/product-requirements.md` — add to implemented features
- [x] Update `docs/functional-design.md` — add binding generation design
- [x] Update `README.md` — add feature to feature list
- [x] Update `sphinx-docs/user/features/advanced.md` — add feature documentation
- [x] Update `sphinx-docs/user/features/index.md` — update card description
- [x] Update `sphinx-docs/user/changelog.md` — add to changelog
- [x] Update Japanese translations (.po files)

## Phase 6: Commit + Branch
- [x] Create feature branch from `main`
- [x] Commit all changes
- [x] Confirm merge to `main`
