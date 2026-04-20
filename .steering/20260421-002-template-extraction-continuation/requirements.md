# Requirements — Wizard Template Resource Extraction (Continuation)

## 背景

`20260416-002-wizard-template-resource-extraction` で 5 テンプレート（full-stack, hono,
hono-graphql, monorepo, react-native-cli）の静的コンテンツを `src/main/resources/templates/`
配下へ抽出し、`TemplateResourceLoader` 経由でロードするように切り替えた。

残り 10 テンプレート（合計 2,236 行）は依然として `*TemplateFiles.kt` 内部の
`buildString { appendLine(...) }` で ReScript ソースと設定ファイルを逐行構築している。

| # | テンプレート | Kotlin 行数 |
|---|-------------|------------|
| 1 | AwsLambda | 179 |
| 2 | Basic | 186 |
| 3 | CliTool | 233 |
| 4 | CloudflareWorkers | 181 |
| 5 | Electron | 248 |
| 6 | GoogleCloudRun | 204 |
| 7 | Nextjs | 260 |
| 8 | NpmLibrary | 260 |
| 9 | ReactNative | 245 |
| 10 | ViteReact | 240 |

## 目的

- 10 テンプレートの ReScript / 設定ファイル静的コンテンツを
  `src/main/resources/templates/<template-name>/` へ抽出する。
- `*TemplateFiles.kt` は `generate()` で `TemplateResourceLoader.load()` を呼び出すだけ
  の薄いディスパッチャに変える。
- 各テンプレートの抽出前後で、`generate()` の戻り値（`Map<String, String>`）が
  byte-identical であることを snapshot diff で保証する。

## 非目的

- 新規テンプレート追加・削除・依存バージョン変更は行わない。
- `CommonFiles`, `TemplateContext`, `TemplateVersions`, `ProjectFileBuilders` の
  インターフェース変更は行わない。
- Wizard UI / `RescriptModuleBuilder` の変更はしない。
- 前回 steering と同様、`package.json` / `rescript.json` / README のような
  パッケージマネージャ依存の生成コンテンツ（`ctx.runCmd(...)` 等を呼ぶもの）は
  抽出対象外のままにする。抽出対象は「各テンプレート固有の .res / .mjs / .ts / .json
  などの静的ファイル」。

## スコープ

### 変更対象（テンプレートごと）

| パス | 変更内容 |
|------|----------|
| `src/main/resources/templates/<name>/**/*` | 静的コンテンツを新規追加 |
| `src/main/kotlin/com/rescript/plugin/wizard/templates/<Name>TemplateFiles.kt` | private helper を `TemplateResourceLoader.load(...)` 呼び出しに置換 |
| `src/test/kotlin/com/rescript/plugin/wizard/templates/TemplateResourcesSmokeTest.kt` | 必要に応じて許容プレースホルダを追加 |

### 変更しない

- 既存の `*TemplateFilesTest.kt`（無修正で pass すべき）
- `CommonFiles.kt`, `TemplateContext.kt`, `TemplateResourceLoader.kt`,
  `TemplateVersions.kt`, `ProjectFileBuilders.kt`
- 他のパッケージ

## 受け入れ条件

各テンプレートごとに以下を満たす（10 × AC）:

- [ ] **AC-T01** 抽出前後で `TemplateFilesTest` が無修正で pass する
- [ ] **AC-T02** `TemplateResourcesSmokeTest` が pass する
- [ ] **AC-T03** pre-snapshot と post-snapshot（3 PM = npm/pnpm/yarn）が
      byte-identical（`diff -r` で完全一致）
- [ ] **AC-T04** `<Name>TemplateFiles.kt` のサイズが抽出前より純減している
- [ ] **AC-T05** ktlint pass

全体として:

- [ ] **AC-01** `./gradlew ktlintCheck && ./gradlew clean buildPlugin && ./gradlew test`
      がすべて pass
- [ ] **AC-02** Kover minBound 85 を下回らない
- [ ] **AC-03** 10 テンプレート = 10 コミット（1 テンプレート = 1 コミット）。
      ドキュメント更新は別途 1 コミット（必要時のみ）
- [ ] **AC-04** CLAUDE.md / repository-structure.md は前回 steering で既に更新済み
      のため追加更新不要
- [ ] **AC-05** deprecated API の新規利用なし

## リスクと緩和策

| リスク | 緩和策 |
|--------|--------|
| Kotlin 内の動的な値（projectName 等）が抽出先で欠落 | `TemplateResourceLoader` のプレースホルダ機構で渡す。`TemplateResourcesSmokeTest` の許容リストに追加 |
| README / package.json 等「PM 依存で動的生成」のコンテンツを誤って抽出 | 対象は「各テンプレート固有の静的コンテンツ」に限定し、`CommonFiles.readme`, `ProjectFileBuilders.packageJson` 呼び出しはそのまま残す |
| snapshot diff 差異を見逃す | コミット前に 3 PM（NPM/PNPM/YARN）で pre/post snapshot を取り `diff -r` で確認。差異があればコミットしない |
| 既存 `*TemplateFilesTest.kt` が失敗 | 失敗したら原因調査。テストを修正するのではなく **リソース/コードを修正** して一致させる |
