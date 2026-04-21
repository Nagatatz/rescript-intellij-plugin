# 設計: res-x Project Template

## 全体構造

`HonoTemplateFiles.kt` が踏襲するパターンに厳密に従い、以下のレイヤで構成する:

1. **Kotlin 生成器** `ResXTemplateFiles.kt` — 動的合成（package.json・README 組立・ファイル名マップ）
2. **静的リソース** `src/main/resources/templates/res-x/` — `.res` サンプル・README セクション・vite.config.js
3. **variants** `src/main/resources/templates/res-x/variants/{zod,sury}/src/Validation.res` — Validation Library ごとの差分

## 生成ファイル一覧

### ReScript ソース
- `src/App.res` — Bun.serve + res-x Handler の組立 + ルーティング
- `src/Handler.res` — Handler 定義（context type、初期化）
- `src/Layout.res` — 共通 HTML シェル（HTMX script 読み込み、viewport meta 等）
- `src/Counter.res` — Counter コンポーネント + GET `/` + POST `/counter/increment` / `/counter/decrement`
- `src/TodoForm.res` — Todo フォームコンポーネント + POST `/todos`（Validation 検証付き）
- `src/ResX.res` — `rescript-x` の最小バインディング（Handler.make, hxPost, hxGet, renderToResponse）
- `src/BunServe.res` — Bun.serve の最小バインディング
- `src/Validation.res` — variants から動的選択（zod または sury で Todo の name/description を検証）

### 設定・テスト
- `rescript.json` — `ProjectFileBuilders.rescriptJson`
- `package.json` — `ProjectFileBuilders.packageJson`（scripts に bun コマンド、deps に rescript-x/rescript-bun）
- `vite.config.js` — res-x の Vite プラグイン登録
- `src/__tests__/App.test.mjs` — Vitest スモークテスト（ルート一覧が 200 を返すか等）

### 共通ファイル
- `README.md` — `CommonFiles.readme` + extraSections (Application / HTMX / Project Layout)
- `.nvmrc` / `LICENSE` / `.gitignore` / `.editorconfig` / `.github/dependabot.yml` / `.github/workflows/ci.yml`

## package.json 構造

```jsonc
{
  "name": "{projectName}",
  "private": true,
  "type": "module",
  "packageManager": "{pm}@{version}",  // ctx.packageManagerSpec()
  "engines": { "node": ">=22" },
  "scripts": {
    "start": "bun src/App.res.mjs",
    "dev": "bun --watch src/App.res.mjs",
    "build": "vite build",
    "test": "vitest run",
    "test:coverage": "vitest run --coverage",
    "res:build": "rescript",
    "res:clean": "rescript clean",
    "res:dev": "rescript -w"
  },
  "dependencies": {
    "rescript": "^12.2.0",
    "@rescript/core": "^1.6.1",
    "rescript-x": "^1.4.0",
    "rescript-bun": "^2.1.0",
    "zod": "^4.3.6"  // or "sury": "^10.0.0"
  },
  "devDependencies": {
    "vite": "^8.0.9",
    "vitest": "^4.1.4",
    "@vitest/coverage-v8": "^4.1.4"
  }
}
```

## TemplateVersions への追加

```kotlin
const val RESCRIPT_X = "^1.4.0"
const val RESCRIPT_BUN = "^2.1.0"
const val HTMX = "2.0.7"  // CDN URL に埋め込む HTMX のバージョン
```

## ResXTemplateFiles.kt 骨格

`HonoTemplateFiles.kt:26-154` のパターンに厳密に従う。動的に合成するのは:
- package.json（deps は validation library で分岐）
- README（extraSections と description が validation library で分岐）
- Validation.res（variants から読み込み）

それ以外は `TemplateResourceLoader.load("res-x/...")` で静的ファイルをそのままロード。

## res-x バインディングの方針

`rescript-x` の公開 API は流動的なため、テンプレート内で使うバインディングは最小限に限定する:

- `src/ResX.res`:
  - `type handler`
  - `@module("rescript-x") external make: 'config => handler`
  - `let hxPost: (handler, string, 'req => 'res) => unit`
  - `let hxGet: (handler, string, 'req => 'res) => unit`
  - `let renderToResponse: 'jsx => 'response`

- `src/BunServe.res`:
  - `@val external serve: {..} => unit = "Bun.serve"` 相当の最小バインディング

内部実装への依存を避けるため、複雑な API 再エクスポートは行わない。

## サンプルアプリの挙動

### Counter
- GET `/` → Layout でラップされた Counter コンポーネント + Todo フォームを返却
- POST `/counter/increment` → カウンタ値をインクリメントした span を返す（`hx-swap="outerHTML"`）
- POST `/counter/decrement` → 同様にデクリメント
- 状態は `ref` でインメモリ保持（シングルプロセス想定）

### TodoForm
- フォーム: name (string, required, 1-80 chars), description (string, optional, 0-240 chars)
- POST `/todos` → Validation.parseTodoInput を呼び出し
  - 成功: Todo を追加した Todo リストを返す
  - 失敗: エラーメッセージ付きのフォームを再描画（ステータス 400）
- Todo リストは Counter と同じく `ref` でインメモリ保持

## テスト設計

`HonoTemplateFilesTest.kt` を参考に以下を検証:

1. package.json に `rescript-x` / `rescript-bun` / `@rescript/core` が含まれる（バージョンは `TemplateVersions` と一致）
2. `src/App.res`, `Handler.res`, `Counter.res`, `TodoForm.res`, `Layout.res`, `Validation.res`, `ResX.res`, `BunServe.res` が生成される
3. scripts の start/dev が `bun` で始まる
4. `Layout.res` に `htmx.org` のスクリプトタグが含まれる
5. `Counter.res` / `TodoForm.res` に `hx-post` が含まれる
6. zod variant: deps に `zod`、Validation に `@module("zod")`、`sury` は含まれない
7. sury variant: deps に `sury`、Validation に `S.object`、`zod` は含まれない
8. README に "Application"、"HTMX"、"Project Layout" のセクションが含まれる
9. `.nvmrc` / LICENSE / dependabot / ci.yml / .editorconfig / .gitignore がすべて生成される
10. CI ワークフローが test ステップを含む

## ドキュメント更新

| ファイル | 内容 |
|---|---|
| `README.md` | "15 production-shaped templates" → "16"、Features セクションに res-x を追加 |
| `CLAUDE.md` | "サーバー系 8 テンプレート" を "9 テンプレート" に更新し res-x を列挙 |
| `docs/templates.md` | テーブルに行 16 を追加、Hono 系グループの注記を維持 |
| `sphinx-docs/user/templates/index.md` | Full Stack セクションに res-x カードを追加 |
| `sphinx-docs/locale/ja/LC_MESSAGES/user/templates/index.po` | 対応する `msgstr` を追加 |

## コミット分割方針

git-conventions に従い、以下の粒度に分割:

1. `🔧 Add TemplateVersions constants for res-x` — TemplateVersions.kt のみ
2. `✨ Add res-x (HTMX on Bun) project template` — ResXTemplateFiles.kt + 静的リソース + ProjectTemplate.kt enum + テスト
3. `📝 Document res-x template` — README / CLAUDE / docs/templates / sphinx-docs + .po 同期
4. `📝 Update tasklist for res-x template` — tasklist.md を `[x]` に更新

## 既知のリスク

- **rescript-x API 不安定**: v1 に達しているが小規模コミュニティ。バインディングは最小限に留め、壊れたら該当ファイルだけ差し替え可能に
- **rescript-bun peer dep**: `>=2.1.0` 範囲で指定。将来の破壊的変更は個別のメンテナンス PR で追う
- **Bun 未インストール環境**: 生成プロジェクト自体が Bun を要求するため、README に明示。IDE プラグインのテストは生成物がビルド可能かを見るだけで Bun 起動は要求しない
