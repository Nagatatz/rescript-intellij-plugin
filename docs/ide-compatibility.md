# IDE Compatibility Verification

The compile target and minimum build are read from `gradle.properties`. Updating a release feed does not authorize changing either value. The current target is 2026.2.3; the minimum is 2026.1.4 (`261.26222`). An unset `untilBuild` expresses intended forward compatibility, not proof of compatibility with every future release.

## Verification matrix

Verify the same plugin artifact against the exact minimum, the adopted stable target, and the latest EAP. `recommended()` may choose a newer patch in the minimum release line; the default Gradle configuration therefore also includes 2026.1.4. Use `-PverificationIde=<version>` to replace that matrix with one IDE per invocation, and `-PverificationIdeType=<product code>` for another product. Always run local heavy tasks sequentially with `--no-parallel --max-workers=1`. Pass `-PverificationMaxHeap=2g` to cap the separate verifier JVM heap; Gradle/compiler processes and native allocations remain additional. The EAP installer uses the product-feed build number (for example, `263.6259.32`), while Maven snapshot metadata may append `-EAP-CANDIDATE`.

| Product / configuration | Binary verification | IDE smoke |
| --- | --- | --- |
| IntelliJ IDEA (`IU`), exact minimum | Required before raising the floor or changing shared APIs | Highlighting, completion, diagnostics, navigation, code actions, LSP restart |
| IntelliJ IDEA (`IU`), adopted stable | Required for dependency/API updates | Same smoke, optional plugin availability, run configuration |
| IntelliJ IDEA (`IU`), latest EAP | Required for forward-compatibility review | Same smoke; record EAP build and observed limitations |
| WebStorm (`WS`), PyCharm (`PY`), PhpStorm (`PS`), stable | Representative checks for the README's other JetBrains IDEs claim | Native features and LSP availability; absent LSP support must leave native features usable |
| Linux x64, macOS arm64, Windows x64 | Scheduled OS matrix, with each exact IDE identified in the report | Include process startup, path handling, cancellation, and language-server restart |

The table is a verification requirement, not a list of tested configurations. A compatible binary result does not establish interactive behavior or equivalence across OS installer layouts. Other products and architectures need separate evidence before broader compatibility claims are made. Coordinate smoke automation with #107 and existing manual validation #92.

## Runtime validation gap

[#134](https://github.com/Nagatatz/rescript-intellij-plugin/issues/134) records a declaration PSI stub-contract failure found during #100's registered Intention regression with a physical `.res` file in project content: indexing/highlighting reports `Non-StubBasedPsiElement requests stub creation` on stable 2026.2.3. The successful binary Verifier results below do not exercise that runtime path or establish that it works.

The additional file-stub identifier collision reported by #100's CLI regression has been identified and repaired in PR #135 alongside the declaration-interface fix. Neither repair is included in this tested artifact. The #100 owner reports that seven programs passed registered Intention before/after/undo compilation and Node output comparison with zero skips against PR #135 head `7ad797504caf656f4a707fb6e48a4f57bce0b9ed`. This is evidence for that repaired dependency, not for the #115 artifact. Retain the runtime prerequisite until the repaired dependency is incorporated and the exact #115 artifact is checked.

Before completing #115's stable runtime smoke, record #134's validated fix commit and either include it in the tested #115 artifact or name its [fix PR #135](https://github.com/Nagatatz/rescript-intellij-plugin/pull/135) as a prerequisite. On that exact artifact, exercise physical project-file declaration indexing/highlighting and registered Intention discovery/application, and retain the test report and `idea.log`. PR #135's regression covers the five declaration indexes and Intention discovery; application/undo/CLI validation is separate #100 work. An in-memory PSI test or a binary-compatible artifact alone is insufficient. The fix's integration into the tested #115 artifact, physical smoke, and remaining product/OS checks are pending; do not attribute a fix to an artifact that does not contain it.

Interactive UI/LSP checks are tracked by [#92](https://github.com/Nagatatz/rescript-intellij-plugin/issues/92) and [#107](https://github.com/Nagatatz/rescript-intellij-plugin/issues/107), using [preparation PR #96](https://github.com/Nagatatz/rescript-intellij-plugin/pull/96). PR #96 supplies samples and procedures, not completed smoke results. Record completion, diagnostics, navigation, restart and the nine Code Actions with display/application outcomes separately. A headless physical indexing/Intention regression can establish its asserted runtime paths; it does not substitute for interactive UI/LSP validation. Remote CI may host a dedicated sequential smoke job with pinned tools and failure artifacts; it must run the tested fix/artifact rather than reuse another head's results.

## Remote cold-IDE checks

Use the Monthly Plugin Verify workflow's `ide_version` input for one cold IDE per run, optionally selecting `ide_type`. Dispatch on the dedicated verification branch so its script/configuration changes are included. The job retains read-only repository permissions, checks at least 12 GiB free before SDK resolution, constrains the verifier heap/Gradle worker count, uploads the verifier report, and skips template audit for an explicit single-IDE request. Scheduled runs and empty-input dispatches retain the default matrix and template audit.

Prefer cached stable verification locally when disk is limited; hand over the local heavy-task slot before Gradle starts. Cold minimum/EAP SDKs should be resolved on the CI runner instead of consuming shared local disk. CI results need the exact branch commit, product, platform build and report artifact recorded. This workflow does not publish a plugin or a release.

## API review

Retain and review `deprecated-usages.txt`, `experimental-api-usages.txt`, `internal-api-usages.txt`, and compatibility problems from Plugin Verifier. Record the plugin commit and artifact checksum, IDE build/product/OS, verifier version, result, warning counts, and smoke outcome in #115. Preserve reports for failed runs as well as successful ones. Compare exact API names and call sites, not only totals.

Known compatibility exceptions live in `plugin-verifier-ignored-problems.txt`. Each deliberate deprecated use needs a source explanation and a reviewed expiry there. The `FileIncludeProvider.acceptFile(VirtualFile)` override is retained for the minimum IDE's abstract method. The legacy LSP provider/descriptor/manager types are deferred to a dedicated migration, with expiry 2027-02-09; verify replacements on the exact minimum before changing the integration. Experimental hints, CodeVision, and LSP APIs require renewed review at every EAP boundary. Do not suppress an unresolved class merely to make verification green.

## Review ledger

| API / area | Decision and evidence to collect | Review deadline |
| --- | --- | --- |
| Legacy LSP provider, descriptor, manager | Migrate independently to the client/integration APIs documented since 2026.1.4; confirm exact-minimum loading and LSP smoke before removing the existing exception | 2027-02-09 (existing exception expiry) |
| `FileIncludeProvider.acceptFile(VirtualFile)` | Retain until the exact-minimum abstract override requirement is lifted | 2027-08-09 (existing exception expiry) |
| Search Everywhere contributor/factory/weighted contributor and `FoundItemDescriptor` | Temporarily retain the two existing contributors: EAP verification adds nine deprecated usages but reports no compatibility or internal-API problems. The replacement `SeItemsProvider`, factory, item and parameter contracts are identical in the exact minimum/stable/EAP sources and remain Experimental. JetBrains documents adapters for existing legacy contributors in local IDEs; this supports temporary retention alongside binary verification, while interactive behavior remains unverified. Migration changes the separate tab, ranking, presentation/navigation and read-action/suspend boundaries; those behaviors need validation before replacement. Owner: project maintainer / #115 Issue owner. No additional suppression is added | Before claiming 263 stable support; recheck by 2026-11-01 |
| Hints, CodeVision and LSP experimental APIs | Recheck exact call sites at each EAP boundary. Investigate any OS-specific unresolved `VcsCodeVisionLanguageContext` before treating product layouts as equivalent | Each EAP review; recheck by 2026-11-01 |

The 2026-10-10 review verified IU-261.26222.65 on Linux in [minimum CI](https://github.com/Nagatatz/rescript-intellij-plugin/actions/runs/38030341151), IU-262.10968.63 locally on macOS arm64, and IU-263.6259.32 on Linux in [EAP CI](https://github.com/Nagatatz/rescript-intellij-plugin/actions/runs/38031085999). Each run selected one IDE with Verifier 1.405 and reported Compatible. Deprecated counts were 35/36/45; the 127 experimental records were identical across all three reports. No internal-API or compatibility-problem report was emitted. These results do not complete the other-product/OS or interactive smoke requirements.

Before closing the Search Everywhere migration decision, smoke both the ReScript symbol search and the ReScript Types tab on the exact minimum and EAP: verify result ranking, type-signature display, navigation offsets, cancellation and read-action behavior. The replacement API already exists on the minimum; its absence is not a reason for retention. The dated retention decision is not a claim that migration is complete, and must be reopened if removal or a compatibility failure is observed.

## Official sources

- [Stable IntelliJ IDEA metadata](https://www.jetbrains.com/intellij-repository/releases/com/jetbrains/intellij/idea/ideaIU/maven-metadata.xml)
- [EAP IntelliJ IDEA metadata](https://www.jetbrains.com/intellij-repository/snapshots/com/jetbrains/intellij/idea/ideaIU/maven-metadata.xml)
- [IntelliJ Platform Gradle verification configuration](https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-extension.html#plugin-verification)
- [LSP integration API](https://plugins.jetbrains.com/docs/intellij/language-server-protocol.html)
- [Exact-minimum Search Everywhere provider contract](https://github.com/JetBrains/intellij-community/blob/idea/261.26222.65/platform/searchEverywhere/shared/src/SeItemsProvider.kt)
- [263 EAP Search Everywhere provider contract](https://github.com/JetBrains/intellij-community/blob/idea/263.6259.32/platform/searchEverywhere/shared/src/SeItemsProvider.kt)
- [263 EAP legacy contributor deprecation](https://github.com/JetBrains/intellij-community/blob/idea/263.6259.32/platform/lang-api/src/com/intellij/ide/actions/searcheverywhere/SearchEverywhereContributor.java)
- [JetBrains legacy Search Everywhere adapters and migration guidance](https://blog.jetbrains.com/platform/2025/12/major-architectural-update-introducing-the-new-search-everywhere-api-built-for-remote-development/)
