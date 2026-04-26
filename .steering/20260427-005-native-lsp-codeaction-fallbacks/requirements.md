# Native PSI Fallbacks for LSP Code Actions

## 背景

`.steering/20260427-004-lsp-code-action-verification/` の静的分析で、`@rescript/language-server` が以下の 9 種の quick fix を `textDocument/codeAction` で emit していることを確認した。本プラグインの IntelliJ Platform `LspCodeActionsSupport` デフォルト実装はこれらを Alt+Enter から自動受領できる前提条件を満たしている。

ただし、20260427-004 の `next-steps.md §2` では、以下 2 種について **LSP 経路で動作しない可能性が高い** と推測した:

| 候補 | LSP で動作しない懸念 |
|---|---|
| `applyUncurried` | ReScript v11+ uncurried-by-default で対応する診断が emit されないため、この quick fix は v10/v11 系の curried 関数定義が残るコードベースでしか発火しない。v12+ コードベースでは LSP は何もしない |
| `extractLocalModuleToFile` | LSP server が返す `WorkspaceEdit.documentChanges[].CreateFile` リソース操作を IntelliJ Platform 標準実装が完全には処理しない可能性。新規ファイル作成は実機で要確認のリスク領域 |

この 2 種を **ネイティブ PSI ベースの Intention Action として独立に実装** することで、LSP 経路の動作可否に依存せず、すべての ReScript ユーザーに同等の機能を提供する。

## 目的

- `applyUncurried`: 既存コードベースのレガシー `let f = (. x) => ...` 定義に対する curried 呼び出し箇所で `f(x)` → `f(. x)` への変換 Intention を提供する
- `extractLocalModuleToFile`: ファイル内のローカル `module M = { ... }` 宣言にカーソルがある状態で、本体を新規 `M.res` に切り出す Intention を提供する
- どちらも PSI / lexer 解析だけで完結し、LSP 接続なしでも動作する

## 受け入れ条件

### 共通

- [ ] 既存の `RescriptBaseIntention` 抽象クラスを継承する
- [ ] `intention/` パッケージに配置する
- [ ] `plugin.xml` に Extension Point として登録する（`intentionAction` カテゴリ）
- [ ] KDoc を英語で記述する
- [ ] テストクラスを `src/test/kotlin/com/rescript/plugin/intention/` に作成する

### A1: `RescriptApplyUncurriedIntention`

- [ ] カーソルが call expression `f(x, y, ...)` の `f` 識別子の上にあるとき発動
- [ ] `f` の定義（`let f = (. x) => ...`）が同一ファイル内 / プロジェクト内のスタブインデックスにある場合に「Convert call to uncurried form」を提案
- [ ] 適用すると `f(x, y, ...)` → `f(. x, y, ...)` に書き換える（先頭引数の前に `. ` を挿入）
- [ ] 既に `f(. x, ...)` の場合は提案しない
- [ ] テスト:
  - [ ] uncurried 定義に対する curried 呼び出しで Intention が表示される
  - [ ] curried 定義に対する呼び出しでは表示されない（false positive 防止）
  - [ ] 既に uncurried 形式の呼び出しでは表示されない
  - [ ] 適用後のテキスト変換が正しい

### A2: `RescriptExtractLocalModuleToFileIntention`

- [ ] カーソルが `module M = { ... }` 宣言の `M` 識別子上にあるとき発動
- [ ] プロジェクト内の `M.res` がまだ存在しない場合に「Extract module to file」を提案
- [ ] 適用すると以下を行う:
  - [ ] `module M = { ... }` の `{ ... }` 部分を新規 `<該当ディレクトリ>/M.res` に書き出す
  - [ ] 元のファイルから `module M = { ... }` 宣言を削除する
  - [ ] ファイル内に `M.x` 等の内部参照が存在する場合は、ユーザー向けに **「References to `M` in this file may need adjustment」** を `Notification` で表示する
- [ ] テスト:
  - [ ] 単純な `module M = { ... }` 宣言で Intention が表示される
  - [ ] トップレベル `module M: SIG = { ... }` （シグネチャ付き）でも動作する
  - [ ] `M.res` が既に存在する場合は表示されない
  - [ ] 適用後にファイルが新規作成され、元の宣言が削除される

## スコープ外

- LSP 経由の quick fix が実際に発火するかの runtime 検証（独立タスク `.steering/20260427-004-.../next-steps.md §1` で扱う）
- `module M: signature = { ... }` のシグネチャを `M.resi` に分離する処理（複雑度が高いため、本ステアリングでは扱わず将来の拡張とする）
- ファイル内参照の自動書き換え（PSI 範囲内の `M.x` を `open M; x` に変換等）。手動修正コメント表示で代替

## 成果物

- `requirements.md`（本ファイル）
- `design.md`: 実装戦略・PSI 検出ロジック・テキスト書き換え方法
- `tasklist.md`: 実行タスクリスト
- 実装コード:
  - `src/main/kotlin/com/rescript/plugin/intention/RescriptApplyUncurriedIntention.kt`
  - `src/main/kotlin/com/rescript/plugin/intention/RescriptExtractLocalModuleToFileIntention.kt`
- テスト:
  - `src/test/kotlin/com/rescript/plugin/intention/RescriptApplyUncurriedIntentionTest.kt`
  - `src/test/kotlin/com/rescript/plugin/intention/RescriptExtractLocalModuleToFileIntentionTest.kt`
- `plugin.xml` への Extension Point 登録
- ドキュメント反映:
  - `CLAUDE.md`「レイヤー 3: IDE 統合機能」（intention カテゴリ）
  - `README.md` Features セクション
  - `sphinx-docs/user/features/code-editing.md`（Intention Actions の例）+ `.po` 翻訳

## リスクと前提

- 軽量パーサーは `module M = { ... }` を `MODULE_DECLARATION` PSI として認識済み（`RescriptParser.kt` のトップレベル宣言処理）。`extractLocalModuleToFile` の検出はこの PSI で実装可能
- `applyUncurried` の `(. x) =>` 検出は PSI ボディ要素を持たないため、定義行のテキスト一致で行う（軽量パーサーの制約内で完結）
- `extractLocalModuleToFile` の新規ファイル作成は `WriteCommandAction` + `VirtualFile.createChildData()` で実装する
- 既存の Intention 18 個と同じ命名規則・パッケージ配置に揃える