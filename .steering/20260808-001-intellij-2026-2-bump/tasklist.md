# tasklist: IntelliJ Platform 2026.2 へのバンプ

## 前提と依存関係

- セクション 1 → 2 → 3 → 4 の順に依存する。**セクション 1 の完了で得られる実測結果（コンパイルエラー、
  `deprecated-usages.txt`、jar のクラス一覧）がセクション 2 以降の設計入力**となる
- 各セクションは単独でビルド・テストを通過し、`main` にマージできる粒度とする
- セクション 3 は代替 API が 2026.2 に実在した場合のみ実施。存在しなければセクション 3 を丸ごとスキップし、
  その旨を本ファイルに記録してセクション 4 に進む
- 作業は worktree 内で行う（ブランチ `worktree-intellij-2026-2-bump`）

## セクション 0: 準備

- [x] `git fetch origin` 実行、ローカル `main` が `origin/main` と同期していることを確認（0 ahead / 0 behind）
- [x] `EnterWorktree` で worktree を作成（`.claude/worktrees/intellij-2026-2-bump`、ブランチ `worktree-intellij-2026-2-bump`）
- [x] worktree 内で `pwd` と `git rev-parse --show-toplevel` を実行し、編集対象が worktree 内であることを確認
- [x] `.steering/20260808-001-intellij-2026-2-bump/` をコミット（`73cc5f40 📝 Add steering docs for IntelliJ Platform 2026.2 bump`）
- [x] worktree は `origin/main` 起点で作られるため、ローカル `main` から `git merge --ff-only` で上記コミットを取り込み

### セクション 0 の記録

- worktree の baseRef 設定は未指定＝デフォルト `fresh`（`origin/main` 起点）。ステアリングコミットは
  ローカル `main` にのみ存在したため、worktree 内で `git merge --ff-only main` して取り込んだ（origin への push はしていない）
- **並列セッションを検知**: 取り込み時に `3089c586 📝 Add steering docs for Windows POSIX test failures`
  （`.steering/20260808-002-windows-posix-test-failures/`、3 ファイル 281 行）が同時に入った。
  ステアリングドキュメントのみで本作業と競合しないが、**セクション 5 のマージ前に再度 `git fetch` と
  同期確認を行うこと**

## セクション 1: platformVersion のバンプ（単独で緑にする）

このセクションでは `@Suppress` も ignored-problems も **一切触らない**。
2026.2.0.1 でビルドが通るかどうかだけを切り分ける。

- [x] `gradle.properties` の `platformVersion` を `2026.1.2` → `2026.2.0.1` に変更
- [x] `pluginSinceBuild` は `253.0` のまま据え置く（D-1: 移行中は Verifier に旧 IDE も検証させるため）
- [x] `./gradlew clean buildPlugin` を実行
- [x] コンパイルエラーが出た場合、内容を本ファイルの「セクション 1 実測ログ」に転記する
- [x] **追加対応 A**: `bundledModule("intellij.platform.smRunner")` / `("intellij.platform.testRunner")` を追加
- [x] **追加対応 B**: `jvmToolchain(21)` → `jvmToolchain(25)`
- [x] `LspServerManager.getInstance()` が解決可能か（互換シムが残っているか）を確認し記録する ← F-2 の核心
- [x] `./gradlew ktlintCheck` が成功する
- [x] `./gradlew test` を実行（8 件失敗＝すべて既知の Windows POSIX 前提の失敗。下記参照）
  - [x] `RescriptSwitchFileActionTest.testActionPerformedOpensResCounterpart` は成功（CI 側のフレーキー）
- [x] `./gradlew verifyPlugin` を実行（**成功** / 17 分 53 秒 / 依存 2.52 GB ダウンロード）
- [x] `deprecated-usages.txt` の内容を「セクション 1 実測ログ」に転記する
- [x] Verifier が検証対象にした IDE の一覧を記録する
- [ ] コミット（`🔧 Bump IntelliJ Platform to 2026.2.0.1`）

### セクション 1 実測ログ（実装時に記入）

> D-4 の未確定 API の対応先をここに確定させる。推測で埋めないこと。

| 項目 | 実測結果 |
|---|---|
| ビルド成否 | **成功**（ただし下記 2 件の追加対応が必要だった） |
| コンパイルエラー | ① `com.intellij.execution.testframework.*` が解決不能 ② 全 Java ソースで「クラスファイルのバージョン 69.0 は不正。65.0 である必要がある」 |
| `LspServerManager.getInstance()` の可否 | **可**。`LspServerManager extends LspClientManager` で `getInstance(Project)` は静的メソッドとして現存。F-2 の null 化は `project.service<LspServerManager>()` 経由に限られ、本プラグインの呼び出し方（`getInstance`）は影響を受けない |
| `LspServerNotificationsHandler` の対応先 | **変更なし**（`LspClientNotificationsHandler` は存在しない） |
| `Lsp4jClient` の対応先 | **変更なし** |
| `LspServerState` の対応先 | **変更なし**（`LspClientState` は存在しない。`LspClient.getState()` の戻り値も `LspServerState`） |
| `LspCustomization` / `LspSemanticTokensSupport` の対応先 | **変更なし**（`customization/` 配下は新旧の別名なし） |
| Verifier 検証対象 IDE | `IU-253.33813.55` (2025.3.6) / `IU-261.27258.27` (2026.1.x) / `IU-262.9437.65` (2026.2.x)。**下限 261.26222 より新しい 261 系が含まれる**ため、セクション 4-1 の懸念（2026.1 系が検証対象から漏れる）は解消 |
| `deprecated-usages.txt` の新規項目 | 253: レポートなし（0 件） / 261: 35 件（すべて LSP） / 262: 37 件（LSP 35 + FloatingToolbar 1 + FileInclude 1）。セクション 2 で 35 件、セクション 3 で 2 件が解消する見込み |

#### 追加発見 C: Java 25 バイトコードにより sinceBuild 引き上げは必須

生成された `build/libs/rescript-intellij-plugin-0.1.16.3.jar` 内のクラスファイルバージョンを実測すると
**69.0 (Java 25)** だった。2025.3 系は JBR 21 で動くため、この成果物は
**2025.3 では `UnsupportedClassVersionError` により読み込めない**。

にもかかわらず Plugin Verifier は `IU-253.33813.55` を green と判定した。
**Plugin Verifier はクラスファイルバージョンの互換性を検査しない**（API シグネチャのみ検証する）。

したがって:

- `pluginSinceBuild` の 261.26222 への引き上げは「互換性を捨てる判断」ではなく、
  **Java 25 バイトコードを配る以上、技術的に必須**である
- セクション 4-1 を完了するまで、この成果物を Marketplace に publish してはならない
- D-1 で「移行中は 253.0 のままにして Verifier に旧 IDE も検証させる」とした方針は、
  API 誤用の検出には有効だが、**253 の green を互換性の裏付けとして読んではならない**

#### LSP 移行対象ファイルの実パス（design.md の想定値を実測で訂正）

`deprecated-usages.txt` から確定した実パス。design.md のパス想定と 4 件相違があった:

| design.md の想定 | 実際 |
|---|---|
| `inspections/RescriptSignatureSyncInspection.kt` | `inspection/RescriptSignatureSyncInspection.kt` |
| `actions/RescriptCreateInterfaceAction.kt` | `navigation/RescriptCreateInterfaceAction.kt` |
| `actions/RescriptOpenCompiledJsAction.kt` | `navigation/RescriptOpenCompiledJsAction.kt` |
| `refactoring/RescriptRenameHandler.kt` | `refactor/RescriptRenameHandler.kt` |

#### 追加対応 A: テストランナーのモジュール分離（design 未想定）

2026.2 で `com.intellij.execution.testframework.*` が implementation-detail プラグイン
`intellij.testRunner.plugin` 配下の content module に切り出された。`build.gradle.kts` に以下を追加した:

```kotlin
bundledModule("intellij.platform.smRunner")   // com.intellij.execution.testframework.sm.*
bundledModule("intellij.platform.testRunner") // com.intellij.execution.testframework.*（基底）
```

`intellij.platform.smRunner` は plugin.xml 上 `intellij.platform.testRunner` に依存しているが、
その依存はコンパイルクラスパスに推移しないため両方の明示が必要だった。両モジュールとも
`visibility="public"` なので依存してよい。

#### 追加対応 B: JDK 25 への toolchain 引き上げ（design 未想定・最重要）

**2026.2 のバイトコードは Java 25（クラスファイルバージョン 69.0）である。** JDK 21 の javac は
65.0 までしか読めないため、Java ソース（`RescriptCodeVisionProvider.java`）のコンパイルが全滅した。
Kotlin コンパイラは 69.0 を読めたため `compileKotlin` は通っており、`compileJava` で初めて露見した。

バンドル JBR の変遷（各リリースの `jbr/release` を実測）:

| リリース | バンドル JBR | プラットフォームのバイトコード |
|---|---|---|
| 2025.3.6 | 21.0.11 | 65.0 (Java 21) |
| 2026.1.2 | 25.0.2 | **65.0 (Java 21)** |
| 2026.1.3 | 25.0.3 | （未計測） |
| 2026.2.0.1 | 25.0.3 | **69.0 (Java 25)** |

2026.1 でランタイムは既に JBR 25 へ移行済みで、2026.2 でバイトコードも 25 に上がった。
`pluginSinceBuild` の下限である 2026.1.4 も JBR 25.0.3 で動くため、Java 25 出力で互換性は保たれる。

対応: `build.gradle.kts` の `jvmToolchain(21)` → `jvmToolchain(25)`。
`settings.gradle.kts` に foojay-resolver があるため JDK 25.0.4 が自動取得された。

**波及**: CI ワークフローの `java-version: 21` と、ドキュメントの「JDK 21+」表記の更新が必要。
→ セクション 4 に追加タスクとして反映すること。

#### テスト結果: 8 件失敗（すべて既知・バンプ起因ではない）

`RescriptCliDetectorTest` ×2 / `RescriptReanalyzeServerServiceTest` ×1 /
`RescriptFormatCheckAnnotatorTest` ×2 / `RescriptSecurityUtilsTest` ×1 /
`RescriptSettingsValidatorTest` ×2 = 計 8 件。

並列セッションのステアリング `.steering/20260808-002-windows-posix-test-failures/requirements.md`
（コミット `3089c586`、**本バンプ以前**に作成）が、2026.1.2 時点の Windows で同じ 8 件が失敗すると
記録しており、テスト名が 1 件も過不足なく一致する。したがって **本バンプによる新規失敗は 0 件**。
原因はいずれもテストコードの POSIX 前提（パス区切り、`/usr/bin/false`、実行ビット）で、CI (Linux) では green。

#### セクション 3 の実施可否: **実施する**

2026.2 に代替 API が両方とも存在することを実測で確認した:

- `FloatingToolbarProvider.isApplicableAsync(DataContext, Continuation<Boolean>)` — Kotlin の suspend 関数
- `FileIncludeProvider.acceptFile(IndexedFile)` — `acceptFile(VirtualFile)` と併存

#### LSP API 対応表（実測で確定・design F-1 を更新）

新旧クラスは**併存**しており、旧名は deprecated だが削除されていない。単純な rename ではなく
**メソッド名も変わる**点に注意:

| 旧 | 新 |
|---|---|
| `LspServerSupportProvider` | `LspIntegrationProvider` |
| `LspServerDescriptor` | `LspClientDescriptor` |
| `ProjectWideLspServerDescriptor` | `ProjectWideLspClientDescriptor` |
| `LspServer` | `LspClient` |
| `LspServerManager` | `LspClientManager` |
| `LspServerManagerListener` | `LspClientManagerListener` |
| `LspServerSupportProvider.LspServerStarter` | `LspIntegrationProvider.LspClientStarter` |
| `getServersForProvider(...)` | `getClients(...)` |
| `startServersIfNeeded(...)` | `startClientsIfNeeded(...)` |
| `ensureServerStarted(...)` | `ensureClientStarted(...)` |
| `stopServers(...)` | `stopClients(...)` |
| `stopAndRestartIfNeeded(...)` | `stopAndRestartClientsIfNeeded(...)` |
| `addLspServerManagerListener(...)` | `addListener(...)` |
| `createLspWidgetItems(...)` | `createWidgetItems(...)` |
| `createLspServerWidgetItem(...)` | `createWidgetItem(...)`（戻り値 `LspClientWidgetItem`） |
| `LspServerNotificationsHandler` | **変更なし** |
| `Lsp4jClient` | **変更なし** |
| `LspServerState` | **変更なし** |
| `customization/*` | **変更なし** |

確認手順（design.md D-4）:

- [ ] 解決済み jar を特定: `find ~/.gradle/caches/modules-2/files-2.1/com.jetbrains.intellij.idea -name "*.jar" | xargs -I{} sh -c 'unzip -l "{}" 2>/dev/null | grep -q "platform/lsp/api" && echo "{}"' | head -1`
- [ ] クラス一覧を確認: `unzip -l <jar> | grep "platform/lsp/api"`
- [ ] シグネチャを確認: `javap -classpath <jar> com.intellij.platform.lsp.api.LspClientManager`
- [ ] `FloatingToolbarProvider` に `isApplicableAsync` が存在するか確認 → セクション 3 の実施可否を判定
- [ ] `FileIncludeProvider` に `acceptFile(IndexedFile)` が存在するか確認 → 同上
- [ ] PolySymbols の `PsiSourced*` を本プラグインが参照していないことを `grep -rn "PsiSourced" src/` で再確認（F-3）

## セクション 2: LSP API の移行 — **本作業のスコープ外（後続作業へ切り出し / 2026-08-09 決定）**

> **本セクションは実施しない。** 以下のタスクは後続作業の設計入力として保持する。
>
> 切り出しの根拠（requirements.md「R-3 の切り出し」参照）:
>
> - 必須と判断した根拠だった design.md F-2（`LspServerManager` が runtime で null になる）は、
>   セクション 1 の実測で **本プラグインには当てはまらない**と判明した。`LspServerManager extends
>   LspClientManager` であり `getInstance(Project)` は静的メソッドとして現存し、旧 API は 2026.2 でも動作する
> - 移行は単純な rename ではなくメソッド名の変更を伴い 13 ファイルに及ぶ
> - 本作業は「2026.2 でビルドでき、リリースできる状態」を最短で作ることを優先する
>
> 後続作業に引き継ぐ資産: セクション 1 実測ログの「LSP API 対応表」と「LSP 移行対象ファイルの実パス」。
> `plugin-verifier-ignored-problems.txt` の LSP エントリに追跡情報を残すこと（4-2 で対応）。

以下は後続作業用の下書きであり、本作業では `[ ]` のまま残す。

### 2-1: コア（Provider / Descriptor / Client）

- [ ] `lsp/RescriptLspServerSupportProvider.kt` の親を `LspIntegrationProvider` へ変更
- [ ] `lsp/RescriptLspServerDescriptor.kt` の親を `ProjectWideLspClientDescriptor` へ変更
- [ ] `lsp/RescriptLsp4jClient.kt` の通知ハンドラ型を実測結果に合わせる
- [ ] `plugin.xml:641` の EP を `platform.lsp.serverSupportProvider` → `platform.lsp.integrationProvider` へ変更
      （D-5: 旧 EP との併記はしない）
- [ ] 各ファイルの `@Suppress("DEPRECATION")` / `@Suppress("OVERRIDE_DEPRECATION")` を除去
- [ ] KDoc / インラインコメント内の旧クラス名への言及を更新（`code-comments.md`）
- [ ] `./gradlew ktlintCheck clean buildPlugin` が成功する
- [ ] コミット（`♻️ Migrate LSP provider and descriptor to the LspClient API`）

### 2-2: 呼び出し側（LspServerManager / LspServer / LspServerState）

対象ファイルは実装時に `grep -rln "LspServerManager\|LspServer\b\|LspServerState" src/main` で確定する。
design.md の一覧は想定値であり、パスが異なる場合はそちらを優先する。

- [ ] `lsp/RescriptLspUtils.kt`
- [ ] `lsp/RescriptLspInstaller.kt`
- [ ] `lsp/RescriptRestartLspAction.kt`
- [ ] `lsp/RescriptDumpLspStateAction.kt`
- [ ] `lsp/RescriptExpressionTypeProvider.kt`
- [ ] `codevision/RescriptCodeVisionProvider.java`（`LspServer` / `LspServerState` / `sendRequestSync`）
- [ ] `inspections/RescriptSignatureSyncInspection.kt`
- [ ] `actions/RescriptOpenCompiledJsAction.kt`
- [ ] `actions/RescriptCreateInterfaceAction.kt`
- [ ] `refactoring/RescriptRenameHandler.kt`
- [ ] `settings/RescriptConfigurable.kt`
- [ ] 上記すべてで `@Suppress` を除去し、旧クラス名を参照するコメントを更新
- [ ] `grep -rn "LspServerManager\|LspServerSupportProvider\|LspServerDescriptor\|LspServerState" src/main` が
      0 件になることを確認（`RescriptLspServer*` という自前クラス名は D-3 によりリネーム対象外）
- [ ] `./gradlew ktlintCheck clean buildPlugin` が成功する
- [ ] コミット（`♻️ Migrate LSP call sites to LspClientManager`）

### 2-3: テスト

- [ ] `lsp/RescriptLspUtilsTest.kt` に **F-2 のリグレッションテスト**を追加
      （`LspClientManager` の取得口が null を返さない／例外を投げないことを検証。
      テスト環境で LSP が起動しない場合は例外非送出の確認に縮退し、理由を本ファイルに明記する）
- [ ] `lsp/RescriptLspUtilsTest.kt` が緑
- [ ] `lsp/RescriptRestartLspActionTest.kt` が緑
- [ ] `lsp/RescriptLspInstallerTest.kt` が緑
- [ ] `lsp/RescriptExpressionTypeProviderTest.kt` が緑
- [ ] `lsp/RescriptSemanticTokensSupportTest.kt` が緑
- [ ] `lsp/RescriptCompilationStatusServiceTest.kt` が緑
- [ ] `./gradlew test > /tmp/test.log 2>&1; tail -40 /tmp/test.log` が全件成功
- [ ] `./gradlew verifyPlugin` が成功し、LSP 関連の deprecated 報告が消えていることを確認
- [ ] コミット（`✅ Add regression test for LspClientManager lookup`）

### 2-4: 実機スモークテスト（AC-7 / 省略不可）

F-2 の性質上、ビルドが通っても LSP が無言で停止しうる。`ui-smoke-test` スキルを使用する。

- [ ] `runIde` サンドボックスで ReScript プロジェクトを開き、LSP サーバーが起動することを確認
- [ ] 補完が動作する
- [ ] 診断（エラー・警告）が表示される
- [ ] 定義ジャンプが動作する
- [ ] ホバーで型情報が表示される
- [ ] `Restart LSP Server` アクションが動作する（`LspClientManager` 経由のため F-2 の直撃箇所）
- [ ] `Dump LSP State` アクションが動作する（同上）
- [ ] 結果を本ファイルに記録する

## セクション 3: FloatingToolbarProvider / FileIncludeProvider の移行 ← **実施する**

**セクション 1 実測ログで代替 API の存在を確認できた場合のみ実施。**

> 実施可否の判定結果: **実施する**。`isApplicableAsync(DataContext, Continuation<Boolean>)` と
> `acceptFile(IndexedFile)` の両方が 2026.2 に存在することを `javap` で実測確認済み。
> LSP 移行（セクション 2）を切り出した後も、本セクションは LSP と無関係なため本作業に残す
> （2026-08-09 のユーザー判断）。
>
> 注意: `isApplicableAsync` は Kotlin の suspend 関数であり、`isApplicable` の単純な置換にはならない。

- [x] `editor/RescriptFloatingToolbarProvider.kt` を `isApplicableAsync` へ移行
- [x] `editor/RescriptFloatingToolbarProviderTest.kt` にテストを追加（新規 2 件）
- [x] `navigation/RescriptFileIncludeProvider.kt` を `acceptFile(IndexedFile)` へ移行
- [x] `navigation/RescriptFileIncludeProviderTest.kt` にテストを追加（新規 4 件）
- [x] 両ファイルの `@Suppress("DEPRECATION")` を除去
- [x] `./gradlew ktlintCheck buildPlugin test` が成功する（既知の Windows 8 件を除く）
- [x] コミット（`♻️ Migrate FloatingToolbarProvider and FileIncludeProvider to the current APIs`）

### セクション 3 の記録

**`isApplicableAsync` のみを override してよいことの裏取り。**
デフォルトの `isApplicable` は `iconst_1`（常に `true`）を返すため、旧メソッド経由の呼び出しが
残っていればフィルタが失われ、全ファイル種別でツールバーが表示される回帰になる。
`intellij.platform.ide.impl.jar` を展開して呼び出し元を調べたところ、
プラットフォーム側の唯一の消費者 `EditorFloatingToolbar$1` は `isApplicableAsync` のみを
invoke しており（`isApplicable` の invoke は無い）、async 版だけの override で十分と確認した。

**移行は機械的に留めた。** `isApplicableAsync` は suspend 関数であり呼び出しが EDT 外になりうるが、
本体は `DataContext` からの取得と `PsiFile.language` の参照のみで PSI ツリーを辿らないため、
`readAction` の導入は行わず旧実装と同一の本体を維持した（スレッド方針の変更は本作業のスコープ外）。

**テストの追加（計 6 件）。** `deprecated-usages.txt` で消える 2 件は「消えたこと」しか保証しないため、
振る舞いのリグレッションテストを追加した:

- `RescriptFloatingToolbarProviderTest`: PSI なしコンテキストで `false` / ReScript ファイルで `true`
- `RescriptFileIncludeProviderTest`: `.res` / `.resi` を受理、他拡張子と拡張子なしを拒否

`isApplicableAsync` の正常系テストは当初失敗した。プラットフォームが `DataContext` を通る PSI を
検証して無効な要素を落とすため、Mockito のモックで `isValid()` が既定の `false` を返し
`getData()` が null になっていた。`isValid` を stub して解決（その旨をテスト内にコメントで明記）。

**テスト件数**: 4456 → 4462（+6）。失敗は既知の Windows 8 件のみで新規failure なし。

## セクション 4: sinceBuild 引き上げ・ignored-problems 棚卸し・ドキュメント同期

### 4-0: JDK 25 への追随（セクション 1 の追加対応 B の波及）

> ドキュメント側の JDK 表記は 4-3 の IDE バージョン更新と同じファイルを触るため、
> 二度編集を避けて **4-3 のドキュメントコミットに集約する**。本節は workflow のみを扱う。

- [x] `.github/workflows/` 全ファイルの `java-version: 21` を `25` に更新（10 箇所 / 7 ファイル）
- [x] `actions/setup-java` の `distribution` が JDK 25 を提供するか確認する
      （全箇所 `temurin`。Temurin 25 は GA。ローカルでも foojay 経由で Temurin 25.0.4 を取得済み）
- [x] コミット（`🔧 Move CI to JDK 25 for the 2026.2 platform`）

### 4-1: sinceBuild と Verifier

- [ ] `gradle.properties` の `pluginSinceBuild` を `253.0` → **`261.26222`** に変更
      （`261.4` などの短縮表記にしないこと。design.md D-2 参照）
- [ ] `./gradlew clean buildPlugin` が成功する
- [ ] `./gradlew verifyPlugin` が成功する
- [ ] Verifier の検証対象 IDE に **2026.1.4 が含まれる**ことを確認し記録する
      （含まれない場合、262 専用 API の誤用を検出できないため `ides { }` の明示指定を検討する）
- [ ] `deprecated-usages.txt` に新規の deprecated 利用が無いことを確認（AC-5）
- [ ] コミット（`🔧 Raise pluginSinceBuild to 261.26222 (2026.1.4)`）

### 4-2: plugin-verifier-ignored-problems.txt の棚卸し

> セクション 2 を切り出したため、当初「削除」としていた LSP エントリは **残す**方針に変更した。

- [ ] LSP API のエントリ（L47-67）は **削除せず残す**。ただし記述を 2026.2 の実測に合わせて訂正する:
  - [ ] 「replacement API が 2026.1.2 に存在しない」という記述は**もう正しくない**。
        2026.2（および 2026.1.4）に `LspIntegrationProvider` / `LspClientDescriptor` /
        `LspClientManager` が存在することを明記する
  - [ ] 移行が後続作業に切り出されている旨と、その理由（旧 API は 2026.2 でも動作する）を記載する
  - [ ] クラス名の誤り（`LspClientSupportProvider`）を `LspIntegrationProvider` に訂正する
  - [ ] `Reviewed: 2026-08-09` に更新し、`Expires` は後続作業の期限として近めに設定する
- [ ] `FloatingToolbarProvider.isApplicable` のエントリ（L34-38）を **削除**（セクション 3 で解消）
- [ ] `FileIncludeProvider.acceptFile` のエントリ（L40-45）を **削除**（セクション 3 で解消）
- [ ] `CodeVisionPlaceholderCollector` のエントリ（L14-22）— 2026.2 でも `@Internal` が残るか確認し
      `Reviewed` を更新（Expires は 2027-04-29 のまま据え置きでよい）
- [ ] ファイル冒頭の `Reviewed: 2026-05-14 | Target: IntelliJ 2025.3+` を
      `Reviewed: 2026-08-09 | Target: IntelliJ 2026.1.4+` に更新
- [ ] `./gradlew verifyPlugin` が成功する（削除したエントリが本当に不要だったことの確認）
- [ ] コミット（`🔧 Prune verifier ignored problems resolved by the 2026.2 bump`）

### 4-3: ドキュメント同期

`documentation.md` の同期対象表および requirements.md AC-8 に従う。表記は **2026.1.4+** に統一する。

- [ ] `README.md:189` — `IntelliJ IDEA 2025.3+` → `2026.1.4+`
- [ ] `CLAUDE.md:13` — 対象プラットフォーム
- [ ] `docs/architecture.md:12` — SDK バージョン表を `2026.2.0.1` へ
- [ ] `docs/architecture.md:62` — 最低 IDE バージョン
- [ ] `docs/versions.md:10` — 対象 IDE バージョン（下限）
- [ ] `sphinx-docs/dev/building.md:67` — `pluginSinceBuild` の例示
- [ ] `sphinx-docs/dev/contributing.md:15,17` — JDK 要件 / IntelliJ IDEA
- [ ] `sphinx-docs/dev/setup.md:14` — IntelliJ IDEA
- [ ] `sphinx-docs/user/faq.md:12` — 対応 IDE の説明
- [ ] `sphinx-docs/user/installation.md:11` — 前提要件
- [ ] `sphinx-docs/user/version-matrix.md:15` — 対応表に新しい行を追加
- [ ] `docs/functional-design.md:637` と `docs/performance-validation.md:90` は **更新しない**
      （当時の実測記録・沿革説明のため。requirements.md AC-8 の注記）
- [ ] `docs/repository-structure.md` / `docs/functional-design.md` のパッケージ表・EP マップに
      LSP EP 名の変更（`serverSupportProvider` → `integrationProvider`）を反映
- [ ] `.po` 同期: `cd sphinx-docs && make gettext && make update-po`
- [ ] 空の `msgstr` を日本語で埋める
- [ ] `cd sphinx-docs && make build-ja` が成功する
- [ ] `docs-lint` スキルで同期崩れがないことを確認
- [ ] コミット（`📝 Update supported IDE version to 2026.1.4+`）

## セクション 5: 完了処理

- [ ] requirements.md の AC-1〜AC-8 をすべて `[x]` にする
- [ ] `definition-of-done-check` スキル（または `.claude/rules/definition-of-done.md`）で Phase 1〜5 を確認
- [ ] `git status` と `git log --oneline origin/main..HEAD` を実行し、出力を確認したうえで状態を報告する
- [ ] 本ファイルの全タスクが `[x]` であることを確認（このタスク自身を含む）
- [ ] tasklist 更新をマージ前の最終コミットに含める
- [ ] `AskUserQuestion` でマージ可否を確認する
  - [ ] `pluginSinceBuild` 引き上げによる対応 IDE 縮小（253.x および 2026.1.0〜2026.1.3 の切り捨て）を明示する
  - [ ] LSP API 全面移行というセキュリティ・可用性上の影響範囲を明示する
- [ ] 承認後、`main` にマージしブランチを削除する
- [ ] セッションを終了する（worktree の自動クリーンアップを発動させる）

## 別作業に送る項目

本作業のスコープ外。完了後に着手を検討する。

- `template-versions-audit` の npm 脆弱性解消（astro / react-router / next / esbuild / drizzle-kit のメジャー更新）
- `RescriptSwitchFileActionTest.testActionPerformedOpensResCounterpart` のフレーキー対応
- `pluginVersion` バンプと Marketplace リリース（`0.1.17`）
- **LSP API 移行（本作業のセクション 2 を切り出したもの / 最優先の後続作業）** — `LspServer*` 系 →
  `LspIntegrationProvider` / `LspClientDescriptor` / `LspClientManager` 系。13 ファイル + EP 変更 +
  実機スモークテスト。設計入力は本ファイルのセクション 1 実測ログとセクション 2 の下書きを参照
- 自前クラス名のリネーム（`RescriptLspServerSupportProvider` → `RescriptLspIntegrationProvider` 等、D-3）
- Dependabot PR #63 / #64 / #65 のマージ
- マージ済み Dependabot リモートブランチの削除
