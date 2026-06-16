# requirements — Test Code Lens (#111) + open qualifier 展開 intention (#112)

`docs/product-requirements.md` の優先度 A 残候補 2 件を実装する。

## 対象機能

### #111 Test Code Lens（カテゴリ: その他 / 難易度: 中 / 優先度: A）

`describe` / `it` / `test` の各呼び出し行に Run / Debug の CodeVision を表示し、既存
`RescriptTestRunConfigurationType` に橋渡しする。

ロードマップ原文:
> Test Code Lens | その他 | describe / it / test 行に Run / Debug の CodeVision を表示し既存 RescriptTestRunConfigurationType に橋渡し | 中 | A

### #112 open qualifier 展開 intention（カテゴリ: Intention / 難易度: 中 / 優先度: A）

`open M` で取り込んだ識別子を修飾形 `M.foo` に書き戻し、`open` 文を削除する Alt+Enter
intention。

ロードマップ原文:
> open qualifier 展開 intention | Intention | open Belt を Belt.Array.map 形に書き戻す Alt+Enter (HLS importLens / rust-analyzer 由来) | 中 | A

## スコープ決定（前セッションでユーザー承認済み）

### #111

- 検出は純構文（`RescriptLexer` のトークン列）で行う。LSP 非依存。
- 検出対象は `describe` / `it` / `test` の LIDENT に `(` と文字列リテラル第 1 引数が続く呼び出し。
- テンプレート文字列（補間 `${...}` を含む可能性のあるバッククォート文字列）は v1 では名前抽出をスキップ（リテラル文字列のみ対象）。
- Run / Debug アクションは `ExecutorAction.getActions(0)` を CodeVision エントリに紐付け、
  既存 `RescriptTestConfigurationProducer` がカーソル offset の TestCall を解決して
  `-t <testName>` フィルタ付き構成を生成する。該当 TestCall がなければファイル単位構成にフォールバック。
- 実行対象ファイルは既存 `RescriptTestSourcesFilter` のパターン（`*_test.res` / `*.test.res` /
  `*_spec.res` / `*.spec.res` / `__tests__/`）に合致するもののみ CodeVision を表示する。

### #112

- **プロジェクト内モジュール限定・純構文（LSP 非依存）**。
- `open M` のうち、プロジェクト内に `M.res` / `M.resi` が存在するもののみ intention を表示する。
  `node_modules` 内のライブラリモジュール（Belt 等）は v1 のスコープ外（intention を出さない）。
- 展開名集合 E は、対象モジュール `M.res`（無ければ `M.resi`）のトップレベル宣言名
  （let / let rec / type / module / external / exception）を抽出して構成する。
- `open` 文より後ろのスコープで、E に含まれる裸の識別子の出現箇所に `M.` を前置し、`open` 文自体を削除する。
- 既に修飾済み（直前が `.`）の出現、E に含まれない名前、保守的にシャドウイングが疑われる名前は対象外。
- 確認ダイアログで書き換え件数を提示してから単一 `WriteCommandAction` で一括実行（Undo 1 ステップ）。

## 受け入れ条件

### #111

- [ ] テストファイル（`RescriptTestSourcesFilter` 合致）の `describe("...", ...)` /
      `it("...", ...)` / `test("...", ...)` 行に Run / Debug の CodeVision が表示される
- [ ] CodeVision の Run / Debug 実行で、その行のテスト名を `-t` フィルタに渡した実行構成が生成される
- [ ] テストでないファイル、または検出対象でない行には CodeVision を表示しない
- [ ] 補間テンプレート文字列を引数に持つ呼び出しは名前抽出をスキップする
- [ ] LSP 未接続でも動作する（純構文）

### #112

- [ ] プロジェクト内に `M.res` / `M.resi` が存在する `open M` にキャレットを置くと Alt+Enter で
      "Expand open qualifier" intention が表示される
- [ ] 実行すると `open M` 配下の `M` のメンバ参照が `M.member` に書き換わり、`open M` 行が削除される
- [ ] 既に `M.x` と修飾済みの箇所は二重修飾しない
- [ ] `M` のメンバでない裸識別子、シャドウイングが疑われる名前は書き換えない
- [ ] `node_modules` のライブラリモジュール（プロジェクト内に `.res` が無い）では intention を出さない
- [ ] 書き換え件数の確認ダイアログを経て単一 Undo で巻き戻せる
- [ ] LSP 未接続でも動作する（純構文）

## 完了時のロードマップ更新

- [ ] `docs/product-requirements.md` の「新機能候補」テーブルから #111 / #112 の行を削除する
