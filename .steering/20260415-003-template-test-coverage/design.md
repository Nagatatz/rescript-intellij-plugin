# 設計: テンプレート test スクリプト追加

## 方針

- 既存の `HonoGraphqlTemplateFiles.serverTest()` のパターン (`import("../Server.res.mjs")).resolves.toBeDefined()`) を踏襲
- テストランナーは **Vitest** に統一 (既存 6 テンプレートと同じ)
- React Native は Expo 標準が Jest だが、ここでは最小スモークテストのみのため Vitest で動作する (サブプロセスでの `.res.mjs` import を vitest がそのまま実行できる)
- Cloudflare Workers / AWS Lambda は `@cloudflare/vitest-pool-workers` などの専用ランナーを使わず、**純粋な import 可否チェック**に留める (依存環境を増やさない)

## テンプレート別 test スクリプト設計

| テンプレート | テストファイル | test スクリプト |
|-------------|-------------|----------------|
| Basic | `src/__tests__/App.test.mjs` | `vitest run` |
| Electron | `src/__tests__/App.test.mjs` | `vitest run` |
| Cloudflare Workers | `src/__tests__/Worker.test.mjs` | `vitest run` |
| AWS Lambda | `src/__tests__/Handler.test.mjs` | `vitest run` |
| Google Cloud Run | `src/__tests__/Server.test.mjs` | `vitest run` |
| React Native | `src/__tests__/App.test.mjs` | `vitest run` |
| Monorepo (server/client) | `packages/{server,client}/src/__tests__/*.test.mjs` | ワークスペース経由 |
| Full-Stack | `src/{server,client}/__tests__/*.test.mjs` | `vitest run` |

### スモークテストの雛形

```javascript
import { describe, expect, it } from "vitest";

describe("<module> module", () => {
  it("loads without throwing", async () => {
    await expect(import("../<EntryModule>.res.mjs")).resolves.toBeDefined();
  });
});
```

### Monorepo の特別対応

- Root `package.json` に `"test": "<pm> --workspaces run test"` 相当を追加 (npm/yarn/pnpm で構文が異なる)
  - pnpm: `"test": "pnpm -r run test"`
  - npm: `"test": "npm --workspaces run test"`
  - yarn: `"yarn workspaces foreach -A run test"` (yarn 4)
- 各サブパッケージ (`packages/server`, `packages/client`) に個別に `"test": "vitest run"` + vitest devDep + スモークテストを配置

### Full-Stack の特別対応

- 単一 `package.json` に `"test": "vitest run"` を追加
- スモークテストは `src/server/__tests__/Server.test.mjs` と `src/client/__tests__/App.test.mjs` の 2 ファイル同梱

## テスト変更

対象 8 テンプレートの `*TemplateFilesTest.kt` に以下のアサートを追加:

```kotlin
@Test
fun `package json declares test script and vitest`() {
    val pkg = TemplateFiles.generate(ctx)["package.json"]!!
    assertTrue(pkg.contains("\"test\""))
    assertTrue(pkg.contains("\"vitest\""))
}

@Test
fun `ships a smoke test`() {
    val files = TemplateFiles.generate(ctx)
    // 各テンプレートのテストファイルパスを検証
}
```

## 影響範囲

- 変更ファイル: 8 テンプレート実装 + 8 テスト + Monorepo 専用の 2 サブパッケージテスト = 合計 ~18 ファイル
- 行数: 各テンプレート ~30 行追加 × 8 = ~250 行
- 破壊的変更なし
- 既存 integration test の再実行により生成→install→build が通ることを確認

## コミット粒度

1. Basic / Electron / Cloudflare Workers / AWS Lambda / Google Cloud Run / React Native — 1 コミットにまとめる (6 個の独立シンプル追加)
2. Monorepo — 独立コミット (サブパッケージ + root script が絡むため)
3. Full-Stack — 独立コミット
4. ドキュメント更新 (CLAUDE.md wizard 記述の test 言及) — 1 コミット
