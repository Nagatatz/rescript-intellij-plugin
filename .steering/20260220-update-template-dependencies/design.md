# Design: Update Project Template Dependencies

## 変更方針

テンプレートファイル内の `package.json` 生成コードにおけるバージョン文字列を更新する。テンプレートのソースコード（ReScript、HTML、設定ファイル）は原則変更しない。

## バージョン更新一覧

| パッケージ | 変更前 | 変更後 |
|-----------|--------|--------|
| `react` | `^19.0.0` | `^19.0.4` |
| `react-dom` | `^19.0.0` | `^19.0.4` |
| `next` | `^15.0.0` | `^15.0.7` |
| `vite` | `^6.0.0` | `^7.0.0` |
| `@vitejs/plugin-react` | `^4.0.0` | `^5.0.0` |
| `electron` | `^33.0.0` | `^35.0.0` |
| `react-native` | `^0.76.0` | `^0.78.0` |
| `expo` | `^52.0.0` | `^53.0.0` |
| `esbuild` | `^0.24.0` | `^0.25.0` |
| `wrangler` | `^3.0.0` | `^4.0.0` |

## 変更ファイル一覧

### テンプレートファイル（バージョン文字列のみ変更）

| ファイル | 変更するパッケージ |
|---------|------------------|
| `ViteReactTemplateFiles.kt` | react, react-dom, vite, @vitejs/plugin-react |
| `NextjsTemplateFiles.kt` | react, react-dom, next |
| `ElectronTemplateFiles.kt` | react, react-dom, vite, @vitejs/plugin-react, electron |
| `ReactNativeTemplateFiles.kt` | react, react-native, expo |
| `CloudflareWorkersTemplateFiles.kt` | wrangler |
| `AwsLambdaTemplateFiles.kt` | esbuild |
| `MonorepoTemplateFiles.kt` | react, react-dom, vite, @vitejs/plugin-react |

### 変更不要なテンプレートファイル

| ファイル | 理由 |
|---------|------|
| `BasicTemplateFiles.kt` | rescript, @rescript/core のみ（更新不要） |
| `HonoTemplateFiles.kt` | hono, @hono/node-server のみ（更新不要） |
| `GoogleCloudRunTemplateFiles.kt` | hono, @hono/node-server のみ（更新不要） |
| `CliToolTemplateFiles.kt` | rescript, @rescript/core のみ（更新不要） |
| `NpmLibraryTemplateFiles.kt` | rescript, @rescript/core のみ（更新不要） |
| `ProjectFileBuilders.kt` | バージョン文字列を含まない（更新不要） |

## テンプレートコード互換性の確認

各メジャーバージョンアップについて、テンプレートが生成するコードとの互換性を確認する。

### Vite 6 → 7

- `defineConfig` API は変更なし
- `@vitejs/plugin-react` v5 は Vite 7 用だが API (`react()`) は同一
- テンプレートの `vite.config.mjs` は変更不要

### Electron 33 → 35

- `app`, `BrowserWindow`, `loadFile` は安定 API で変更なし
- `main.cjs` (CommonJS) 形式はそのまま動作
- テンプレートコードは変更不要

### React Native 0.76 → 0.78 / Expo 52 → 53

- `View`, `Text` コンポーネントは安定 API
- Expo `app.json` 形式は変更なし
- テンプレートコードは変更不要

### Wrangler 3 → 4

- `wrangler.jsonc` 形式は互換性あり
- `wrangler dev` / `wrangler deploy` コマンドは同一
- テンプレートコードは変更不要

### esbuild 0.24 → 0.25

- CLI インターフェースに破壊的変更なし
- バンドルコマンド書式は同一
- テンプレートコードは変更不要

## テスト設計

各テンプレートの `generate()` メソッドが返す `package.json` 内のバージョン文字列が正しいことを検証するユニットテストを作成する。

### テストクラス

`src/test/kotlin/com/rescript/plugin/wizard/templates/TemplateDependencyVersionsTest.kt`

### テスト方針

- 各テンプレートの `generate(projectName)` を呼び出し、`package.json` の内容を取得
- JSON 文字列から依存パッケージのバージョンを抽出し、期待値と比較
- セキュリティ上重要なパッケージ（react, next, vite, electron 等）のバージョンが最低安全版以上であることを検証

### テストケース

| テストメソッド | 検証内容 |
|--------------|---------|
| `viteReact has secure dependency versions` | react ≥19.0.4, vite ≥7.0.0, plugin-react ≥5.0.0 |
| `nextjs has secure dependency versions` | react ≥19.0.4, next ≥15.0.7 |
| `electron has secure dependency versions` | react ≥19.0.4, vite ≥7.0.0, electron ≥35.0.0 |
| `reactNative has secure dependency versions` | react-native ≥0.78.0, expo ≥53.0.0 |
| `cloudflareWorkers has secure dependency versions` | wrangler ≥4.0.0 |
| `awsLambda has secure dependency versions` | esbuild ≥0.25.0 |
| `monorepo has secure dependency versions` | react ≥19.0.4, vite ≥7.0.0 |
