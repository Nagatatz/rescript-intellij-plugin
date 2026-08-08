# tasklist: LSP API の LspClient 系への移行

## 前提と依存関係

- セクション 1 → 2 → 3 → 4 の順に依存する
- **セクション 2 は分割できない。** 旧 `LspServerManager` の引数型が旧 provider 型に固定されて
  いるため、provider の親を変えた時点で呼び出し側が型エラーになる（design.md 1.3 / D-1）
- セクション 1 / 3 / 4 はそれぞれ単独でビルド・テストを通過し、`main` にマージできる粒度とする
- 作業は worktree 内で行う（ブランチ `worktree-lsp-client-api-migration`）
- **AC-7（実機スモークテスト）は 2026-08-09 のユーザー判断で省略する。**
  マージ確認時にその旨を再掲すること（requirements.md「AC-7 省略の経緯と残存リスク」）

## セクション 0: 準備

- [ ] `git fetch origin` を実行し、ローカル `main` が `origin/main` と同期していることを確認する
      （0 ahead / 0 behind。ずれていれば `git pull --ff-only origin main`）
- [ ] `.steering/20260809-001-lsp-client-api-migration/` をコミット（`📝 Add steering docs for the LSP client API migration`）
- [ ] `EnterWorktree` で worktree を作成する
- [ ] worktree 内で `pwd` と `git rev-parse --show-toplevel` を実行し、編集対象が worktree 内であることを確認する
- [ ] worktree が `origin/main` 起点で作られた場合、ステアリングコミットを `git merge --ff-only main` で取り込む

## セクション 1: Verifier の検証対象に下限ビルドを追加

`recommended()` は 261 系として 2026.1.5 RC (`261.27258.27`) を選ぶため、
`pluginSinceBuild = 261.26222`（2026.1.4 GA = `261.26222.65`）そのものが未検証だった（design.md D-5）。

- [ ] `build.gradle.kts` の `pluginVerification.ides { }` に `ide(...)` で `261.26222.65` を追加する
- [ ] 追加理由をコメントで明記する（`recommended()` が下限を拾わないこと）
- [ ] `./gradlew verifyPlugin > /tmp/verify-baseline.log 2>&1; tail -40 /tmp/verify-baseline.log` を実行し、
      **移行前のベースライン**として下限ビルドを含む 3 IDE の判定と deprecated 件数を記録する
- [ ] 所要時間と追加ダウンロード量を「セクション 1 の記録」に転記する
- [ ] `./gradlew ktlintCheck` が成功する
- [ ] コミット（`🔧 Verify the plugin against the 2026.1.4 sinceBuild floor`）

### セクション 1 の記録（実装時に記入）

| 項目 | 実測結果 |
|---|---|
| 検証対象 IDE | |
| 各 IDE の判定 | |
| deprecated 件数（移行前） | |
| verifyPlugin 所要時間 | |
| 追加ダウンロード量 | |

## セクション 2: LSP API の移行（分割不可）

### 2-1: コア（provider / descriptor / utils）

- [ ] `lsp/RescriptLspServerSupportProvider.kt` — 親を `LspIntegrationProvider` へ。
      `fileOpened` の第 3 引数を `LspIntegrationProvider.LspClientStarter` へ、
      `ensureServerStarted` → `ensureClientStarted`
- [ ] `lsp/RescriptLspServerDescriptor.kt` — 親を `ProjectWideLspClientDescriptor` へ
      （override 対象メンバは名称・シグネチャとも不変）
- [ ] `lsp/RescriptLspUtils.kt` — `LspServerManager` → `LspClientManager`、
      `getServersForProvider` → `getClients`、戻り値 `LspServer?` → `LspClient?`

### 2-2: 呼び出し側

- [ ] `lsp/RescriptRestartLspAction.kt` — `stopAndRestartIfNeeded` → `stopAndRestartClientsIfNeeded`
- [ ] `lsp/RescriptDumpLspStateAction.kt` — `getServersForProvider` → `getClients`
- [ ] `lsp/RescriptLspInstaller.kt` — `stopAndRestartClientsIfNeeded`
- [ ] `lsp/RescriptExpressionTypeProvider.kt`
- [ ] `inspection/RescriptSignatureSyncInspection.kt`
- [ ] `navigation/RescriptOpenCompiledJsAction.kt`
- [ ] `navigation/RescriptCreateInterfaceAction.kt` — ヘルパの引数型 `LspServer` → `LspClient`
- [ ] `refactor/RescriptRenameHandler.kt` — import + ヘルパ引数型
- [ ] `settings/RescriptConfigurable.kt`
- [ ] `codevision/RescriptCodeVisionProvider.java` — `LspServer` → `LspClient`。
      `LspServerState` の import は据え置く（`LspClient.getState()` の戻り値型のため）

### 2-3: 抑制の除去とコメント整理

- [ ] LSP 由来の `@Suppress("DEPRECATION")` / `@Suppress("OVERRIDE_DEPRECATION")` /
      `@SuppressWarnings("deprecation")` をすべて除去する
- [ ] `@Suppress("UnstableApiUsage")` も除去する（design.md 1.5: 新 API はクラスレベルに
      `@ApiStatus.Experimental` / `Internal` が付いていない）
- [ ] 「the replacement ... does not exist on the 2026.1.2 compile target」のコメントを削除する
      （実測で誤りと確認済み）
- [ ] 「Explicit 10s timeout」のコメントは **残す**（新 `LspClient` でも
      `sendRequestSync$default` は合成されるため意味を持つ）。文言のクラス名のみ追随させる
- [ ] KDoc の `@see` と本文の旧クラス名への言及を更新する（`.claude/rules/code-comments.md`）

### 2-4: EP と Verifier 設定

- [ ] `plugin.xml` の EP を `platform.lsp.serverSupportProvider` → `platform.lsp.integrationProvider` へ
      （D-3: 旧 EP と併記しない）
- [ ] `plugin-verifier-ignored-problems.txt` から `com.intellij.platform.lsp.api.*` エントリを削除する
- [ ] ファイル冒頭の `Reviewed:` 日付を更新する

### 2-5: テスト（本セクションのコミットに同梱）

- [ ] `lsp/RescriptLspUtilsTest.kt` — `getServer(project)` が LSP 未起動環境で例外を投げず
      `null` を返すことを検証するテストを追加する（`LspClientManager.getInstance` の取得口の回帰検知）
- [ ] `lsp/RescriptLspServerSupportProviderTest.kt` — `fileOpened` が `.res` / `.resi` でのみ
      `ensureClientStarted` を呼び、他拡張子・拡張子なしでは呼ばないことを検証する
      （`LspClientStarter` をモック）
- [ ] 既存 LSP テスト（`RescriptRestartLspActionTest` / `RescriptLspInstallerTest` /
      `RescriptExpressionTypeProviderTest` / `RescriptSemanticTokensSupportTest` /
      `RescriptCompilationStatusServiceTest`）が緑であることを確認する

### 2-6: 検証とコミット

- [ ] `grep -rn "LspServerSupportProvider\|LspServerDescriptor\|ProjectWideLspServerDescriptor\|LspServerManager" src/`
      が **自前クラス名を除いて 0 件**（AC-1。この時点では自前クラス名は未リネーム）
- [ ] `grep -rn "DEPRECATION\|SuppressWarnings(\"deprecation\")" src/` に LSP 由来の残りがない（AC-2）
- [ ] `./gradlew ktlintCheck clean buildPlugin > /tmp/build.log 2>&1; tail -40 /tmp/build.log` が成功（AC-3）
- [ ] `./gradlew test > /tmp/test.log 2>&1; tail -40 /tmp/test.log` が全件成功（AC-4）
- [ ] `./gradlew verifyPlugin > /tmp/verify.log 2>&1; tail -60 /tmp/verify.log` — 3 IDE すべて `Compatible`、
      かつ `deprecated-usages.txt` の LSP 関連が **0 件**（AC-5 / AC-6）
- [ ] セクション 1 のベースラインと突き合わせ、deprecated 件数の減少を「セクション 2 の記録」に転記する
- [ ] コミット（`♻️ Migrate the LSP integration to the LspClient API`）

### セクション 2 の記録（実装時に記入）

| 項目 | 移行前 | 移行後 |
|---|---|---|
| deprecated 件数（261.26222.65） | | |
| deprecated 件数（261.27258.27） | | |
| deprecated 件数（262.9437.65） | | |
| テスト件数 | | |

## セクション 3: 自前クラス名のリネーム

- [ ] `lsp/RescriptLspServerSupportProvider.kt` → `lsp/RescriptLspIntegrationProvider.kt`（クラス名も）
- [ ] `lsp/RescriptLspServerDescriptor.kt` → `lsp/RescriptLspClientDescriptor.kt`（クラス名も）
- [ ] `lsp/RescriptLspUtils.kt` — `getServer` → `getClient`
- [ ] 呼び出し側の `getServer` → `getClient` 追随:
      `intention/RescriptAddMissingSwitchArmsIntention.kt` /
      `intention/RescriptBatchInsertInferredTypesIntention.kt` /
      `typeinfo/RescriptTypeInfoPanel.kt` /
      `lsp/RescriptExpressionTypeProvider.kt` /
      `inspection/RescriptSignatureSyncInspection.kt` /
      `navigation/RescriptOpenCompiledJsAction.kt` /
      `navigation/RescriptCreateInterfaceAction.kt` /
      `refactor/RescriptRenameHandler.kt` /
      `codevision/RescriptCodeVisionProvider.java`
- [ ] KDoc `@see` の追随: `documentation/RescriptDocumentationProvider.kt` /
      `lsp/RescriptLspDetector.kt` / `util/RescriptProcessUtils.kt` /
      `lsp/RescriptDumpLspStateAction.kt` / `lsp/RescriptRestartLspAction.kt` /
      `lsp/RescriptLspInstaller.kt`
- [ ] ローカル変数名 `server` / `lspServer` を `client` / `lspClient` へ揃える
      （ユーザーに見える文字列 `**LSP Server**` 等は変更しない）
- [ ] `plugin.xml` の `implementation` 属性を新クラス名へ
- [ ] テストクラスのリネーム: `RescriptLspServerSupportProviderTest.kt` →
      `RescriptLspIntegrationProviderTest.kt`。他テストの参照追随
- [ ] **`plugin.xml` の全 `implementation` / `implementationClass` / `class` 属性の値が
      実在するクラスの完全修飾名と一致することを `grep` で機械照合する**
      （AC-7 省略の緩和策。EP 登録失敗は実行時ログにしか出ないため）
- [ ] `grep -rn "RescriptLspServerSupportProvider\|RescriptLspServerDescriptor" src/` が 0 件
- [ ] `./gradlew ktlintCheck clean buildPlugin > /tmp/build.log 2>&1; tail -40 /tmp/build.log` が成功
- [ ] `./gradlew test > /tmp/test.log 2>&1; tail -40 /tmp/test.log` が全件成功
- [ ] コミット（`♻️ Rename the LSP provider and descriptor to match the LspClient API`）

## セクション 4: ドキュメント同期

`.claude/rules/documentation.md` の同期対象表および AC-8 に従う。

- [ ] `docs/repository-structure.md` — `lsp/` パッケージ表の代表クラス名
- [ ] `docs/functional-design.md` — LSP レイヤーの解説と **Extension Point マップの EP 名**
- [ ] `docs/glossary.md` — LSP 関連用語のクラス名
- [ ] `docs/lsp-fallback-matrix.md` — クラス名の言及
- [ ] `sphinx-docs/dev/architecture.md` — クラス名・EP 名
- [ ] `sphinx-docs/dev/project-structure.md` — クラス名
- [ ] 以下は **更新しない**（当時の記録・沿革のため。requirements.md のスコープ外に明記）:
      `docs/archive/implemented-features.md` / `docs/performance-validation.md` / `docs/ideas/concept.md`
- [ ] `README.md` / `CLAUDE.md` に旧クラス名の言及がないことを `grep` で確認する
      （あれば更新、なければ変更不要と記録する）
- [ ] `.po` 同期: `cd sphinx-docs && make gettext && make update-po`
- [ ] 空の `msgstr` を日本語で埋める / fuzzy エントリを確認して解除する
- [ ] `make build-ja` が成功する
- [ ] `docs-lint` スキルで同期崩れがないことを確認する
- [ ] コミット（`📝 Update docs for the LspClient API migration`）

## セクション 5: 完了処理

- [ ] requirements.md の AC-1〜AC-8 を確認し、状態を反映する
      （AC-7 は「省略」として `[x]` 済み。他は実測で判定する）
- [ ] `.claude/rules/definition-of-done.md` で Phase 1〜5 を確認する
- [ ] `git status` と `git log --oneline origin/main..HEAD` を実行し、**出力を引用したうえで**状態を報告する
- [ ] 本ファイルの全タスクが `[x]` であることを確認する（このタスク自身を含む）
- [ ] tasklist 更新をマージ前の最終コミットに含める
- [ ] マージ前に `git fetch origin` し、`main` が進んでいれば先に作業ブランチへ取り込む
      （前作業で並列セッションとの衝突が発生した経緯があるため）
- [ ] `AskUserQuestion` でマージ可否を確認する
  - [ ] **AC-7（実機スモークテスト）が未実施であること**と、その残存リスクを明示する
  - [ ] EP を `platform.lsp.integrationProvider` へ切り替えたこと（実行時挙動に直結）を明示する
- [ ] 承認後、`main` にマージしブランチを削除する
- [ ] セッションを終了する（worktree の自動クリーンアップを発動させる）

## 別作業に送る項目

- LSP 機能の実機スモークテスト（本作業で省略した AC-7）。**Marketplace リリース前に実施を推奨**
- `pluginVersion` バンプと Marketplace リリース（`0.1.17`）
- `template-versions-audit` の npm 脆弱性解消
- `RescriptSwitchFileActionTest.testActionPerformedOpensResCounterpart` のフレーキー対応
- Dependabot PR のマージとマージ済みリモートブランチの削除
