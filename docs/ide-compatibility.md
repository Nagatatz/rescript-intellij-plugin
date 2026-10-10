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
| Search Everywhere contributor/factory/weighted contributor and `FoundItemDescriptor` | 263 EAP adds deprecated findings in `RescriptSearchEverywhereContributor` and `RescriptTypeSignatureSearchContributor`; inspect replacement APIs in both the EAP and the minimum before migration. Stable compilation alone cannot prove EAP API lifetime | Before claiming 263 stable support; recheck by 2026-11-01 |
| Hints, CodeVision and LSP experimental APIs | Recheck exact call sites at each EAP boundary. Investigate any OS-specific unresolved `VcsCodeVisionLanguageContext` before treating product layouts as equivalent | Each EAP review; recheck by 2026-11-01 |

## Official sources

- [Stable IntelliJ IDEA metadata](https://www.jetbrains.com/intellij-repository/releases/com/jetbrains/intellij/idea/ideaIU/maven-metadata.xml)
- [EAP IntelliJ IDEA metadata](https://www.jetbrains.com/intellij-repository/snapshots/com/jetbrains/intellij/idea/ideaIU/maven-metadata.xml)
- [IntelliJ Platform Gradle verification configuration](https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-extension.html#plugin-verification)
- [LSP integration API](https://plugins.jetbrains.com/docs/intellij/language-server-protocol.html)
