# Notebook 風 Worksheet — Requirements

## 背景

ReScript で「コードを試行錯誤する」用途では、現状 2 つの選択肢がある:

1. **既存 `.resw` Worksheet** — ファイル全体を一括評価し、結果をインライン `// =>` でコメント付与する（実装は placeholder で未完）
2. **REPL ツールウィンドウ** — 1 行ずつ評価できるが、結果が履歴に流れていき、後で見返したり共有したりしにくい

両者の中間にある Jupyter / Polynote のような **セルベース・notebook** がない。データ探索、API 検証、教材作成などのユースケースで、コードと結果を順序立てて保存・共有したいニーズがある。

本機能はこのギャップを埋めるため、**`.resnb` 拡張子の新ファイル形式** で notebook 風 Worksheet を提供する。各セルは独立した ReScript コードブロックで、評価結果（標準出力・コンパイルエラーなど）と一緒に JSON 形式でファイルに保存される。

## ユーザーストーリー

### US-Notebook-01: セルベースのファイル形式

**ReScript 開発者として**、コードを順序立てた一連のセルとして保存し、評価結果と一緒にファイル化したい。

**受け入れ条件（Phase 1 実装スコープ）:**

- [x] `.resnb` ファイルが「ReScript Notebook」として認識される（FileType + Icon）
- [x] ファイル形式は JSON で、`{ "version": 1, "cells": [{ "code": "...", "lastOutput": "..." }] }` のスキーマを持つ
- [x] 空の `.resnb` ファイル（または空文字列）は `{ "version": 1, "cells": [] }` として扱われる
- [x] 不正な JSON は警告ヘッダーで通知し、空 Notebook にフォールバックする（ファイル全体を失わない）

### US-Notebook-02: セル UI と編集

**Notebook を編集する開発者として**、セルを縦に並べて編集し、各セルを独立して評価できる UI を期待する。

**受け入れ条件（Phase 1 実装スコープ）:**

- [x] `.resnb` を開くと専用の `FileEditor` が起動し、セルが縦に並んで表示される
- [x] 各セルには「コード入力エリア（multi-line text area）」「Run ボタン」「結果表示エリア（read-only）」がある
- [x] ツールバーに「Add Cell」「Run All」「Copy as Markdown」ボタンがある
- [x] 各セルの上部に「Move Up」「Move Down」「Delete」アイコンボタンがある
- [x] セル数が 0 のとき、自動的に空セル 1 つを追加する
- [x] 編集内容は通常の Save (`Ctrl+S`) でファイルに JSON 形式で保存される

### US-Notebook-03: セル評価と結果表示

**コードを評価したい開発者として**、各セルの「Run」ボタンを押すと結果がそのセルの出力エリアに表示されることを期待する。

**受け入れ条件（Phase 1 実装スコープ）:**

- [x] Run ボタンを押すと既存の `RescriptReplExecutor` 経由でセルコードが評価される
- [x] 評価結果（stdout + stderr）が結果エリアに表示される
- [x] コンパイルエラーは赤、成功時の出力は通常色で表示する
- [x] 評価中はボタンが「Running…」に変わり、ダブル実行を防ぐ
- [x] 評価結果は `cell.lastOutput` として保存される（次回ファイルを開いても結果が残る）

### US-Notebook-04: Markdown エクスポート（基本のみ）

**Notebook をブログ記事や PR コメントに貼り付けたい開発者として**、Markdown 形式で Notebook 全体をエクスポートしたい。

**受け入れ条件（Phase 1 実装スコープ）:**

- [x] ツールバーに「Copy as Markdown」ボタンがあり、現在の Notebook を Markdown 文字列にしてクリップボードにコピーできる
- [x] Markdown 形式: 各セルを ` ```rescript ... ``` ` でコードブロック、その下に ` ``` ... ``` ` で出力ブロックを並べる

## スコープ外（Phase 1）

- セル間の状態共有（前のセルの `let` を後のセルから参照）— 各セルは独立して評価される MVP
- リッチ出力（HTML、画像、グラフ）
- Markdown セル（コード以外のセル）
- セル並列実行・キャンセル
- バージョン管理ツール（Git diff の見やすさ等）
- LSP 統合（補完・診断）— Phase 2 以降
- セル評価のキャッシュ・差分実行
- セルエディタのシンタックスハイライト

## 受け入れ確認

- [ ] 空 Notebook を開いて 1 セル追加・評価・保存・再オープンが動作する — マージ後にユーザー側で手動検証
- [ ] 3 セルの Notebook を Markdown エクスポートして外部に貼れる — マージ後にユーザー側で手動検証（Exporter のスナップショットテストはユニットテストでカバー）
- [x] 不正な JSON ファイルを開いてもクラッシュしない（FileEditor の fallback メッセージ + Serializer の例外パスをユニットテスト）
- [x] ユニットテストで JSON シリアライザ・パーサと Markdown エクスポータをスナップショット検証する（Serializer 8 / Markdown 4 ケース）

## 非機能要件

- セル UI は Swing コンポーネント（`JBPanel`、`JTextArea`、`JButton`）で構築する
- 評価はバックグラウンドスレッド（`executeOnPooledThread`）で行い、UI スレッドをブロックしない
- 1 評価のタイムアウトは既存 `RescriptReplExecutor` の 30 秒に従う
