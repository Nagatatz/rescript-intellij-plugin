# 要求内容: テンプレート実用度向上 + ドキュメント拡充

## 背景

20260415-001 で各テンプレートの足回り (README/.gitignore/CI/PM 反映/Vite+/バージョン集約/動作検証) を整備したが、サンプルコード自体は最小限 (Hello World レベル) のままだった。ユーザーが「次の一歩」(例: Hono に POST API を追加) を試みた際に躓くケースが報告されている。

## 目的

各テンプレートを「Hello World の次の一歩」まで踏み込んだ構成にして、生成直後から実用的な開発体験を提供する。さらに新規 2 テンプレート (Hono GraphQL / Full-Stack) を追加し、ドキュメントを大幅拡充する。

## スコープ

### Phase 1: 既存 12 テンプレートの実用度向上

| テンプレ | 追加する具体例 |
|---------|--------------|
| **Basic** | コマンドライン引数 + ファイル読み書き (Node.js fs binding) |
| **Vite + React** | `useState` フォーム + `fetch` で API 呼び出し例 |
| **Next.js** | App Router の Server Action + Client Component の組み合わせ |
| **Electron** | IPC (renderer ↔ main 通信)、Preload script |
| **Hono** | POST + JSON body 受信、ルートパラメータ、ロガーミドルウェア、SQLite + Drizzle、OpenAPI + Scalar UI |
| **Cloudflare Workers** | POST + KV ストア、wrangler.jsonc に bindings 例 |
| **AWS Lambda** | POST + APIGateway イベント型、DynamoDB 例 (README) |
| **Cloud Run** | POST + 環境変数読み取り、Cloud SQL 接続例 (README) |
| **React Native** | `useState` + ボタン + `FlatList` |
| **npm Library** | 複数関数 (sync + async) + ユニットテスト充実 |
| **CLI Tool** | サブコマンド (`cli greet`/`cli init`) + フラグパース |
| **Monorepo** | client から server に POST、shared 型を request/response 両側で利用、SQLite + Drizzle を server に統合 |

### Phase 2: 新規テンプレート 2 種追加

#### Hono GraphQL
- **graphql-yoga** + GraphiQL (`/graphql` で対話的 IDE)
- **SQLite + Drizzle** で `users` テーブルへの CRUD リゾルバ
- `pnpm docs:graphql` で `docs/schema.md` を自動生成 (graphql-markdown)
- ReScript で type/query/mutation を定義
- カテゴリ: Backend

#### Full-Stack (Hono + Vite+React)
- **単一パッケージ** (workspace なし)
- `src/server/` (Hono + Drizzle + OpenAPI)
- `src/client/` (Vite+React)
- `src/shared/` (Request/Response 型を ReScript で定義)
- `pnpm dev` で server + client を同時起動 (concurrently)
- カテゴリ: Full Stack

### Phase 3: REST/GraphQL ドキュメント機能

#### REST (Hono)
- `@hono/zod-openapi` で OpenAPI 3.x 仕様自動生成
- **Scalar UI** を `/docs` でホスト
- `/openapi.json` で生 spec 提供

#### GraphQL (Hono GraphQL)
- yoga 標準の GraphiQL を `/graphql` で有効化
- `pnpm docs:graphql` で `docs/schema.md` 自動生成

### Phase 4: README 大幅拡充

各テンプレ README を 150〜250 行目安に拡充:
- プロジェクト概要 + アーキテクチャ図 (Mermaid)
- セットアップ手順詳細
- 主要ファイル解説
- 「次にやりたいこと」レシピ集 (例: DB マイグレーション追加、認証層、Docker デプロイ)
- トラブルシューティング
- 関連リンク

### Phase 5: sphinx-docs テンプレートセクション新設

新規:
- `sphinx-docs/user/templates/index.md` — テンプレートカタログ (14 種類の比較表)
- `sphinx-docs/user/templates/basic.md` 〜 `sphinx-docs/user/templates/full-stack.md` — 各テンプレ詳細ガイド (14 ファイル)

レシピ拡充:
- `sphinx-docs/user/recipes/add-hono-endpoint.md`
- `sphinx-docs/user/recipes/add-graphql-resolver.md`
- `sphinx-docs/user/recipes/setup-drizzle.md`
- `sphinx-docs/user/recipes/add-openapi-docs.md`

### データ転送方針 (Monorepo / Full-Stack 共通)

- `shared/Api.res` で Request/Response 型を ReScript で定義
- server: Hono ハンドラで shared 型を使用、Zod でランタイムバリデーション
- client: ReScript で薄い fetch wrapper (`Api.createUser(payload): promise<User>`)
- tRPC は採用しない (ReScript で旨味薄)

## 非スコープ

- 認証層 (lucia 等) の実装 — README で言及のみ
- Docker / Kubernetes デプロイ設定 — README で言及のみ
- E2E テスト (Playwright) — Vitest のサンプルは含むが E2E は範囲外
- 既存 Monorepo の workspace 構成変更 — packages の構成は維持

## 受け入れ条件

### 各テンプレ
- [ ] 「Hello World の次の一歩」となるサンプルコードが含まれる
- [ ] README が 150 行以上で、アーキテクチャ図・セットアップ・主要ファイル解説・レシピ集・トラブルシューティングを網羅
- [ ] 統合テスト (`./gradlew integrationTest`) でパス

### Hono REST
- [ ] `/docs` で Scalar UI が動作
- [ ] `/openapi.json` で OpenAPI 3.x 仕様が出力
- [ ] CRUD 全エンドポイント (POST/GET/PUT/DELETE) + ID パラメータ
- [ ] SQLite (libsql) ファイルが起動時に自動作成

### Hono GraphQL (新規)
- [ ] `/graphql` で GraphiQL が動作
- [ ] `pnpm docs:graphql` で `docs/schema.md` を生成
- [ ] users CRUD リゾルバが動作

### Full-Stack (新規)
- [ ] `pnpm dev` で server + client が同時起動
- [ ] client から server の `/api/users` への POST が動作
- [ ] shared 型が両側で参照されている

### sphinx-docs
- [ ] `templates/index.md` に 14 テンプレ比較表
- [ ] 各テンプレートに専用ページ
- [ ] 4 つの新規レシピが追加されている

### 共通
- [ ] `./gradlew ktlintCheck buildPlugin test checkKdoc` がすべて成功
- [ ] CLAUDE.md / README.md / docs/product-requirements.md が新規 2 テンプレートを反映
- [ ] 統合テストワークフローで全 14 テンプレがパス

## セキュリティ考慮事項

- Drizzle: SQL インジェクション対策はパラメータ化クエリで担保 (Drizzle が標準で対応)
- Zod スキーマで全 POST/PUT body をバリデーション
- README で本番デプロイ時の注意 (環境変数管理、CORS、レート制限) を明記
- libsql ファイルの権限設定 (Cloud Run / Lambda での読み書き権限)
