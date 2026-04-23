# Requirements — ValidationLibrary を全 16 テンプレートへ展開

## 背景

現状、Project Wizard は `ValidationLibrary`（zod / sury）を常に選択させるが、生成物に反映されるのは「サーバー系 9 テンプレート」（hono / hono-graphql / aws-lambda / cloudflare-workers / google-cloud-run / nextjs / full-stack / monorepo / res-x）のみ。

残る 7 テンプレート（Basic / Vite+React / Electron / React Native Expo / React Native CLI / npm Library / CLI Tool）では選択が黙って無視されるため、ユーザーは「選んだのに何も変わらない」と感じる UX バグとなっている。

## ゴール

全 16 テンプレートにおいて、`ValidationLibrary` 選択が生成物に**意味のある形で反映される**状態にする。

## 非ゴール

- ValidationLibrary の選択肢を増やす（valibot 等）
- サーバー系 9 テンプレートの既存 variants を変更する（必要最小限の調整にとどめる）
- TypeScript 側の strict 設定や `package.json` の `exports` フィールド等、別トピック（#2 以降）で扱う項目

## 受け入れ条件

- [ ] Basic / Vite+React / Electron / React Native Expo / React Native CLI / npm Library / CLI Tool の 7 テンプレートが `TemplateContext.validationLibrary` を参照し、zod / sury の選択に応じて生成物が変わる
- [ ] 各テンプレートに `variants/zod/` と `variants/sury/` を配置し、対応する `Validation.res`（または同等の入力検証サンプル）を提供する
- [ ] 各テンプレートの `package.json` に選択されたライブラリ（`zod` または `sury`）が `dependencies` として追加される
- [ ] 各テンプレートにその validation を実際に呼び出すサンプルコード（1 ファイル or 既存ファイル内）を含める
- [ ] 各 variant に対応する Kotlin 側テストが `RescriptProjectGeneratorTest` または各テンプレート専用テストに追加される
- [ ] `./gradlew ktlintCheck buildPlugin test` が成功する
- [ ] `CLAUDE.md` の「Project Wizard」記述が「サーバー系 9」から「全 16」に更新される

## 各テンプレートの validation 適用ポイント

| テンプレート | validation の対象 | 理由 |
|---|---|---|
| Basic | ローカル `config.json` の shape 検証 | CLI + fs I/O という既存デモと相性が良い |
| Vite + React | サインアップフォームの入力検証（email/length） | SPA での定番パターン |
| Electron | renderer ↔ main の IPC ペイロード検証 | セキュリティ境界として必須の実務パターン |
| React Native (Expo) | フォーム入力検証（name/email） | モバイルフォームの典型 |
| React Native (CLI) | フォーム入力検証（name/email） | 同上 |
| npm Library | `public` API 引数のランタイム型検証 | ライブラリが外部 JS 呼び出し元からの入力を守るパターン |
| CLI Tool | `init` サブコマンドオプションの検証 | CLI 引数は文字列しかこないため実務で典型 |

## 影響範囲

- `src/main/kotlin/com/rescript/plugin/wizard/templates/*.kt` （7 ファイル修正）
- `src/main/resources/templates/<name>/variants/{zod,sury}/` （7 × 2 = 14 ディレクトリ新設）
- テスト（`src/test/kotlin/com/rescript/plugin/wizard/RescriptProjectGeneratorTest.kt` 等）
- ドキュメント（`CLAUDE.md`, `README.md` は該当箇所のみ）

## コミット粒度

テンプレート 1 つにつき 1 コミット（7 コミット）＋ 仕上げの CLAUDE.md 更新コミット＝合計 8 コミットを予定。
