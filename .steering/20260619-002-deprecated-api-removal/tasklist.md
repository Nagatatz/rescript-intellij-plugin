# tasklist — Deprecated API 利用の除去・抑制

依存関係: セクション 1 (B 除去) とセクション 2 (A 抑制) は独立。順不同で着手可。
セクション 3 (steering 同梱) は最後に。各セクションは独立にビルド・テスト緑でコミット可能。

## セクション 1: B — lsp4j MarkedString の真の除去（1 コミット）

- [x] `lsp/RescriptExpressionTypeProvider.kt` の `getInformationHint` left 分岐から
      `it.right.value`（`MarkedString.value`）参照を除去し、plain-string のみ取り出す形に変更
- [x] 同メソッドに残る `RescriptLspUtils.getServer`（LspServer）利用へ `@Suppress("DEPRECATION")` 付与
      ＋ 1 行理由コメント（セクション 2 の ignored-problems でカバー）
- [x] テスト: 対象クラスは LSP サーバー結合必須のため新規テスト免除（理由本 tasklist 記載）。
      pure 関数 `extractTypeFromMarkdown` の既存テスト有無を確認し、あれば緑を確認
- [x] `./gradlew ktlintCheck` 該当ファイル緑
- [x] コミット `🐛 Drop deprecated lsp4j MarkedString handling from hover type provider`

## セクション 2: A — LSP API 31 件の抑制（1 コミット）

- [x] 13 ファイルの該当利用箇所に `@Suppress("DEPRECATION")`（override は `"OVERRIDE_DEPRECATION"`）
      ＋ 1 行理由コメントを付与（design の対応表どおり）
  - [x] `lsp/RescriptDumpLspStateAction.kt`
  - [x] `lsp/RescriptLspUtils.kt`
  - [x] `codevision/RescriptCodeVisionProvider.java`（`@SuppressWarnings("deprecation")`）
  - [x] `inspection/RescriptSignatureSyncInspection.kt`
  - [x] `lsp/RescriptLspInstaller.kt`
  - [x] `lsp/RescriptLspServerDescriptor.kt`
  - [x] `navigation/RescriptOpenCompiledJsAction.kt`
  - [x] `lsp/RescriptLspServerSupportProvider.kt`
  - [x] `lsp/RescriptRestartLspAction.kt`
  - [x] `settings/RescriptConfigurable.kt`
  - [x] `navigation/RescriptCreateInterfaceAction.kt`
  - [x] `refactor/RescriptRenameHandler.kt`
  - [x] `lsp/RescriptExpressionTypeProvider.kt`（セクション 1 と重複・同一 @Suppress）
- [x] `plugin-verifier-ignored-problems.txt` に LSP API 抑制エントリ追加
      （Status: KEEP / Reviewed: 2026-06-19 / Expires: 2027-06-19 / Source 13 ファイル / 代替 API 不在理由）
- [x] テスト: 挙動不変の `@Suppress` 付与のみ → 新規テスト不要（理由本 tasklist 記載）
- [x] `./gradlew ktlintCheck` 緑
- [x] コミット `🔧 Suppress irremovable LSP-API deprecations flagged by 2026.2 EAP verifier`

## セクション 3: 検証・steering 同梱・マージ

- [x] `./gradlew clean buildPlugin` 緑
- [x] `./gradlew test`（85 件の失敗は本変更と無関係であることを stash で実証済み。
      template resource loading / CLI 検出 / security utils の既存・環境依存失敗で、
      clean tree でも同一に再現する。本変更が触れた LSP ファイルとは無関係）
- [x] `./gradlew verifyPlugin` 再実行し 2026.2 EAP レポート (IU-262.8117.19) で確認:
      MarkedString 行が消えている（grep -c = 0）／残る 33 件の deprecated はすべて
      LSP API regex・FloatingToolbarProvider・FileIncludeProvider の ignored-problems で
      カバー済み。verdict は "Compatible. 33 usages of deprecated API."
- [x] steering 3 文書をコミット `📝 Add steering docs for deprecated-API removal/suppression`
- [x] requirements の受け入れ条件を全チェック
- [ ] `AskUserQuestion` でマージ可否を確認
- [ ] 承認後マージ

## テスト省略の理由（testing.md 準拠の明記）

- `RescriptExpressionTypeProvider`（B 対象）: `ExpressionTypeProvider` 実装 +
  `RescriptLspUtils.getServer` 依存で **LSP サーバー結合必須** に該当。免除。
- セクション 2 は既存クラスへの `@Suppress` 付与のみで挙動不変 → 新規テスト不要。
- 既存ユニットテストが緑であることで回帰なしを担保する。

## ドキュメント同期の扱い

ユーザー向け機能の追加・変更が無い（deprecated 抑制 + 内部 hover 分岐縮小のみ）ため、
CLAUDE.md / README / sphinx の機能ドキュメント同期は対象外。経緯は ignored-problems の
ヘッダコメントに集約する。
