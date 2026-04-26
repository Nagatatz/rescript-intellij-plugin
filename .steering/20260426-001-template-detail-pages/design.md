# 設計

## 配置

```
sphinx-docs/user/templates/
├── index.md            # 既存。一覧 + Which template should I choose? + 全 toctree
├── basic.md            # 新規 ×16
├── npm-library.md
├── cli-tool.md
├── vite-react.md
├── nextjs.md
├── electron.md
├── react-native.md
├── react-native-cli.md
├── hono.md
├── hono-graphql.md
├── cloudflare-workers.md
├── aws-lambda.md
├── google-cloud-run.md
├── monorepo.md
├── full-stack.md
└── res-x.md
```

スラグ命名は `*TemplateFiles.kt` のクラス名から CamelCase → kebab-case で機械的に決める（例: `HonoGraphqlTemplateFiles` → `hono-graphql.md`）。

## 各ページのテンプレート構造

```markdown
---
myst:
  html_meta:
    "keywords": "<カンマ区切りキーワード>"
---

# <Template Display Name>

{bdg-info}`<category-tag>` {bdg-success}`<feature-tag>` …

<1〜2 段落の概要>

## What You Get

<ファイルツリー（実際の生成物。代表的なファイルだけで OK）>

## Wizard Options

| Option | Effect |
| --- | --- |
| Package manager | <effect> |
| Validation library | <effect on Validation.res> |

## Key Dependencies

| Package | Purpose | Version source |
| --- | --- | --- |

## Key Files

### `<file>` ／ `<file>`

<役割の解説>

## npm Scripts

| Script | Description |

## Day-Two Recipes

- <related recipe links>

## Notes

- <gotchas / OS / runtime requirements>
```

## advanced.md 修正方針

- L421: "15 pre-configured templates" → "16 pre-configured templates"
- L436-450 の表に 3 行追加:
  - `| Backend | **Hono GraphQL** | Hono server hosting graphql-yoga at /graphql with GraphiQL UI |`
  - `| Full Stack | **Full-Stack (single package)** | Single-package alternative to Monorepo with Hono backend + Vite+React client |`
  - `| Full Stack | **res-x (HTMX on Bun)** | Server-driven web app with rescript-x JSX + HTMX + Bun runtime |`
- L505: `(Hono, Cloudflare Workers, AWS Lambda, Google Cloud Run)` → `(Hono, Hono GraphQL, Cloudflare Workers, AWS Lambda, Google Cloud Run)`
- 表の末尾に「For per-template detail pages, see {doc}`../templates/index`.」のリンクを追加

## index.md（templates）への toctree 追加

```rst
```{toctree}
:hidden:
:maxdepth: 1

basic
npm-library
cli-tool
vite-react
nextjs
electron
react-native
react-native-cli
hono
hono-graphql
cloudflare-workers
aws-lambda
google-cloud-run
monorepo
full-stack
res-x
```

加えて、各 grid-item-card に `:link: <slug>` `:link-type: doc` を追加して詳細ページに飛べるようにする。

## ソースから抽出するデータ

各テンプレートで以下を `*TemplateFiles.kt` から取得:
- `generate(ctx)` の `linkedMapOf` キー一覧（生成ファイル）
- `<X>Dependencies()` 関数（依存パッケージ）
- `scripts = linkedMapOf(...)` （npm スクリプト）
- `extraSections` （README extraSections のセクション名 = ユーザー向けの章立てヒント）
- `CommonFiles.ciWorkflow(..., hasTest = ?, setupBun = ?)` のフラグ（CI 上の挙動）

## 翻訳同期手順

1. `cd sphinx-docs && make gettext` で `.pot` 再生成
2. `make update-po` で `.po` を `.pot` に同期（新規 `msgid` には空 `msgstr` が追加される）
3. 各 `.po` の空 `msgstr` を日本語で埋める
4. `make build-ja` で翻訳ビルドが通ることを確認

`make` ターゲットが無い場合や Python 環境セットアップが要る場合に備え、まず Makefile 確認 → uv sync → 実行の順で進める。
