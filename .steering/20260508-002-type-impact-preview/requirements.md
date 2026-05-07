# Type Impact Preview — Requirements

## 背景

ReScript はノミナルな variant・record・polymorphic variant を多用するため、共有モジュールの型を変更すると往々にしてプロジェクト全体に波及する。たとえば `User.t` のフィールド追加・renaming や `Event.t` の variant 追加は、`switch` exhaustiveness、record literal、関数引数など多数の場所に影響する。

現状、開発者は **変更後にコンパイルしてみないと影響範囲が分からない**。LSP の `textDocument/references` を都度実行する手もあるが、複数の型を一度に把握できない。本機能は **カーソル下の型定義に対する影響箇所をリアルタイムで一覧表示する ToolWindow** を提供し、「壊れる呼び出し側」を可視化する。

「破壊的変更の予算」を見積もる手段として、Rename / Extract / Change Signature と並ぶ補助ツールに位置付ける。

## ユーザーストーリー

### US-Impact-01: カーソル下の型の参照箇所を一覧表示

**ReScript 開発者として**、エディタでカーソルを型宣言に置いた状態で、その型がプロジェクト内のどこから参照されているかを ToolWindow で確認したい。

**受け入れ条件:**

- [ ] `Tools > Show Type Impact` メニューから ToolWindow を開ける
- [ ] ToolWindow を開くと、カーソルを置いている `type` 宣言（`type t = ...`、`type t = | A | B`、`type t = { x: int }` 等）を対象として参照箇所一覧を表示する
- [ ] 参照箇所はファイルパス + 行番号 + プレビュー行で一覧表示される
- [ ] ダブルクリックで該当ファイル・該当行にジャンプできる
- [ ] カーソルが型宣言の外にある場合は「No type declaration under caret」プレースホルダーを表示する
- [ ] カーソル位置の変更で 200ms debounce 後に自動更新される
- [ ] 参照件数のサマリー（`References: N`）をステータスバーに表示する

### US-Impact-02: PSI Stub Index ベースの高速参照検索

**大きなプロジェクト（数百ファイル）でも快適に使いたい開発者として**、参照検索が瞬時に完了することを期待する。

**受け入れ条件:**

- [ ] 参照検索は IntelliJ Platform の Stub Index（既存 `RescriptStubIndex`）を活用し、テキスト検索を避ける
- [ ] Stub Index に該当する宣言が無い場合は LSP `textDocument/references` をフォールバックとして利用する
- [ ] 1000 ファイルプロジェクトでも 500ms 以内に結果が返る（手動検証指標）

### US-Impact-03: 影響種別の分類

**変更影響の重大度を見積もりたい開発者として**、参照を「読み取り（型注釈）」「コンストラクタ呼び出し」「パターンマッチ」「フィールドアクセス」のような種別で分類して見たい。

**受け入れ条件:**

- [ ] 各参照行の左にバッジを表示する（`type-ref`, `constructor`, `pattern`, `field-access` 等の単純なテキストラベル）
- [ ] 分類はヒューリスティック（PSI 周辺のトークンパターンで判別）。完全な意味解析は LSP に委譲する将来拡張で扱う
- [ ] 分類できない参照は `unknown` として表示する

## スコープ外

- 変更後の修正案の自動生成（次フェーズ）
- record/variant フィールド単位の影響表示（型全体への参照のみ扱う）
- リアルタイムシミュレーション（「このフィールドを削除したら何件壊れる」）
- 別プロジェクトへの参照追跡

## 受け入れ確認

- [ ] 5 種類の型定義（基本 alias、record、variant、polymorphic variant、abstract type）でツリーが描画される
- [ ] 参照ジャンプが該当行を開く
- [ ] LSP 未起動時、Stub Index ベースの検索で結果が返る
- [ ] LSP 起動時、Stub Index と LSP の結果がマージされる（重複は除去）
- [ ] ユニットテストで参照分類ヒューリスティックの判定をスナップショット検証する

## 非機能要件

- ToolWindow 描画は既存パターン（`SimpleToolWindowPanel` + `JTree` または `JBList`）を踏襲する
- 参照検索はバックグラウンドスレッド（`ApplicationManager.runReadAction` または `ProgressManager`）で実行する
- 大量参照（1000 件超）は最初の 200 件を表示し、残りは「N more references…」サマリーで省略する
