# LSP Code Action 動作検証 — 次のアクション

本ステアリング（`20260427-004-lsp-code-action-verification/`）は、静的分析の結論をもって完了した。実機検証 (Phase 3) は別タスクに分離する。

## 1. 実機検証（独立タスク）

`./gradlew runIde` でサンドボックス IDE を起動し、`samples/01_*.res` 〜 `samples/09_*.res` を実プロジェクトにコピーして以下を確認する。手順は本ステアリングの `design.md` 「検証手順」セクションを再利用してよい。

### 検証チェックリスト

- [ ] `01_missing_cases.res`: switch 不完全マッチに `simpleAddMissingCases` が表示・適用される
- [ ] `02_wrap_in_some.res`: option 型不整合に `wrapInSome` が表示・適用される
- [ ] `03_record_missing_fields.res`: record 必須フィールド欠落に `addUndefinedRecordFields` が表示・適用される
- [ ] `04_simple_conversion.res`: int/string 不整合に `simpleConversion` が表示・適用される
- [ ] `05_did_you_mean.res`: typo 識別子に `didYouMean` が表示・適用される
- [ ] `06_remove_unused.res`: reanalyze 有効化 (`rescript.json` の `"reanalyze": {"analysis": ["dce"]}`) 下で `removeUnusedCode` が表示・適用される
- [ ] `07_extract_local_module.res`: ローカルモジュール宣言上で `extractLocalModuleToFile` が表示され、新規 `.res` ファイルが正しく作成される
- [ ] `08_expand_catch_all.res`: switch の `_ =>` ケース上で `expandCatchAllPatterns` が表示・適用される
- [ ] `09_apply_uncurried.res`: 同梱 ReScript バージョン下で `applyUncurried` が表示・適用される、または発火しないことを `N/A` として記録

### 検証結果が NG / PARTIAL の場合の起票テンプレート

新規ステアリング `20260MMDD-NNN-native-<action>-quickfix/` を作成し、以下を含める:

- 失敗した code action ID（rescript-vscode 命名）
- LSP ログ抜粋（`Help | Show Log in Finder` の `idea.log`）
- 想定原因（本ステアリング `findings.md` の「原因分析」 1〜5 を参照して特定）
- ネイティブ Quick Fix の必要性判定:
  - PSI ベースで実装可能か（診断パターンが正規表現で安定して取れるか）
  - 既存の `quickfix/` パッケージ（`RescriptAddOpenQuickFix` 等）と命名・配置を揃える

### 想定される追加調査の出力先

- `docs/lsp-fallback-matrix.md`: 動作しない code action の行を追加（「LSP 接続済みでも未動作」のカテゴリ）
- `docs/archive/implemented-features.md`: ネイティブ実装した行を追加
- 本ステアリングの `findings.md` 個別ケース項目を埋める（履歴記録）

## 2. ネイティブ Quick Fix 候補（事前推測）

実機検証前の推測ベースで、ネイティブ実装の必要性が高いと考えられるのは以下:

| 候補 | 推測される NG 理由 | 実装難易度 |
|---|---|---|
| `applyUncurried` | ReScript v11+ uncurried-by-default で診断が出ないため発火しない | 中（PSI で `let f = (. x) =>` パターン検出 + curried call 補完） |
| `extractLocalModuleToFile` | `WorkspaceEdit.documentChanges` の `CreateFile` 操作を IntelliJ が処理しない可能性 | 高（PSI で `module M = { ... }` 範囲を切り出して `WriteCommandAction` で新規ファイル作成） |
| `removeUnusedCode` | reanalyze 連携が動かない場合 | 既存の `RescriptReanalyzeAnnotator` のクイックフィックスで代替済み（重複実装回避） |

実機検証で具体的な NG が確認された段階で再評価する。

## 3. ロードマップへの反映

`docs/product-requirements.md` の「将来機能（ロードマップ）」テーブルへの追加判断:

- 実機検証で全 9 種が動作: ロードマップ追加なし（静的分析結論を docs に反映済み）
- 実機検証で 1〜数種が NG: 該当 code action ごとにネイティブ実装候補を `Quick Fix` カテゴリで起票
- 実機検証で全種 NG: `RescriptLspServerDescriptor.lspCustomization` の調整を最初に試みる（API 不足ではなくサーバー連携の問題が疑われる）
