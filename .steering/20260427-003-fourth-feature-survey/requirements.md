# 第 4 回 機能調査: 他プラグインからの移植候補発掘

## 背景

本プラグインは過去 3 回の機能調査で 109 件の候補を集め、2026-04 時点で 174 機能を実装完了している（`docs/archive/implemented-features.md`）。`docs/product-requirements.md` のロードマップは「現時点で計画中の機能はない」状態となり、能動的な機能発掘の必要性が高まった。

本調査は **競合・近接プラグインを横断して、まだ採用していない機能を発掘する** ことを目的とする 4 回目の機能調査である。実装は本 steering の対象外（個別の機能ごとに別 steering を切る）。

## 目的

- **DX ギャップの可視化**: rescript-vscode / reasonml-idea-plugin に存在し、本プラグインに無い機能を網羅的にリストアップする
- **プラットフォーム追従の棚卸し**: IntelliJ Platform 2025.3 / 2026.x で公開された Extension Point のうち、未活用のものを特定する
- **クロス言語ベンチマーク**: IntelliJ Rust / Scala / Haskell / OCaml の機能から、ReScript に移植価値が高いものを抽出する

## 受け入れ条件

- [x] reasonml-idea-plugin（先行 IntelliJ プラグイン）の現行機能を調査済み
- [x] rescript-vscode（公式 VSCode 拡張）の `package.json` contributes と `codeActions.ts` を確認済み
- [x] IntelliJ Rust / Scala / Haskell / OCaml / Erlang プラグインの代表的機能を調査済み
- [x] IntelliJ Platform 2025.3 / 2026.x の Extension Point / Notable API を確認済み
- [x] 候補機能の重複を実コード（`src/main/`、`plugin.xml`）と照合し、既実装のものは除外済み
- [x] 残った候補に S/A/B/C 優先度を付与した一覧を `candidates.md` として保存済み
- [x] 不確実性の高い項目を `findings.md` の "要追加調査" セクションに記録済み

## スコープ外

- 候補機能の実装（個別 steering で扱う）
- ユーザー要望ヒアリング / アンケート（本調査は文献ベースのみ）
- 競合 IDE プラットフォーム（VSCode 以外の Zed、Helix 等）の調査

## 成果物

- `requirements.md`（本ファイル）
- `findings.md`: 各プラグインの調査要約と既実装との照合結果
- `candidates.md`: 移植価値が高い未実装機能の優先度付きリスト
