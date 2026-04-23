# 要件: res-x Project Template の追加

## 背景

Project Wizard は現状 15 種類のテンプレートを提供しているが、Bun ランタイムや HTMX 系のサーバドリブン Web アプリ向けのテンプレートが存在しない。ユーザーからの要望に基づき、[`zth/res-x`](https://github.com/zth/res-x) を使った 16 番目のテンプレート "res-x (HTMX on Bun)" を追加する。

res-x は ReScript 12 + Bun + Vite を前提とし、JSX とコンポーネントモデルで HTML をサーバレンダリングし、クライアント側インタラクションは HTMX に委譲するフレームワーク。

## スコープ

- `ProjectTemplate` enum に `RES_X` エントリを追加（`FULL_STACK` カテゴリ）
- `ResXTemplateFiles.kt` 生成器を `HonoTemplateFiles.kt` のパターンに従って実装
- 静的リソース（`.res` サンプル・README セクション・vite.config.js）を `src/main/resources/templates/res-x/` に配置
- Validation Library 選択（zod/sury）を他のサーバ系テンプレートと同様に variants として対応
- ユニットテスト（`ResXTemplateFilesTest.kt`）を追加
- ドキュメント同期（README.md / CLAUDE.md / docs/templates.md / sphinx-docs/user/templates/index.md + 日本語 .po）

## スコープ外

- 統合テスト（nightly CI での実プロジェクト生成 + Bun 起動）は初期リリースでは扱わない。別 PR で後追い
- `PackageManager` enum への Bun 追加は行わない（install コマンド表記のみ既存 PM を使用）

## 受け入れ条件

- [ ] Wizard で "res-x (HTMX on Bun)" が FULL_STACK カテゴリに表示される
- [ ] zod / sury を選択した場合、それぞれ対応する `Validation.res` が生成される
- [ ] 生成された `package.json` の scripts が `bun` コマンドを使用する
- [ ] 生成された `Layout.res` に HTMX の script タグが含まれる
- [ ] 生成された `Counter.res` / `TodoForm.res` に `hx-post` が含まれる
- [ ] 依存関係に `rescript-x` / `rescript-bun` / 選択した validation lib が含まれる
- [ ] `./gradlew ktlintCheck clean buildPlugin test` がすべて成功
- [ ] README.md / CLAUDE.md / docs/templates.md / sphinx-docs の template count が 16 に更新され、res-x の説明が追加されている
- [ ] `sphinx-docs/locale/ja/LC_MESSAGES/**.po` が同期され `make build-ja` が通る

## 設計判断（ユーザー承認済み）

1. **ランタイム**: Bun 固定。`package.json` の scripts は `bun` を直接使用
2. **Validation Library**: zod/sury 両対応
3. **サンプルアプリ**: Counter（hx-post で増減）+ Todo フォーム（Validation 検証付き）
4. **カテゴリ**: `FULL_STACK`
