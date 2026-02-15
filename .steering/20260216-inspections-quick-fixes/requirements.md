# Requirements: Inspections & Quick Fixes

## 概要

ReScript IntelliJ プラグインに IntelliJ の Inspection フレームワークを活用した静的解析機能と Quick Fix（自動修正）を追加する。軽量パーサーの PSI ツリーおよびプロジェクト設定に基づく検査を提供し、LSP 診断を補完する。

## 背景

- 現在のプラグインは LSP 経由のコンパイラ診断（エラー・警告）のみを表示している
- IntelliJ の Inspection フレームワークを活用することで、コンパイラが検出しない構造的問題やプロジェクト設定の問題を検出できる
- Quick Fix により、検出された問題をワンクリックで修正できる

## ユーザーストーリー

### US-I01: 重複 open 文の検出

**ユーザーとして**、同一ファイル内で同じモジュールを複数回 `open` していることを警告で知り、ワンクリックで重複を除去したい。

**受け入れ条件:**
- [ ] 同一ファイルの同一スコープ内に同じモジュールパスの `open` 文が複数存在する場合、2つ目以降に警告を表示する
- [ ] 警告のホバーに「Duplicate open statement」のメッセージを表示する
- [ ] Quick Fix「Remove duplicate open」で重複する `open` 文を削除できる
- [ ] 削除後、空行が連続する場合は1行に整理する

### US-I02: 空モジュール宣言の検出

**ユーザーとして**、中身のないモジュール宣言（`module Foo = {}`）を情報レベルで通知され、不要なら削除したい。

**受け入れ条件:**
- [ ] `module Name = {}` のように子宣言を含まないモジュール宣言を WEAK WARNING として検出する
- [ ] Quick Fix「Remove empty module」でモジュール宣言全体を削除できる

### US-I03: rescript.json 不在の検出

**ユーザーとして**、プロジェクトに `rescript.json`（または `bsconfig.json`）が存在しないことを警告で知り、LSP が正常動作しない原因を素早く特定したい。

**受け入れ条件:**
- [ ] `.res` / `.resi` ファイルを開いた際、プロジェクトルートに `rescript.json` も `bsconfig.json` も存在しない場合に WARNING を表示する
- [ ] 警告メッセージに「rescript.json not found in project root. LSP features may not work correctly.」を含める
- [ ] ファイル単位ではなくプロジェクト単位の検査として1回だけ表示する

### US-I04: Inspection 設定の統合

**ユーザーとして**、Settings > Editor > Inspections から ReScript の各 Inspection を個別に有効/無効にしたり、重大度を変更したい。

**受け入れ条件:**
- [ ] Settings > Editor > Inspections に「ReScript」グループが表示される
- [ ] 各 Inspection の有効/無効を切り替えられる
- [ ] 重大度（Error / Warning / Weak Warning / Info）を変更できる

## 制約事項

- 軽量パーサーはトップレベル宣言のみ認識するため、式レベルの検査（未使用変数、到達不能コード等）は実装しない
- セマンティック解析を要する検査（未使用 open、型エラー等）は LSP 診断に委譲する
- Inspection はエディタのパフォーマンスに影響を与えないよう、O(n) 以内の処理とする

## スコープ外

- 式レベルの Inspection（変数の未使用、型の不一致等）
- コードスタイル系 Inspection（命名規則、インデント等）
- Intention Action（Inspection とは異なるコード変換提案）
