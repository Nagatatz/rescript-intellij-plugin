# Requirements — Wizard Template Resource Extraction

## 背景

`src/main/kotlin/com/rescript/plugin/wizard/templates/` にある 18 個のテンプレート生成オブジェクトのうち、上位 5 ファイルが 378〜542 行に肥大している。Explore 調査によれば、これらの ~70% は `buildString {}` 内の静的文字列 (README / tsconfig / `.gitignore` / サンプル `.res` / `drizzle.config.ts` 等) であり、Kotlin ソースとして管理される必然性が低い。

多行文字列リテラルは以下の問題を生む:
- diff レビューが重く、テンプレート内容の変更と Kotlin ロジックの変更が混在する
- 生成される実ファイルとの等価性 (インデント・改行・エスケープ) が視覚的に確認しにくい
- 新テンプレート追加時のコピー元になりにくい

## ゴール

上位 5 テンプレートの静的コンテンツを `src/main/resources/templates/<template>/` 配下の実ファイルとして分離し、テンプレート生成オブジェクトは「リソースロード + 軽量プレースホルダ置換 + 動的要素の Kotlin 合成」に役割を絞る。

## 対象ファイル (Top 5)

| ファイル | 現状行数 |
|---|---|
| `wizard/templates/MonorepoTemplateFiles.kt` | 542 |
| `wizard/templates/FullStackTemplateFiles.kt` | 478 |
| `wizard/templates/ReactNativeCliTemplateFiles.kt` | 404 |
| `wizard/templates/HonoGraphqlTemplateFiles.kt` | 386 |
| `wizard/templates/HonoTemplateFiles.kt` | 378 |

## 非ゴール (今回やらないこと)

- 残り 13 テンプレート (Basic, ViteReact, Next.js, Electron, ReactNative Expo, CloudflareWorkers, AWSLambda, GCR, NpmLibrary, CliTool — いずれも ≤ 300 行) のリソース化。`TemplateResourceLoader` 導入後、将来個別に対応可能。
- 既存テンプレートの中身の改修 (依存バージョン更新、機能追加など)。純粋な構造変更に徹する。
- 既存公開 API (`ProjectTemplate.generateFiles(ctx)`) のシグネチャ変更。

## 受け入れ条件

### AC-01: 機能等価性
- [ ] 5 テンプレートのいずれかを Wizard で選択し新規プロジェクトを作成したとき、生成されるファイル名・内容が refactor 前と完全一致する (バイト単位)。
- [ ] 既存テスト (`HonoTemplateFilesTest`, `HonoGraphqlTemplateFilesTest`, `ReactNativeCliTemplateFilesTest`, `FullStackTemplateFilesTest`, `MonorepoTemplateFilesTest`) が **一切の修正なし** で通る。

### AC-02: リソース抽出範囲
- [ ] 完全静的なファイル (README セクション定型, LICENSE, `.gitignore`, `.editorconfig`, `.nvmrc`, `tsconfig.json`, `drizzle.config.ts`, サンプル `.res` / `.ts`, GraphQL schema 雛形, 静的な `.github/dependabot.yml` 等) が `src/main/resources/templates/<template>/<relative-path>` に移動している。
- [ ] 動的要素を含むファイル (`package.json` の version 差込み・PM 条件分岐、`pnpm-workspace.yaml` の PM 条件、`.github/workflows/ci.yml` の `ctx.runCmd()` 依存スクリプト) は Kotlin に残っている。

### AC-03: ユーティリティの責務
- [ ] `TemplateResourceLoader.kt` が以下を提供する:
  - `load(path: String, vars: Map<String, String> = emptyMap()): String` — classpath 読み + `{{key}}` 置換
  - リソース未検出時に明確な例外メッセージを出す
  - 置換漏れ (値が渡されていない `{{key}}`) を検出する手段がある (`TemplateResourcesSmokeTest` で検証)

### AC-04: プレースホルダ構文
- [ ] 置換トークンは `{{key}}` (Mustache 風)。`${...}` は `.ts` / `.res` の template literal と衝突するため不採用。

### AC-05: 新規テスト
- [ ] `TemplateResourceLoaderTest.kt` — ロード成功 / ファイル不在 / 置換ロジック / 未使用プレースホルダ検出
- [ ] `TemplateResourcesSmokeTest.kt` — `src/main/resources/templates/` を再帰走査し、全ファイルが読める + ビルトイン既知トークン以外に `{{...}}` が残存しないこと

### AC-06: コード品質
- [ ] `./gradlew ktlintCheck` が通る
- [ ] `./gradlew clean buildPlugin` が通る
- [ ] `./gradlew test` が通る
- [ ] `./gradlew koverHtmlReport` でカバレッジが既存 `minBound(85)` を下回らない

### AC-07: コミット粒度
- [ ] `.claude/rules/git-conventions.md` に従い、最低でも以下の 7 コミットに分割される:
  1. `✨ Add TemplateResourceLoader utility`
  2. `✨ Add templates resource smoke test`
  3. `♻️ Extract static content from HonoTemplateFiles to resources`
  4. `♻️ Extract static content from HonoGraphqlTemplateFiles to resources`
  5. `♻️ Extract static content from ReactNativeCliTemplateFiles to resources`
  6. `♻️ Extract static content from FullStackTemplateFiles to resources`
  7. `♻️ Extract static content from MonorepoTemplateFiles to resources`

### AC-08: ドキュメント
- [ ] `CLAUDE.md` 「util/」項に `TemplateResourceLoader` の1行記述を追加する (既存ユーティリティの並びに倣う)。
- [ ] `docs/repository-structure.md` の `src/main/resources/` テーブルに `templates/` 行を追加する。
- [ ] その他 (README.md / product-requirements.md / sphinx-docs) は **更新不要** — 内部リファクタリングでユーザー可視機能の変更なし。

## 制約

- IntelliJ Platform 2025.3+、JDK 21+、Gradle Configuration Cache 有効の現行設定を維持する。
- 外部プロセス呼び出しやネットワーク通信は追加しない (純粋な classpath リソース読み込みのみ)。
- セキュリティ: リソースパスはコード内ハードコード限定で、外部入力で動的に組み立てない。

## リスクと緩和策

| リスク | 緩和策 |
|---|---|
| リソースファイルのインデント/改行の微妙な差異で等価性が崩れる | 既存テスト (`contains("...")` アサーション) に加え、主要テンプレートは refactor 前の `generate(ctx)` 出力を `.steering/` にスナップショット保存し diff で確認 |
| プレースホルダ `{{key}}` が `.ts`/`.res` の template literal と衝突 | `{{key}}` は JS/TS/ReScript で構文エラーになる形なので実運用上衝突しない。衝突が判明した場合は `@@key@@` にフォールバック |
| 置換忘れ (例: `{{projectName}}` を渡さずにロード) | `TemplateResourcesSmokeTest` と `TemplateResourceLoader` 内の検査で検出。`TemplateResourceLoader` のオプション引数 `strict = true` で残存 `{{...}}` 時に例外を投げる |
| classpath リソースが buildPlugin アーティファクトに正しく含まれない | `./gradlew buildPlugin` 後に `.zip` を展開し `templates/` 配下の存在を確認 |
