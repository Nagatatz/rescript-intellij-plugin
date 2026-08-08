# tasklist: IntelliJ Platform 2026.2 へのバンプ

## 前提と依存関係

- セクション 1 → 2 → 3 → 4 の順に依存する。**セクション 1 の完了で得られる実測結果（コンパイルエラー、
  `deprecated-usages.txt`、jar のクラス一覧）がセクション 2 以降の設計入力**となる
- 各セクションは単独でビルド・テストを通過し、`main` にマージできる粒度とする
- セクション 3 は代替 API が 2026.2 に実在した場合のみ実施。存在しなければセクション 3 を丸ごとスキップし、
  その旨を本ファイルに記録してセクション 4 に進む
- 作業は worktree 内で行う（ブランチ `worktree-intellij-2026-2-bump`）

## セクション 0: 準備

- [ ] `git fetch origin` 実行、ローカル `main` が `origin/main` と同期していることを確認
- [ ] `EnterWorktree` で worktree を作成
- [ ] worktree 内で `pwd` と `git rev-parse --show-toplevel` を実行し、編集対象が worktree 内であることを確認
- [ ] `.steering/20260808-001-intellij-2026-2-bump/` をコミット（`📝 Add steering docs for IntelliJ 2026.2 bump`）

## セクション 1: platformVersion のバンプ（単独で緑にする）

このセクションでは `@Suppress` も ignored-problems も **一切触らない**。
2026.2.0.1 でビルドが通るかどうかだけを切り分ける。

- [ ] `gradle.properties` の `platformVersion` を `2026.1.2` → `2026.2.0.1` に変更
- [ ] `pluginSinceBuild` は `253.0` のまま据え置く（D-1: 移行中は Verifier に旧 IDE も検証させるため）
- [ ] `./gradlew clean buildPlugin > /tmp/build.log 2>&1; tail -30 /tmp/build.log` を実行
- [ ] コンパイルエラーが出た場合、内容を本ファイルの「セクション 1 実測ログ」に転記する
- [ ] `LspServerManager.getInstance()` が解決可能か（互換シムが残っているか）を確認し記録する ← F-2 の核心
- [ ] `./gradlew ktlintCheck` が成功する
- [ ] `./gradlew test > /tmp/test.log 2>&1; tail -40 /tmp/test.log` が成功する
  - [ ] `RescriptSwitchFileActionTest.testActionPerformedOpensResCounterpart` が失敗した場合は
        フレーキー（別作業）として記録し、再実行で緑になることを確認する
- [ ] `./gradlew verifyPlugin > /tmp/verify.log 2>&1; tail -40 /tmp/verify.log` を実行
- [ ] `deprecated-usages.txt` の内容を「セクション 1 実測ログ」に転記する
- [ ] Verifier が検証対象にした IDE の一覧を記録する
- [ ] コミット（`🔧 Bump IntelliJ Platform to 2026.2.0.1`）

### セクション 1 実測ログ（実装時に記入）

> D-4 の未確定 API の対応先をここに確定させる。推測で埋めないこと。

| 項目 | 実測結果 |
|---|---|
| ビルド成否 | （未記入） |
| コンパイルエラー | （未記入） |
| `LspServerManager.getInstance()` の可否 | （未記入） |
| `LspServerNotificationsHandler` の対応先 | （未記入） |
| `Lsp4jClient` の対応先 | （未記入） |
| `LspServerState` の対応先 | （未記入） |
| `LspCustomization` / `LspSemanticTokensSupport` の対応先 | （未記入） |
| Verifier 検証対象 IDE | （未記入） |
| `deprecated-usages.txt` の新規項目 | （未記入） |

確認手順（design.md D-4）:

- [ ] 解決済み jar を特定: `find ~/.gradle/caches/modules-2/files-2.1/com.jetbrains.intellij.idea -name "*.jar" | xargs -I{} sh -c 'unzip -l "{}" 2>/dev/null | grep -q "platform/lsp/api" && echo "{}"' | head -1`
- [ ] クラス一覧を確認: `unzip -l <jar> | grep "platform/lsp/api"`
- [ ] シグネチャを確認: `javap -classpath <jar> com.intellij.platform.lsp.api.LspClientManager`
- [ ] `FloatingToolbarProvider` に `isApplicableAsync` が存在するか確認 → セクション 3 の実施可否を判定
- [ ] `FileIncludeProvider` に `acceptFile(IndexedFile)` が存在するか確認 → 同上
- [ ] PolySymbols の `PsiSourced*` を本プラグインが参照していないことを `grep -rn "PsiSourced" src/` で再確認（F-3）

## セクション 2: LSP API の移行（必須）

F-2 のとおり `LspServerManager` は runtime で壊れうるため、このセクションは省略できない。
セクション 1 実測ログで確定した対応表に従って置換する。

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

## セクション 3: FloatingToolbarProvider / FileIncludeProvider の移行

**セクション 1 実測ログで代替 API の存在を確認できた場合のみ実施。**
存在しない場合はこのセクション全体をスキップし、以下に理由を記録してセクション 4 へ進む。

> スキップ理由（該当する場合に記入）: （未記入）

- [ ] `editor/RescriptFloatingToolbarProvider.kt` を `isApplicableAsync` へ移行
- [ ] `editor/RescriptFloatingToolbarProviderTest.kt` を新シグネチャに追従させ、緑にする
- [ ] `navigation/RescriptFileIncludeProvider.kt` を `acceptFile(IndexedFile)` へ移行
- [ ] `navigation/RescriptFileIncludeProviderTest.kt` を新シグネチャに追従させ、緑にする
- [ ] 両ファイルの `@Suppress("DEPRECATION")` を除去
- [ ] `./gradlew ktlintCheck clean buildPlugin test` が成功する
- [ ] コミット（`♻️ Migrate FloatingToolbarProvider and FileIncludeProvider to the current APIs`）

## セクション 4: sinceBuild 引き上げ・ignored-problems 棚卸し・ドキュメント同期

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

- [ ] LSP API のエントリ（L47-67）を削除（セクション 2 で解消済みのため）
- [ ] `FloatingToolbarProvider.isApplicable` のエントリ（L34-38）— 解消したら削除、未解消なら
      `Status` の理由を「2026.2 にも代替なし」に更新し `Reviewed: 2026-08-08` / `Expires: 2027-08-08` に更新
- [ ] `FileIncludeProvider.acceptFile` のエントリ（L40-45）— 同上
- [ ] `CodeVisionPlaceholderCollector` のエントリ（L14-22）— 2026.2 でも `@Internal` が残るか確認し
      `Reviewed` を更新（Expires は 2027-04-29 のまま据え置きでよい）
- [ ] ファイル冒頭の `Reviewed: 2026-05-14 | Target: IntelliJ 2025.3+` を
      `Reviewed: 2026-08-08 | Target: IntelliJ 2026.1.4+` に更新
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
- 自前クラス名のリネーム（`RescriptLspServerSupportProvider` → `RescriptLspIntegrationProvider` 等、D-3）
- Dependabot PR #63 / #64 / #65 のマージ
- マージ済み Dependabot リモートブランチの削除
