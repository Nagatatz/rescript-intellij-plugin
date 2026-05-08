# Type Impact Preview — Requirements

## 背景

ReScript はノミナルな variant・record・polymorphic variant を多用するため、共有モジュールの型を変更すると往々にしてプロジェクト全体に波及する。たとえば `User.t` のフィールド追加・renaming や `Event.t` の variant 追加は、`switch` exhaustiveness、record literal、関数引数など多数の場所に影響する。

現状、開発者は **変更後にコンパイルしてみないと影響範囲が分からない**。LSP の `textDocument/references` を都度実行する手もあるが、複数の型を一度に把握できない。本機能は **カーソル下の型定義に対する影響箇所をリアルタイムで一覧表示する ToolWindow** を提供し、「壊れる呼び出し側」を可視化する。

「破壊的変更の予算」を見積もる手段として、Rename / Extract / Change Signature と並ぶ補助ツールに位置付ける。

## ユーザーストーリー

### US-Impact-01: カーソル下の型の参照箇所を一覧表示

**ReScript 開発者として**、エディタでカーソルを型宣言に置いた状態で、その型がプロジェクト内のどこから参照されているかを ToolWindow で確認したい。

**受け入れ条件（Phase 1 実装スコープ）:**

- [x] `Tools > Show Type Impact` メニューから ToolWindow を開ける
- [x] ToolWindow を開くと、カーソルを置いている `type` 宣言を対象として参照箇所一覧を表示する
- [x] 参照箇所は `[kind] file:line preview` 形式で一覧表示される
- [x] ダブルクリックで該当ファイル・該当行にジャンプできる
- [x] カーソルが型宣言の外にある場合は「No type declaration under caret」プレースホルダーを表示する
- [x] カーソル位置の変更で 200ms debounce 後に自動更新される
- [x] 参照件数のサマリー（`Type.name: N reference(s)`）をステータスバーに表示する

### US-Impact-02: 高速な参照検索

**大きなプロジェクト（数百ファイル）でも快適に使いたい開発者として**、参照検索が瞬時に完了することを期待する。

**受け入れ条件（Phase 1 実装スコープ）:**

- [x] 参照検索は IntelliJ Platform の `PsiSearchHelper.processElementsWithWord`（word index）を活用し、テキスト全文検索を避ける
- [x] 200 件のソフトキャップで panel 描画を保護する
- [ ] 1000 ファイルプロジェクトで 500ms 以内に結果が返る — `IntelliJPlatformExtension` の light project は content root を持たず PsiSearchHelper の populated ケースを駆動できないため、Phase 2 で content-root 付き fixture を導入する案あり（pure helper のスループットは `RescriptInteropClassifierPerfTest` で間接的に確認）

**Phase 2 以降:**
- LSP `textDocument/references` フォールバック（精度向上のため）
- Stub Index 連携による参照側の indexing（現状は全ファイルスキャン）

### US-Impact-03: 影響種別の分類

**変更影響の重大度を見積もりたい開発者として**、参照を「読み取り（型注釈）」「コンストラクタ呼び出し」「パターンマッチ」「フィールドアクセス」のような種別で分類して見たい。

**受け入れ条件（Phase 1 実装スコープ）:**

- [x] 各参照行の左にバッジを表示する（`[type_ref]`、`[constructor]`、`[pattern]`、`[field_access]`、`[unknown]`）
- [x] 分類はトークンヒューリスティック（直前の非空白文字 + 先読み）で判別
- [x] 分類できない参照は `[unknown]` として表示し、漏れを避ける

## スコープ外

- 変更後の修正案の自動生成
- record/variant フィールド単位の影響表示（型全体への参照のみ扱う）
- リアルタイムシミュレーション
- 別プロジェクトへの参照追跡
- LSP 結合（Phase 2 以降）

## 受け入れ確認

- [x] 5 種類の型定義（基本 alias、record、variant、polymorphic variant、abstract type）に対して PSI Resolver が動作することを `RescriptTypeTargetResolverIntegrationTest`（20260508-006）で自動検証
- [x] 参照ジャンプが該当行を開く（Panel の double-click navigation 実装で対応）
- [x] LSP 未起動時、word-index ベースの検索で結果が返る
- [ ] 1000 ファイルプロジェクトで 500ms 以内 — content-root 付き fixture が必要なため Phase 2 で対応
- [x] ユニットテストで参照分類ヒューリスティック（8 ケース）と lineAndPreview helper（5 ケース）の判定をスナップショット検証する

## 非機能要件

- ToolWindow 描画は既存パターン（`SimpleToolWindowPanel` + `JBList`）を踏襲する
- 参照検索はバックグラウンドスレッド（`ApplicationManager.runReadAction`）で実行する
- 大量参照（200 件超）はソフトキャップで切り捨て、ステータスバーに `(showing first 200)` を表示
