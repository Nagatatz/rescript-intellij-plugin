# LSP Code Action 動作検証 — 設計

## 検証対象

rescript-vscode `server/src/codeActions.ts` で実装されている 9 種の LSP code action:

| ID | トリガとなる診断 | 期待される編集 |
|---|---|---|
| `simpleAddMissingCases` | `You forgot to handle a possible case here, for example: (Foo\|Bar)` | switch ブロックの末尾に `\| Foo => failwith("TODO")` を挿入 |
| `wrapInSome` | option 型不整合（`This has type: int / Somewhere wanted: option<int>`） | 値を `Some(...)` で包む |
| `unwrapOptional` | option 型逆方向不整合 | `let x = optValue` → `let x = Belt.Option.getExn(optValue)` 等 |
| `addUndefinedRecordFieldsV10/V11` | `Record literal X is missing field Y` | `{a: 1}` → `{a: 1, b: failwith("TODO")}` |
| `simpleConversion` | `int / float / string` 型不整合 | `int_of_string(x)` 等の変換関数挿入 |
| `applyUncurried` | uncurried 関数の curried 呼び出し | `f(x)` → `f(.x)`（v10 系）/ ReScript v11+ では不要 |
| `didYouMean` | `The value X can't be found. Did you mean Y?` | `X` を `Y` に置換 |
| `removeUnusedCode` | reanalyze の "unused" 警告 | 該当宣言行を削除 |
| `extractLocalModuleToFile` | カーソルがローカルモジュール定義上 | `module M = { ... }` を新規 `M.res` に抽出 |
| `expandCatchAllPatterns` | カーソルが switch の `_ =>` ケース上 | `_` を全コンストラクタに展開 |

## 検証用サンプル `.res` ファイル

各 code action ごとに 1 ファイルずつ最小サンプルを用意する。配置先は `.steering/20260427-004-lsp-code-action-verification/samples/`（commit 対象、テスト fixture ではない）。

### `samples/01_missing_cases.res`

```rescript
type direction = North | South | East | West

let describe = (d: direction) =>
  switch d {
  | North => "up"
  // 期待: 残り3ケースの quickfix
  }
```

### `samples/02_wrap_in_some.res`

```rescript
let foo: option<int> => unit = _ => ()

let _ = foo(42)  // 期待: "Wrap in Some" quickfix で foo(Some(42))
```

### `samples/03_record_missing_fields.res`

```rescript
type user = {name: string, age: int, email: string}

let u: user = {name: "Ada"}  // 期待: age, email の挿入 quickfix
```

### `samples/04_simple_conversion.res`

```rescript
let toInt: int => unit = _ => ()

let _ = toInt("42")  // 期待: int_of_string ラップ quickfix
```

### `samples/05_did_you_mean.res`

```rescript
let myValue = 42
let _ = myValu  // 期待: myValu → myValue 置換 quickfix
```

### `samples/06_remove_unused.res`

```rescript
let unusedFunction = () => "never called"
// reanalyze 有効化時に "unused" 警告 → 削除 quickfix を期待
```

### `samples/07_extract_local_module.res`

```rescript
module Inner = {
  let value = 42
  let double = x => x * 2
}
// カーソルを Inner 上に → "Extract module to file" quickfix
```

### `samples/08_expand_catch_all.res`

```rescript
type color = Red | Green | Blue

let toString = (c: color) =>
  switch c {
  | Red => "red"
  | _ => "other"  // カーソルを _ 上に → 全ケース展開 quickfix
  }
```

### `samples/09_apply_uncurried.res`

ReScript 12（uncurried by default）では不要のため、検証時のバージョンを `findings.md` に明記。サンプルは v10 互換構文を使用する。

```rescript
@uncurry
let f = (. x) => x + 1

let _ = f(42)  // v10/11 で curried call を uncurried へ変換する quickfix
```

## 検証手順

1. **環境準備**
   - `cd <project-root>`
   - `node --version` / `npm ls @rescript/language-server` で LSP サーバーバージョンを記録
   - `./gradlew runIde` を起動

2. **プロジェクト準備**
   - サンドボックス IDE で新規 ReScript プロジェクトを作成（Wizard の Basic テンプレート）
   - `samples/*.res` を `src/` 直下にコピー
   - `npm install` を実行し `@rescript/language-server` を導入

3. **各サンプルの検証**
   - サンプルを開き、診断の波線が出ることを確認
   - 該当行にカーソルを置き、`Alt+Enter` で intention popup を開く
   - 表示されるアクション一覧をスクリーンショットまたは記述で記録
   - Quick Fix を実行し、編集結果を確認
   - 失敗時は `Help | Show Log in Finder` で `idea.log` の LSP セクションを確認し、エラー内容を記録

4. **結果記録**
   - `findings.md` の検証結果テーブルを埋める
   - 各エントリ: `Code Action 名 / 表示有無 / 適用結果 / LSP ログ抜粋 / 備考`

5. **原因分析（動かなかった場合）**
   - `RescriptLspServerDescriptor.lspCustomization` の code action 関連オプション（`com.intellij.platform.lsp.api.customization.LspCustomization` の Javadoc）を確認
   - 必要に応じて IntelliJ Platform の `LspCodeActionsSupport` 関連 API を調査
   - 動作しない理由を 1〜2 文で記録

## ドキュメント反映

検証完了後、以下を同一コミットで更新:

- `docs/lsp-fallback-matrix.md`: "LSP Code Actions（Quick Fix）" セクションに 9 種の動作可否表を追記
- `docs/archive/implemented-features.md`: 動作確認できた code action の一覧を追記（既存「Quick Fix (LSP Code Actions)」エントリを補強）
- `sphinx-docs/user/features/code-analysis.md`: 利用可能な Quick Fix の一覧をユーザー向けに記載
- `sphinx-docs/locale/ja/LC_MESSAGES/user/features/code-analysis.po`: 上記の日本語訳

## 検証で必要な API 確認ポイント

`RescriptLspServerDescriptor.kt` で以下が設定されているか確認:

```kotlin
override val lspCustomization = object : LspCustomization() {
    // codeActions 関連の override（あれば）
}
```

IntelliJ Platform の `LspCustomization` のサブクラス / プロパティ:

- `LspCodeActionsSupport`（あれば）
- `lspGoToDefinitionSupport` 等の sibling
- code action filter / classifier の有無

`com.intellij.platform.lsp.api.customization` パッケージのクラス一覧を `External Libraries` から確認すること。

## スコープ外（再掲）

- ネイティブ Quick Fix の実装（次の steering で扱う）
- LSP プロトコル自体の改修
- UI 比較（reasonml-idea-plugin / VSCode との見た目比較）
