# 要求: テンプレート品質向上 (1〜7)

## 背景

ユーザーから "テンプレートをより改良する余地はありますか？" という問いに対して以下 7 項目を提案し、全てに合意を得た。

## スコープ

### 全 14 テンプレートに適用する共通追加

1. **`.env.example`** — DB 接続文字列や PORT 等の env 変数を使う 6 テンプレートに追加 (Hono REST / Hono GraphQL / Full-Stack / Monorepo server / Google Cloud Run / AWS Lambda)
2. **`.nvmrc`** — 全 14 テンプレ。`TemplateVersions.NODE_ENGINE` (現 `>=20`) に対応する major version (`20`) を記載
3. **`LICENSE` (MIT)** — 全 14 テンプレ。標準 MIT ライセンス、年は 2026、著者は project name
4. **`.github/dependabot.yml`** — 全 14 テンプレ。npm のみ、weekly schedule
7. **Vitest coverage** — 全 14 テンプレ。`"test:coverage": "vitest run --coverage"` + `@vitest/coverage-v8` devDep を追加。React Native は `vitest` 採用しているテンプレだけなので対象

### Hono 系 4 テンプレートに追加

5. **`app.onError` グローバルエラーハンドラ** — Hono REST / Hono GraphQL / Full-Stack / Monorepo server
6. **`app.request()` スタイルのテスト** — 上記 4 テンプレのスモークテストを実ルートテストに拡張 (例: `const res = await app.request('/users'); expect(res.status).toBe(200)`)

## 受け入れ条件

- [ ] 対象テンプレで生成ファイルが増加 (既存テストに assertion 追加)
- [ ] `CommonFiles` に `nvmrc()`, `license()`, `dependabotYaml()`, `envExample()` ヘルパ追加
- [ ] `TemplateVersions` に `VITEST_COVERAGE_V8` 定数追加、必要なら `NODE_MAJOR` も
- [ ] Hono 系 4 テンプレは `app.onError` でキャッチし JSON `{"error": ...}` を返す
- [ ] Hono 系 4 テンプレのテストは `app.request()` を使用し、status コードと body を検証
- [ ] `./gradlew ktlintCheck buildPlugin test` が成功する
- [ ] `TemplateIntegrationTest` が通る (既存 nightly で検証)
- [ ] CLAUDE.md / README.md / product-requirements.md / sphinx-docs (EN+JA) に反映

## 非対象

- 8. 非 root ユーザー / healthcheck (別スコープ)
- 9. Hono `hc` RPC (別スコープ)
- ESLint / Prettier / Oxlint 等の外部 linter/formatter (前ラウンドで除外済み)
