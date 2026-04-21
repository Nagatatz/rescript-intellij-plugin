# 要求内容: Hono テンプレートへの CORS プレ配線

## 背景

Hono 系テンプレート (HONO / HONO_GRAPHQL / CLOUDFLARE_WORKERS / AWS_LAMBDA / GOOGLE_CLOUD_RUN / FULL_STACK / MONOREPO) の共通バインディング `Hono.res` (生成元: `ProjectFileBuilders.honoBindings()`) には CORS ミドルウェアが含まれていない。利用者は:

- `@module("hono/cors") external cors: ...` を自分で書く
- `app->Hono.use(cors({...}))` を Server.res に追加する

の 2 ステップを経ないとブラウザクライアント↔サーバー通信が成立せず、ブラウザコンソールに出る CORS エラーは原因が分かりにくいため、多くの初学者が詰まる。

特に **FULL_STACK / MONOREPO** はテンプレート内に Vite+React フロントエンドを同梱しているため、生成直後に `pnpm dev` すれば自動で CORS が成立する状態が強く求められる。

## 目的

1. `honoBindings()` に `cors` external を追加し、全 Hono テンプレートで `Hono.cors(...)` を追加インポート無しで呼べるようにする
2. FULL_STACK / MONOREPO の Server.res に dev 用 CORS (`http://localhost:5173` 許可) をプレ配線する
3. その他 5 つの Hono 系テンプレートの Server.res には `Hono.cors` 呼び出しのコメント例を置き、詰まったときに気付ける導線を用意する
4. FULL_STACK / MONOREPO の README に「プロダクションデプロイ前に origin を書き換えること」の注記を追加
5. 既存の `extending-bindings.md` の middleware レシピを更新 (cors は共通バインディングに昇格したため、汎用例として `hono/jwt` などに差し替え)

## 受け入れ条件

- [ ] `honoBindings()` が生成する `Hono.res` 文字列に `@module("hono/cors")` が含まれる
- [ ] 生成された FULL_STACK `src/server/Server.res` に `app->Hono.use(Hono.cors({"origin": "http://localhost:5173"` を含む行が存在する
- [ ] 生成された MONOREPO `packages/server/src/Server.res` に上記と同等の CORS 配線が存在する
- [ ] HONO / HONO_GRAPHQL / CLOUDFLARE_WORKERS / AWS_LAMBDA / GOOGLE_CLOUD_RUN の Server.res に `Hono.cors` を示すコメント例が存在する
- [ ] `extending-bindings.md` の middleware レシピが `hono/cors` 以外の例 (例: `hono/jwt`) に更新されている
- [ ] FULL_STACK / MONOREPO の README に本番 origin 差し替えの注記がある
- [ ] 既存の `ProjectTemplateTest` 全件 + 新規テストがすべて通る
- [ ] `./gradlew ktlintCheck clean buildPlugin test` が成功する

## スコープ外

- Cloudflare Workers / AWS Lambda の dev サーバー側にフロントエンド配線を追加すること (その種のテンプレートはバックエンド単独用途が主)
- CORS の設定値を PackageManager / Validation Library 選択肢に連動させること (常に固定の dev origin で良い)
- 本プラグインの言語機能や UI (CLAUDE.md / README.md / sphinx-docs) への変更
- `@rescript/react-router` の導入 (別タスクで検討)
