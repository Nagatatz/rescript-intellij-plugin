# 要求内容: 低リスク util 抽出 (完全リファクタリング Phase 1)

## 背景

完全リファクタリング計画 (プランファイル: `~/.claude/plans/virtual-exploring-kernighan.md`) の Phase 1。
ロードマップ #126 (EditorTextField factory) と、一次調査で新発見された scanner 走査ループの重複を解消する。
直近の `20260519-003-refactor-ui-helpers` (HtmlEditorPaneFactory 抽出) と同型の低リスク抽出であり、後続フェーズの worktree 運用・kover ラチェット・テスト免除記載のリハーサルを兼ねる。

## 対象の重複 (実コードで確認済み)

### 1. EditorTextField の addSettingsProvider 定型 (#126)

3 箇所で共通 3 設定 + 固有 1 設定:

| ファイル | 共通設定 | 固有設定 |
|---|---|---|
| `repl/RescriptReplPanel.kt:84` | lineNumbers / foldingOutline / rightMargin = false | `contentComponent.isFocusable = true` |
| `notebook/RescriptNotebookCellPanel.kt:58` | 同上 | `isUseSoftWraps = true` |
| `typeinfo/RescriptTypeInfoPanel.kt:57` | 同上 | `isCaretRowShown = false` |

### 2. scanner の FileTypeIndex 走査ループ

`coverage/RescriptTypeCoverageScanner.scan` (L40-61) と `interop/RescriptInteropScanner.scan` (L39-62) で
「read action → projectScope → FileTypeIndex.getFiles → ループ先頭でキャップ判定 → contentsToByteArray の安全読み取り → 処理」が重複 (~20 行 × 2)。
相違点: coverage はファイル数キャップ / interop はエントリ数キャップ + `.res`+`.resi` の 2 FileType。

## 要求

1. `util/EditorTextFieldFactory` を新設し、3 panel の定型を集約する (固有設定は customizer で残す)
2. `util/RescriptProjectFileScanner` を新設し、2 scanner の走査ループを集約する
3. 両 scanner の **公開シグネチャ・戻り値型・truncated 判定の挙動を変えない**
4. ロードマップ #126 の進捗管理 (着手時 🚧、完了時に実装済みへ移動)

## 受け入れ条件

- [ ] 既存テスト (scanner 系 4 本 + interop IntegrationTest + repl/notebook/typeinfo 関連) が **無変更で** green
- [ ] 新 util 2 クラスに KDoc + ユニットテストが付いている
- [ ] `./gradlew ktlintCheck clean buildPlugin test` + `koverVerify` (minBound 86) が通る
- [ ] `docs/repository-structure.md` の util/ 行に新クラスが追記されている
- [ ] `docs/product-requirements.md` から #126 が実装済み扱いに移動している
- [ ] sphinx-docs は更新なし (ユーザー向け機能不変のため)

## スコープ外

- ui/ パッケージ新設・panel 基盤クラス (Phase 2)
- typeinfo の Alarm 置換 (Phase 4)
