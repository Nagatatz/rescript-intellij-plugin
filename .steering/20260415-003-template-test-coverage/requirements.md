# 要求: 全テンプレートに test スクリプトを追加する

## 背景

ユーザーからの指摘: 「linter, formatter, test が存在するようにしてください」。

調査の結果:
- **Linter/Formatter**: ReScript は `rescript` (ビルド時 lint) と `rescript format` (フォーマッタ) を CLI として同梱している。ユーザーとの確認で、これらで十分とみなし、Prettier/ESLint/Oxlint 等の追加ツーリングは導入しない方針で合意。`package.json` に `lint`/`format` スクリプトも新規追加しない。
- **Test**: 14 テンプレート中 6 つ (CliTool, Hono, HonoGraphql, Nextjs, NpmLibrary, ViteReact) に `test` スクリプトが存在するが、残り 8 つには未設定。

## 対象テンプレート (test 未設定の 8 つ)

1. Basic
2. Electron
3. Cloudflare Workers
4. AWS Lambda
5. Google Cloud Run
6. React Native
7. Monorepo
8. Full-Stack

## 受け入れ条件

- [ ] 対象 8 テンプレートすべての `package.json` に `"test"` スクリプトが登録されている
- [ ] 対象 8 テンプレートすべてに対応するスモークテスト (`src/__tests__/*.test.mjs` 等) が同梱されている
- [ ] スモークテストは最小限: 「モジュールが例外なくロードできること」程度で可
- [ ] 各テンプレートの `devDependencies` に `vitest` が追加されている (React Native は Vitest 互換性に注意)
- [ ] 既存の 6 テンプレートの `test` スクリプトは変更しない
- [ ] `./gradlew ktlintCheck buildPlugin test` が成功する
- [ ] `TemplateIntegrationTest` が既存ワークフロー (nightly + manual) で通る
- [ ] 各テンプレートの既存テスト (`*TemplateFilesTest.kt`) で `"test"` スクリプトの存在がアサートされる

## 非対象

- Linter / Formatter のスクリプト追加 (ユーザー指示により除外)
- Prettier / ESLint / Oxlint / Biome 等の追加ツーリング
- 既存テンプレートのテスト内容の改善
