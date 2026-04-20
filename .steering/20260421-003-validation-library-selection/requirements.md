# Requirements — Wizard Validation Library Selection (zod / sury)

## 背景

現状、サーバー系テンプレート 8 個のうち `hono` / `hono-graphql` のみが zod を用いた HTTP 入力検証を実装している。他 6 個（`aws-lambda`, `cloudflare-workers`, `google-cloud-run`, `nextjs`, `full-stack`, `monorepo`）は型推論または手書き `typeof` チェックに留まり、実務投入時の堅牢性が不足する。

ReScript エコシステムには [sury](https://github.com/DZakh/sury)（旧 `rescript-struct`）という ReScript ネイティブのバリデーション/パーサライブラリがある。TS 生態系との相互運用を重視するなら zod、ReScript 側で型と検証を一元化したいなら sury が適する。両者を利用者が選べる状態にしたい。

## 目的

- Project Wizard に「Validation Library」選択 UI（zod / sury）を追加する。
- サーバー系 8 テンプレート全てで、選択されたライブラリに基づくランタイム検証（`Schema.res` モジュール + `package.json` 依存）を生成する。
- 既存 `PackageManager` 選択と同じパターン（enum + ComboBox + Builder field + TemplateContext + 各テンプレート分岐）で実装し、ロガー／フロント状態管理など将来の選択制オプションの基盤とする。

## 非目的

- ロギングライブラリ選択、フロントエンド状態管理選択、E2E テスト整備などは本スコープ外。
- 既存 `PackageManager` の挙動は変更しない。
- デフォルト `ZOD` 選択時は既存 `hono` / `hono-graphql` の生成物を byte-identical に保つ（その他 6 テンプレートはバリデーションが新規追加されるため、生成物の差分が発生する）。
- フロントエンド系テンプレート（vite-react, nextjs の UI, electron, react-native, react-native-cli）の UI コンポーネントは本プランでは変更しない（Next.js の Route Handler のみバリデーション対象）。

## スコープ

### 変更対象

| 区分 | パス |
|------|------|
| 新規 enum | `src/main/kotlin/com/rescript/plugin/wizard/ValidationLibrary.kt` |
| Wizard UI | `src/main/kotlin/com/rescript/plugin/wizard/RescriptProjectWizardStep.kt` |
| Builder | `src/main/kotlin/com/rescript/plugin/wizard/RescriptModuleBuilder.kt` |
| Context | `src/main/kotlin/com/rescript/plugin/wizard/templates/TemplateContext.kt` |
| Versions | `src/main/kotlin/com/rescript/plugin/wizard/templates/TemplateVersions.kt`（`SURY` 追加） |
| サーバーテンプレート | `HonoTemplateFiles`, `HonoGraphqlTemplateFiles`, `AwsLambdaTemplateFiles`, `CloudflareWorkersTemplateFiles`, `GoogleCloudRunTemplateFiles`, `NextjsTemplateFiles`, `FullStackTemplateFiles`, `MonorepoTemplateFiles` |
| リソース | `src/main/resources/templates/<name>/variants/{zod,sury}/src/Schema.res`（及び必要に応じ README 断片） |
| テスト | 上記に対応する `*Test.kt`、`TemplateResourcesSmokeTest`（新プレースホルダ許可）、`RescriptModuleBuilderTest`、`TemplateContextTest`、`ValidationLibraryTest` |

### 変更しない

- `PackageManager` / その他の既存 Wizard UI
- フロントエンドテンプレート（vite-react / react-native 等）の UI コード
- `TemplateResourceLoader` / `CommonFiles` / `ProjectFileBuilders` の公開 API

## 受け入れ条件

- [ ] **AC-01** Wizard に「Validation library:」ComboBox が表示される（zod / sury、既定 zod）。
- [ ] **AC-02** サーバー系 8 テンプレートで、選択されたライブラリに応じて `package.json` に `zod` または `sury` 依存が入る（他方は入らない）。
- [ ] **AC-03** サーバー系 8 テンプレートで、`src/Schema.res` が選択ライブラリの API で書かれる。呼び出し側（`Server.res` 等）は 1 本のまま機能する。
- [ ] **AC-04** `ctx.validationLibrary = ZOD` のとき、既存 `hono` / `hono-graphql` の生成物は byte-identical（snapshot diff ゼロ）。
- [ ] **AC-05** 両ライブラリ選択時に `./gradlew test` が pass。新規テストで zod / sury 両バリアントを検証。
- [ ] **AC-06** `TemplateResourcesSmokeTest` が新プレースホルダ込みで pass、`variants/` 配下が UTF-8 として読み込み可能。
- [ ] **AC-07** `./gradlew ktlintCheck && ./gradlew clean buildPlugin && ./gradlew koverVerify && ./gradlew verifyPluginStructure` が pass。
- [ ] **AC-08** CLAUDE.md / `docs/repository-structure.md` で Wizard の新オプションに 1 行言及する。
- [ ] **AC-09** deprecated API の新規利用ゼロ。

## リスクと緩和策

| リスク | 緩和策 |
|--------|--------|
| sury の API が pre-1.0 相当で将来変更 | `TemplateVersions.SURY` でピン止め。版更新時に 8 テンプレ横断チェック |
| zod/sury の公開 API を Schema.res で揃えにくい | `parseXxx: JSON.t => result<_, _>` に統一し呼び出し側は無変更 |
| テンプレート資源が zod × sury で倍増 | `variants/` 下に閉じ込め、Kotlin 側は 1 本を維持 |
| 既存 Hono の出力が崩れる | 既存 `src/Schema.res` を `variants/zod/src/Schema.res` へ移すだけにし、デフォルト ZOD で byte-identical を確認 |
