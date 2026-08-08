# requirements: LSP API の LspClient 系への移行

## 背景

`.steering/20260808-001-intellij-2026-2-bump/` のセクション 2 として計画されていたが、
2026-08-09 のユーザー判断で **後続作業に切り出された**もの。切り出し時の記録は
当該 tasklist.md の「セクション 2」冒頭および「別作業に送る項目」を参照。

切り出しの理由は「2026.2 でビルドでき、リリースできる状態を最短で作る」ことを優先したためであり、
移行そのものが不要と判断されたわけではない。現状は以下のとおり:

- IntelliJ Platform 2026.2 では `LspServerSupportProvider` / `LspServerDescriptor` /
  `LspServer` / `LspServerManager` 系がすべて deprecated
- 本プラグインは 13 ファイルでこれらを使用し、`@Suppress("DEPRECATION")` で警告を抑制している
- `plugin-verifier-ignored-problems.txt` の `com.intellij.platform.lsp.api.*` エントリで
  Plugin Verifier の報告を抑制している（`Expires: 2027-02-09`）
- Verifier の deprecated 報告は 261 系 35 件 / 262 系 36 件。うち **35 件が LSP 関連**

`.claude/rules/deprecated-api.md` は新規実装での deprecated API 参照を禁じており、
既存箇所も代替 API が存在するなら移行することを求めている。本作業でこれを解消する。

## 目的

1. プラットフォームの LSP API 参照を、deprecated な `LspServer*` 系から現行の
   `LspIntegrationProvider` / `LspClientDescriptor` / `LspClient` / `LspClientManager` 系へ移行する
2. 上記に伴い不要になる `@Suppress("DEPRECATION")` / `@Suppress("OVERRIDE_DEPRECATION")` を除去する
3. `plugin-verifier-ignored-problems.txt` の LSP エントリを削除する
4. 自前クラス名（`RescriptLspServerSupportProvider` / `RescriptLspServerDescriptor`）を
   新 API の命名に合わせてリネームする（2026-08-09 のユーザー判断でスコープに含めた）

## 前提となる実測結果（本作業の着手前に確認済み）

推測ではなく `javap` と EP 宣言の実測で確認した。確認手順と結果は design.md に転記する。

- 新 API 一式（`LspIntegrationProvider` / `LspClientManager` / `LspClientDescriptor` /
  `ProjectWideLspClientDescriptor` / `LspClient` / `LspIntegrationProvider$LspClientStarter`）は
  **`IU-261.27258.27` と `IU-262.9437.65` の両方に同一シグネチャで存在する**
- EP `com.intellij.platform.lsp.integrationProvider` は **両バージョンの
  `intellij.platform.lsp.impl.xml` に宣言されている**（旧 EP `serverSupportProvider` と併存）
- `LspClient.sendRequestSync(int, Function1)` は新インタフェース側に存在し、**deprecated ではない**
- `LspServerState` / `Lsp4jClient` / `LspServerNotificationsHandler` /
  `customization/*` は新旧で名称変更なし

## スコープ

### 含むもの

| 対象 | 内容 |
|---|---|
| `src/main` の 13 ファイル | プラットフォーム LSP 型の参照を新 API へ置換 |
| `plugin.xml` | EP を `platform.lsp.serverSupportProvider` → `platform.lsp.integrationProvider` へ変更 |
| 自前クラス 2 件のリネーム | `RescriptLspServerSupportProvider` → `RescriptLspIntegrationProvider`、`RescriptLspServerDescriptor` → `RescriptLspClientDescriptor` |
| `RescriptLspUtils` の API 名 | `getServer(project): LspServer?` → `getClient(project): LspClient?` |
| テスト | 既存 LSP テストの追随 + 新規リグレッションテスト |
| `plugin-verifier-ignored-problems.txt` | LSP エントリの削除 |
| `build.gradle.kts` | Verifier の検証対象 IDE に下限ビルド `IU-261.26222.65` (2026.1.4 GA) を明示追加 |
| ドキュメント | 現行実装を説明する docs / sphinx-docs と `.po` の同期 |

### 含まないもの

- LSP 通信ロジック・スレッドモデル・タイムアウト値の変更（純粋な API 移行に留める）
- `RescriptLanguageServer`（lsp4j の `LanguageServer` 実装。プラットフォーム API とは無関係）のリネーム
- `RescriptReanalyzeServerService` / `RescriptReanalyzeServerStartupActivity`
  （ReScript の analysis server を指す名前であり LSP API とは無関係）
- `pluginVersion` のバンプと Marketplace リリース
- 歴史的記録ドキュメント（`docs/archive/`、`docs/performance-validation.md`、
  `docs/ideas/concept.md`）の旧クラス名の書き換え

## 受け入れ条件

- [ ] **AC-1**: `grep -rn "LspServerSupportProvider\|LspServerDescriptor\|ProjectWideLspServerDescriptor\|LspServerManager" src/` が 0 件
      （`LspServerState` / `LspServerNotificationsHandler` は新 API 側でも同名のため対象外）
- [ ] **AC-2**: `src/` に LSP 由来の `@Suppress("DEPRECATION")` / `@Suppress("OVERRIDE_DEPRECATION")` /
      `@SuppressWarnings("deprecation")` が残っていない
- [ ] **AC-3**: `./gradlew ktlintCheck clean buildPlugin` が成功する
- [ ] **AC-4**: `./gradlew test` が全件成功する（現在の基準は 4464 tests / failures 0）
- [ ] **AC-5**: `./gradlew verifyPlugin` が **下限ビルド `IU-261.26222.65` (2026.1.4 GA) を含む
      すべての検証対象 IDE** で `Compatible`。かつ `deprecated-usages.txt` の LSP 関連 35 件が **0 件**になる
- [ ] **AC-5b**: `build.gradle.kts` の `pluginVerification.ides { }` に `ide("IU", "261.26222.65")` が
      追加され、`recommended()` が拾わない下限ビルドも検証対象になっている
- [ ] **AC-6**: `plugin-verifier-ignored-problems.txt` から
      `com.intellij.platform.lsp.api.*` のエントリが削除されている
- [x] **AC-7**: ~~実機スモークテスト（`runIde`）で LSP サーバー起動・補完・診断・定義ジャンプ・ホバー・
      `Restart LSP Server` / `Dump LSP State` アクションの動作を確認する~~
      → **省略する（2026-08-09 のユーザー判断）**。下記「AC-7 省略の経緯と残存リスク」を参照
- [ ] **AC-8**: 現行実装を説明するドキュメント（`docs/repository-structure.md` /
      `docs/functional-design.md` / `docs/glossary.md` / `docs/lsp-fallback-matrix.md` /
      `sphinx-docs/dev/architecture.md` / `sphinx-docs/dev/project-structure.md`）の
      クラス名・EP 名が更新され、`.po` が同期されて `make build-ja` が成功する

### AC-7 省略の経緯と残存リスク

当初、本ドキュメントは AC-7 を「省略不可」と定義していた。実装担当（Claude）が挙げた根拠は
以下のとおりで、これらは **いずれもコンパイルが通るため静的検証では検出できない**:

- EP を新名称に切り替えたことで、LSP サーバーがそもそも起動しなくなる
- `LspClientManager` 経由のアクション（Restart / Dump）が実行時に対象クライアントを見つけられない
- `plugin.xml` の `implementation` 属性とクラス名のリネームがずれ、EP 登録が壊れる

これに対し **2026-08-09 にユーザーが「今回も AC-7 を省略する」と判断した**ため、
その判断に従い省略する。前作業（2026.2 バンプ）でも Gradle のロック競合により AC-7 は未実施であり、
**LSP 機能が 2026.2 上で実際に動作することは 2 作業連続で未確認のまま**となる。

省略により受容するリスク:

| リスク | 検出できるまでの猶予 |
|---|---|
| 新 EP へ切り替えた結果 LSP が起動しない | Marketplace リリース後、ユーザー報告まで検出されない |
| `Restart LSP Server` / `Dump LSP State` が実行時に無反応になる | 同上 |
| リネームによる `plugin.xml` の `implementation` 属性のずれ | 同上（EP 登録失敗はログにのみ出る） |

緩和として本作業で行うこと:

- `verifyPlugin` の検証対象に下限ビルド `IU-261.26222.65` を追加し、静的検証の網を最大化する（AC-5b）
- `plugin.xml` の `implementation` 属性とクラスの完全修飾名の一致を、リネームコミット後に
  `grep` で機械的に照合する（tasklist に明示タスクとして置く）
- `fileOpened` が `.res` / `.resi` でのみ `ensureClientStarted` を呼ぶことを
  ユニットテストで検証する（EP 起動経路のうちテスト可能な部分を可能な限り押さえる）

**マージ確認時に、AC-7 未実施であることをユーザーへ再掲すること。**

## リスク

| リスク | 内容 | 緩和策 |
|---|---|---|
| R-1 | 新 EP へ切り替えた結果 LSP が起動しなくなる | AC-7 の実機スモークテストを省略しない |
| R-2 | 旧 EP と新 EP を併記すると LSP サーバーが二重起動する | 前作業の設計判断 D-5 に従い **新 EP のみ**を登録する |
| R-3 | `pluginSinceBuild` の下限 `261.26222` (2026.1.4) に新 API が無い可能性 | **解決済み**。`recommended()` が拾う 261 系は `261.27258.27` = **2026.1.5 RC**（未 GA）であり、下限そのものを検証していなかった。`ides { }` に `ide("IU", "261.26222.65")` (2026.1.4 GA) を明示追加し、Verifier に下限を直接検証させる（2026-08-09 のユーザー判断）。`pluginSinceBuild` は据え置く |
| R-4 | リネームによる差分肥大でレビューが困難になる | API 移行とリネームをコミット単位で分離する |
