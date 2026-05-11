# 設計: Tauri プロジェクトテンプレート

## アーキテクチャ概観

```
my-tauri-app/
├── rescript.json                # JSX 有効、@rescript-tauri/core を bs-dep に追加
├── package.json                 # type: module、tauri-cli devDep、@rescript-tauri/core dep
├── index.html                   # Vite+ renderer エントリ
├── vite.config.mjs              # Vite+ defineConfig + React plugin
├── src/
│   ├── Main.res                 # React root を #root にマウント
│   ├── App.res                  # ボタン → Tauri.getInfoRaw → Validation.parseInfo
│   ├── Tauri.res                # @rescript-tauri/core 経由の IPC bindings
│   ├── Validation.res           # zod または sury 製の info パーサ
│   └── __tests__/App.test.mjs   # vitest スモークテスト
├── src-tauri/
│   ├── Cargo.toml               # tauri = "2"、serde、serde_json
│   ├── build.rs                 # tauri_build::build()
│   ├── tauri.conf.json          # devUrl / frontendDist / CSP / windows / beforeDev/Build
│   ├── .gitignore               # target/
│   └── src/main.rs              # greet / get_info コマンド + invoke_handler
├── README.md                    # IPC + Production Bundling セクション
├── LICENSE / .nvmrc / .editorconfig / .gitignore / .github/...
```

## 主要な設計判断

### フロントエンド: Vite+ + React + ReScript

ベースは Electron テンプレートと同じ構成を採用する。理由:

- Vite+ がベア specifier (`@rescript-tauri/core`) を解決するので Tauri の dev URL がそのまま機能する
- React は Tauri 採用層で最も一般的で、@rescript-tauri/core の typed IPC レイヤーを React の state hook と組み合わせた実例を示せる
- Electron テンプレートの `Validation.res` パターン (IPC 受信時のパース) をそのまま流用できる

vanilla DOM 版 (rescript-tauri/examples/hello-world と同じ) も検討したが、bare module specifier (`@rescript-tauri/core`) を browser で解決できず非現実的なので却下。

### Rust 側: 最小限 (greet + get_info)

`src-tauri/src/main.rs` に 2 つの `#[tauri::command]` を置く。

- `greet(name: String) -> String` — Hello World 相当。`Core.Raw.invoke` の動作確認用
- `get_info() -> AppInfo` — `{ name, version, platform, arch }` を返す struct。`Validation.parseInfo` で検証する payload

`AppInfo` は `serde::Serialize` の derive を使う。これにより JS 側で JSON.t として受信し、Validation.res で型付き record に落とせる。

### IPC bindings: `@rescript-tauri/core` 直接利用

`src/Tauri.res` は @rescript-tauri/core の `Core.Raw.invoke` を薄くラップする。typed `Command` レイヤーは将来移行先として README で言及。

```rescript
let greet = (name: string): promise<string> =>
  RescriptTauriCore.Core.Raw.invoke("greet", ~args={"name": name})

let getInfoRaw = (): promise<JSON.t> =>
  RescriptTauriCore.Core.Raw.invoke("get_info", ~args=())
```

Electron テンプレートの `getInfoRaw` 命名規則 (Validation を通っていないことを示す `Raw` 接尾辞) を踏襲する。

### Validation: Electron と同じパターン

`Validation.parseInfo: JSON.t => result<info, string>` を zod / sury 2 種類用意する。`info` レコードは Electron の `{ name, electronVersion, platform, arch }` から `{ name, version, platform, arch }` に変更 (Tauri は Electron version を持たない代わりに app version を返す)。

### tauri.conf.json

```jsonc
{
  "$schema": "https://schema.tauri.app/config/2",
  "productName": "{{projectName}}",
  "version": "0.0.0",
  "identifier": "com.example.{{projectName}}",
  "build": {
    "beforeDevCommand": "<pm> dev",     // wizard が PM を埋め込む
    "devUrl": "http://localhost:5173",
    "beforeBuildCommand": "<pm> build",
    "frontendDist": "../dist"
  },
  "app": {
    "windows": [
      { "title": "{{projectName}}", "width": 1024, "height": 768 }
    ],
    "security": {
      "csp": "default-src 'self'; img-src 'self' asset: https://asset.localhost; style-src 'self' 'unsafe-inline'; connect-src ipc: http://ipc.localhost"
    }
  },
  "bundle": {
    "active": true,
    "targets": "all"
  }
}
```

- `bundle.icon` は省略 (`tauri dev` では不要、`tauri build` 前に `tauri icon` で生成する旨を README で案内)
- `beforeDevCommand` / `beforeBuildCommand` は wizard が package manager に応じて `npm run dev` / `pnpm dev` / `yarn dev` / `bun run dev` を選択 (placeholder ではなく Kotlin 側で文字列を組み立てて書き出す)

### package.json scripts

| Script | 内容 |
| --- | --- |
| `dev` | `vp dev` (renderer dev server) |
| `build` | `vp build` (renderer prod bundle) |
| `tauri` | `tauri` passthrough |
| `tauri:dev` | `tauri dev` (Rust + renderer 一括起動) |
| `tauri:build` | `tauri build` (アプリ署名・バンドル) |
| `res:build` / `res:dev` / `res:clean` | rescript 各種 |
| `test` / `test:coverage` | `vp test` / `vp test --coverage` |

### CI

`CommonFiles.ciWorkflow(hasBuild = true, hasTest = true)` を流用。`vp build` (renderer のみ) と `vp test` が走る。Rust ビルドは `tauri:build` が必要だがランナー上のセットアップが重く CI 範囲外。README で local 検証手順を案内。

### sourceRoots

`listOf("src")`。`src-tauri/src/` は Rust なので ReScript ソースルートには含めない。

### supportsValidationSelection / supportsDatabaseSelection

- validation: `true` (Electron と同様、IPC レスポンス検証で使う)
- database: `false` (デスクトップアプリで永続化が必要なら Tauri 側のプラグインや SQLite を直接使うパターンが主流のため、wizard のオプションには出さない)

## ファイル構成

```
src/main/resources/templates/tauri/
├── index.html
├── vite.config.mjs
├── src/
│   ├── Main.res
│   ├── App.res
│   ├── Tauri.res
│   └── __tests__/App.test.mjs
├── src-tauri/
│   ├── Cargo.toml
│   ├── build.rs
│   ├── tauri.conf.json
│   ├── .gitignore
│   └── src/main.rs
├── variants/
│   ├── zod/src/Validation.res
│   └── sury/src/Validation.res
└── readme/
    ├── ipc.md
    └── production.md
```

`{{projectName}}` placeholder は既知のキーなので `TemplateResourcesSmokeTest` の `knownPlaceholders` 更新は不要。

## 既存コードへの影響

| ファイル | 変更内容 |
| --- | --- |
| `ProjectTemplate.kt` | enum エントリ追加 + `generateFiles` の when 分岐追加 |
| `ProjectTemplateTest.kt` | `21 entries` → `22 entries`、`React templates include jsx config` リストに TAURI 追加 |
| `RescriptModuleBuilder.kt` 等 | 変更不要 (enum 駆動) |
| `CLAUDE.md` | レイヤー 3 の Project Wizard 段落で "21 テンプレート" → "22 テンプレート"、追加 4 件のリストに TAURI が含まれない (TAURI は validation あり) ことを記述 |
| `README.md` | Templates 一覧に Tauri を追加 |
| `docs/repository-structure.md` | "21 種類" → "22 種類" |
| `docs/product-requirements.md` US-11 | 21 → 22、テンプレート名一覧に Tauri を追加 |
| `docs/templates.md` | 21 → 22、参照表に Tauri 追加 |
| `docs/functional-design.md` | 21 テンプレート → 22 テンプレート |
| `sphinx-docs/user/templates/index.md` | カード追加 (Desktop カテゴリ)、本文の "21" → "22" |
| `sphinx-docs/user/templates/tauri.md` | 新規ページ |
| `sphinx-docs/locale/ja/LC_MESSAGES/user/templates/*.po` | tauri.po 新規 + 既存 .po の数字更新 (`make update-po` で自動同期 → 手動翻訳) |

## テスト戦略

1. `TauriTemplateFilesTest` で
   - 主要ファイル (`rescript.json`, `package.json`, `index.html`, `vite.config.mjs`, `src/App.res`, `src/Tauri.res`, `src-tauri/Cargo.toml`, `src-tauri/src/main.rs`, `src-tauri/tauri.conf.json` 等) が生成される
   - package.json が `@rescript-tauri/core`, `@tauri-apps/api`, `@tauri-apps/cli` を持つ
   - rescript.json が `@rescript-tauri/core` を bs-dep に含む
   - zod / sury の Validation.res variant が切り替わる
   - PM に応じて `beforeDevCommand` が変わる (npm → `npm run dev`、pnpm → `pnpm dev`、yarn → `yarn dev`、bun → `bun run dev`)
   - `tauri.conf.json` の CSP が安全な値である
   - README に Tauri セキュリティ / 本番バンドル / アイコン生成セクションを含む
2. `ProjectTemplateTest` の 21→22 / React-jsx 一覧更新
3. `TemplateDependencyVersionsTest` に `Tauri has secure dependency versions` を追加 (rescript-tauri/core の minor 範囲、@tauri-apps/api ^2、@tauri-apps/cli ^2)

## バージョン pin

`TemplateVersions` に以下を追加:

```kotlin
const val RESCRIPT_TAURI_CORE = "^0.1.0"
const val TAURI_APPS_API = "^2.11.0"
const val TAURI_APPS_CLI = "^2.0.0"
```

## リスクと回避策

| リスク | 回避策 |
| --- | --- |
| @rescript-tauri/core が rapid breaking change される (0.x なので) | 範囲 `^0.1.0` は patch のみ追従。バージョン bump はリリース時に意識して行う |
| Tauri 2.x のコマンド API が変わる | TemplateVersions に集約済み。`@tauri-apps/api`, `@tauri-apps/cli` の最新を pin |
| ユーザーが `cargo` を持っていない | README 冒頭の Prerequisites で明示。`tauri dev` を実行するまでは Node 側のテンプレートだけで完結する |
| Validation.res の `version` フィールドが Electron 由来の `electronVersion` と混同される | sphinx-docs/templates/tauri.md で違いを明示 |
