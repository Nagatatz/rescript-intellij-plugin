# Design — ValidationLibrary を全 16 テンプレートへ展開

## アーキテクチャ方針

既存のサーバー系 9 テンプレートと同じ仕組みを踏襲する。

1. **Kotlin 側**: 各テンプレート `*TemplateFiles.kt` の `generate(ctx)` 内で、`ctx.validationLibrary.variantKey()`（`"zod"` または `"sury"`）を用いて `TemplateResourceLoader.loadText("templates/<name>/variants/<key>/…")` から `Validation.res` を読む。
2. **リソース側**: `src/main/resources/templates/<name>/variants/zod/` と同 `/sury/` にライブラリ固有の `.res` ファイルを配置する。
3. **依存宣言**: 各テンプレートの `package.json` 生成部で `ctx.validationLibrary.npmPackage` と `TemplateVersions` 由来のバージョン文字列を `dependencies` に追加する。

既存の `ResXTemplateFiles` が簡潔な参考実装になる（`variants/<key>/src/Validation.res` を読むだけ）。

## 適用例（Kotlin 側の定型パターン）

```kotlin
fun generate(ctx: TemplateContext): Map<String, String> {
    val files = mutableMapOf<String, String>()
    // …既存ファイル…
    files["src/Validation.res"] =
        TemplateResourceLoader.loadText("templates/<name>/variants/${ctx.validationLibrary.variantKey()}/src/Validation.res")
    return files
}
```

## 各テンプレートの変更内容

### 1. Basic

- 追加ファイル: `src/Validation.res`, `config.sample.json`
- 役割: `Fs.readFile("config.json")` → `JSON.parse` → `Validation.decode` → 成功時に `greeting` を出力するフローを示す
- `package.json` dependencies: `zod` or `sury`
- 既存 `App.res` に `Validation.decode` 呼び出しを 3 行追加

### 2. Vite + React

- 追加ファイル: `src/SignUpForm.res`, `src/Validation.res`
- 役割: `email` + `name` の submit 時に validation → `App.res` から呼び出し
- `package.json` dependencies: `zod` or `sury`

### 3. Electron

- 追加ファイル: `src/IpcPayload.res`（renderer 用）, `main.cjs` 側に validate 関数
- 役割: IPC メッセージが `{ kind: "ping", payload: string }` 形に従うか検証
- ただし `main.cjs` は JS なので、そちらは軽い `typeof` チェック＋注釈で済ませ、ReScript 側でメインの validation を書く

### 4. React Native (Expo) / (CLI)

- 追加ファイル: `src/Validation.res`
- 役割: name/email のフォーム validation。既存 `App.res` のサンプル表示に `Validation.decode` を組み込む
- 両 RN テンプレートで同じ shape（name/email）を使うが、ファイルは独立して配置する

### 5. npm Library

- 追加ファイル: `src/Validation.res`
- 役割: ライブラリの public API 関数が引数を validate する（外部 JS 呼び出し元向けの防御）
- 既存 `Lib.res`（or 相当）に `Validation.decode` を組み込み、不正入力時は `Error` を返すパターン

### 6. CLI Tool

- 追加ファイル: `src/Validation.res`
- 役割: `init` コマンドの `--name` / `--dir` オプションを validation
- 既存 `Commands.res` の `init` 分岐で呼び出し

## Validation.res の構造

全テンプレートで共通する zod / sury の ReScript binding 例。

### variants/zod/src/Validation.res（Basic の例）

```rescript
@module("zod") external z: 'a = "z"

type config = { greeting: string, repeat: int }

let schema = %raw(`
  z.object({
    greeting: z.string().min(1),
    repeat: z.number().int().positive()
  })
`)

let decode = (value: JSON.t): result<config, string> =>
  try {
    Ok(schema["parse"](. value))
  } catch {
  | _ => Error("invalid config")
  }
```

### variants/sury/src/Validation.res（Basic の例）

```rescript
@module("sury") external string: unit => 'a = "string"
@module("sury") external int: unit => 'a = "int"
@module("sury") external object: 'fields => 'a = "object"

type config = { greeting: string, repeat: int }

let schema = %raw(`
  S.object(s => ({
    greeting: s.field("greeting", S.string),
    repeat: s.field("repeat", S.int)
  }))
`)

let decode = (value: JSON.t): result<config, string> =>
  switch S.parseOrThrow(schema, value) {
  | value => Ok(value)
  | exception _ => Error("invalid config")
  }
```

テンプレートごとの具体的な型は各ファイルで定義する（IPC payload / Form / argv など）。

## テスト戦略

1. **生成ファイル一覧の検証**: 既存 `RescriptProjectGeneratorTest` に各テンプレート × ValidationLibrary の組み合わせケースを追加
2. **`src/Validation.res` の存在確認**: 各テンプレートで zod/sury 選択時の Validation.res が正しい variant から読まれることを検証
3. **placeholder residue smoke test**: 既存の全テンプレート横断テストに zod/sury 両 variant を追加（BUN 対応と同じ方針）

## ロールアウト順序（コミット粒度）

依存関係が最小のものから順に:

1. CLI Tool （生成物が最小、テンプレ構造もシンプル）
2. npm Library
3. Basic
4. Electron
5. React Native (Expo)
6. React Native (CLI)
7. Vite + React
8. CLAUDE.md 更新（「サーバー系 9 テンプレート」→「全 16 テンプレート」への書き換え）

各コミットで ktlint / build / test を通してから次へ進む。

## リスクと緩和

| リスク | 緩和策 |
|---|---|
| zod/sury のバージョンを各テンプレートで個別宣言すると version skew | `TemplateVersions.ZOD` / `TemplateVersions.SURY` を単一の真実の源として使用する（既存のサーバー系も同じ） |
| Validation.res で `%raw` に頼ると型安全性が薄い | 既存の hono-graphql / full-stack の書き方に揃える（サーバー系 9 で既に確立されたパターンを踏襲） |
| RN テンプレートで `zod` が Metro bundler の ESM 解決と衝突 | Metro resolver 設定に `.mjs` / `zod` が通ることを README に記載。既存テンプレートで既に通過しているため追加作業は不要のはず |
