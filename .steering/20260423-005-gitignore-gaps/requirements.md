# Requirements — テンプレート別 .gitignore ギャップ充実

## 背景

調査の結果、現在の各テンプレートの `.gitignore` ベースは網羅的だが、以下のフレームワーク特有のパターンが欠落している:

| テンプレート | 欠けているパターン | 理由 |
|---|---|---|
| Next.js | `.env*.local` | Next.js は `.env.local` / `.env.development.local` など複数の local overrides を使う |
| Cloudflare Workers | `.dev.vars` | Wrangler 4 の dev secrets ファイル |
| AWS Lambda | `.aws-sam/` | SAM CLI ビルド成果物 |
| React Native (CLI) | `*.apk`, `*.aab`, `*.ipa` | Android / iOS のローカルビルド成果物 |

これらはいずれも `node_modules/` と同等に「絶対に git にコミットされてはいけない」性質のもの。secrets リーク（`.env.local`, `.dev.vars`）や意図しないバイナリ含有（`*.apk` 等）を防ぐため、生成時点で gitignore しておく。

## ゴール

上記 4 テンプレートに対して、欠けていたフレームワーク固有パターンを `CommonFiles.gitignore(extra = ...)` の引数に追加する。

## 非ゴール

- ベース `.gitignore`（`CommonFiles.gitignore()`）への変更（ベースは現状で OK）
- Basic / CLI Tool / npm Library 等、既にシンプルなテンプレートへのパターン追加（過剰）
- `next-env.d.ts` の gitignore 扱い — Next.js 公式は commit 推奨に方針変更したため触らない

## 受け入れ条件

- [ ] Next.js テンプレートの `.gitignore` が `.env*.local` を含む
- [ ] Cloudflare Workers テンプレートの `.gitignore` が `.dev.vars` を含む
- [ ] AWS Lambda テンプレートの `.gitignore` が `.aws-sam/` を含む
- [ ] React Native (CLI) テンプレートの `.gitignore` が `*.apk` / `*.aab` / `*.ipa` を含む
- [ ] 各テンプレートテストで新パターン存在を assert
- [ ] `./gradlew ktlintCheck buildPlugin test koverVerify integrationTest` が成功する

## 影響範囲

- `src/main/kotlin/com/rescript/plugin/wizard/templates/{Nextjs,CloudflareWorkers,AwsLambda,ReactNativeCli}TemplateFiles.kt`（4 ファイル、それぞれ 1 箇所の `extra = listOf(...)` 更新）
- 各テンプレートのテスト（4 ファイル）

## コミット粒度

テンプレート 4 つにつき 1 コミットでまとめる（全部 `.gitignore` のパターン追加という単一のトピックなので、分けてもレビューに価値がない）。
