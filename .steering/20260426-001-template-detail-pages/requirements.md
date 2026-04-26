# 要求

## 背景

`sphinx-docs/user/features/advanced.md` に Project Wizard の説明があるが、以下の不整合がある:

1. L421: "15 pre-configured templates" → 実際は **16 テンプレート**（Basic / npm Library / CLI Tool / Vite + React / Next.js / Electron / React Native (Expo) / React Native (Community CLI) / Hono / Hono GraphQL / Cloudflare Workers / AWS Lambda / Google Cloud Run / Monorepo / Full-Stack / res-x）
2. テンプレート一覧表（L436-450）が 13 行しか無く、**Hono GraphQL / Full-Stack / res-x** が抜けている
3. L505 "Hono-based templates" の括弧列挙に **Hono GraphQL** が抜けている

加えて、現在のドキュメントでは各テンプレートの個別解説ページが存在しない（`sphinx-docs/user/templates/index.md` にカード+表で全 16 テンプレートを一覧表示しているのみ）。

## 要求

### R-1: `advanced.md` の不整合解消

- "15 pre-configured templates" → "16" に修正
- テンプレート一覧表に **Hono GraphQL / Full-Stack / res-x** の 3 行を追加
- L505 の Hono-based 列挙に Hono GraphQL を追加

### R-2: 個別テンプレートページ作成

`sphinx-docs/user/templates/` 配下に 16 テンプレート各 1 ページの Markdown を作成し、`templates/index.md` の toctree に登録する。各ページには以下を含めること:

- **Generated layout**: ファイルツリー（実際の生成物に即した構成）
- **Wizard options**: PackageManager / ValidationLibrary の選択がどこに効くか
- **Dependencies**: 主要な npm パッケージとバージョンソース（`TemplateVersions`）
- **Key files**: 重要な `.res` / 設定ファイル単位の役割
- **Scripts**: `package.json` の `scripts` 一覧と意味
- **Day-2 extension pointers**: そのテンプレート特有の拡張ポイント
- **Gotchas**: 既知の制約や運用上の注意（例: res-x は bun 必須、ViteReact は DISPLAY が必要、Electron は spawned IPC、…）
- **Recipe link**: 該当する `recipes/` ページへの相互リンク

### R-3: 日本語訳同期

- 修正・追加した英語 `.md` に対応する `sphinx-docs/locale/ja/LC_MESSAGES/**/*.po` を `make gettext && make update-po` で同期し、新規 msgstr を日本語で記述する
- `make build-ja` が成功すること

### R-4: ビルド検証

- `make build-all` が警告なしで成功すること（Sphinx の WARNING 含む）
- 既存リンク（`templates/index.md` のカード `:link:` 等）は壊さない

## 受け入れ条件

- [ ] `advanced.md` の 3 件の不整合が解消されている
- [ ] `sphinx-docs/user/templates/<slug>.md` が 16 ファイル新規作成され、`index.md` toctree から到達できる
- [ ] 各ページが上記 R-2 の構成を満たす
- [ ] `make build-ja` および `make build-all` が成功する
- [ ] 該当 `.po` の `msgstr` が日本語で埋まっている

## 非要件

- テンプレート生成ロジック（`*TemplateFiles.kt`）には触れない
- README.md / docs/templates.md は既に正しいので変更しない（advanced.md と sphinx-docs のみが対象）
