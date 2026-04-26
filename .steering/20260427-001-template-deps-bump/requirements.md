# テンプレート依存バージョンの一括バンプ

## 背景

`TemplateVersions.kt` で集約管理しているプロジェクトテンプレートの依存バージョンを 2026-04-27 時点の npm レジストリ最新と突き合わせた結果、以下が判明した。

- 大半は最新だが、複数の patch / 小 minor 遅れがある
- メジャー更新が 3 件: `@hono/node-server` 1→2、`relay-compiler` 19→20、`bun` floor 1.2→1.3
- `rescript-relay` 4.x は内部で `react-relay` / `relay-runtime` 20.1.1 を peer dep として要求しており、`relay-compiler ^19.0.0` 固定は **既存の不整合**
- Node.js LTS は 22 (Jod) と 24 (Krypton) の 2 系統。ユーザー指示で 24 に引き上げる

## ゴール

1. プロジェクトテンプレートが生成する `package.json` / README / CI ワークフロー / `.nvmrc` の Node.js フロアを 24 に揃える
2. `@hono/node-server` を v2 系に更新する
3. `relay-compiler` を v20 系に更新し、合わせて `rescript-relay` の peer dep 整合を取る
4. `bun` packageManager のフロアを 1.3.13 に引き上げる
5. その他 patch / minor の遅れを 1 コミットでまとめてバンプする

## 受け入れ条件

- [ ] `TemplateVersions.NODE_ENGINE` = `">=24"`、`NODE_MAJOR` = `"24"`
- [ ] `ProjectTemplate.kt` の各テンプレート description にある "Node.js 22+" がすべて "Node.js 24+" に更新されている
- [ ] `sphinx-docs/user/templates/*.md` の "Node.js 22" / "Node.js 22+" / "nodejs22.x" / "Node 22 が必須" 等の言及がすべて 24 系に更新されている
- [ ] 対応する `sphinx-docs/locale/ja/LC_MESSAGES/user/templates/*.po` の `msgstr` も同期している
- [ ] `TemplateVersions.HONO_NODE_SERVER` = `"^2.0.0"`
- [ ] `TemplateVersions.RELAY_COMPILER` = `"^20.1.1"`
- [ ] `TemplateVersions.RESCRIPT_RELAY` = `"^4.4.1"`
- [ ] `TemplateVersions.BUN` = `"1.3.13"`
- [ ] その他の patch/minor 対象が `TemplateVersions.kt` 内で最新値に更新されている
- [ ] `./gradlew ktlintCheck clean buildPlugin test` がすべて成功する
- [ ] tasklist.md の全タスクが `[x]` になっている

## 非ゴール

- IntelliJ Platform 自体のバージョンアップ (2025.3 → 2026.1) は別途扱う (verifier-cli 1.402 が 2026.1 split-jar を未サポート)
- Kotlin / IntelliJ Platform Gradle Plugin の minor バンプは別 PR
- Marketplace へのリリース (本作業は内部更新のみ。リリースは別セッションで `intellij-release-flow` 経由)

## 制約

- 既存の `TemplateDependencyVersionsTest` は `assertMinVersion` でフロアを検証するのみ。バンプはすべて単調増加なのでテスト変更は不要
- `TemplateVersionsTest` は形式 (semver-like) のみ検証。バンプ後も形式を維持すること
- ktlint / kover カバレッジラチェット (86%) を割らないこと
