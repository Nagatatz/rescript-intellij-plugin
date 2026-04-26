# 設計

## アプローチ概要

`TemplateVersions.kt` を中心に、以下 5 コミットで段階的にバンプする。各コミットは独立してビルドが通る粒度。

| # | 目的 | 影響ファイル | 絵文字 |
|---|------|------------|------|
| 1 | Node 22 → 24 (LTS Krypton) | `TemplateVersions.kt`、`ProjectTemplate.kt`、`sphinx-docs/user/templates/*.md` (全 16 件)、対応 `.po` | 🔧 |
| 2 | `@hono/node-server` 1.x → 2.x | `TemplateVersions.kt` のみ | 🔧 |
| 3 | `relay-compiler` 19 → 20 + `rescript-relay` 4.4.1 | `TemplateVersions.kt` のみ | 🔧 |
| 4 | `bun` 1.2.0 → 1.3.13 (packageManager floor) | `TemplateVersions.kt` のみ | 🔧 |
| 5 | patch/minor 一括バンプ | `TemplateVersions.kt` のみ | 🔧 |

## 各バンプの根拠

### 1. Node 24 LTS

- Node.js 24.15.0 (Krypton) が現在 LTS
- ユーザー指示
- `TemplateContext.nodeMajor` / `nodeEngine` がコンテキスト経由で全テンプレートに伝播する設計のため、`TemplateVersions` の 2 定数のみで生成 `package.json` / `.nvmrc` / GitHub Actions の `setup-node@v4 node-version` / aws-lambda の `nodejs{{nodeMajor}}.x` runtime 文字列が一括追従する
- `ProjectTemplate.kt` の各 `description` にハードコードされた "Node.js 22+" 文言は手動更新
- 静的に書かれた `sphinx-docs/user/templates/*.md` も手動更新 (こちらは `{{nodeMajor}}` 置換を経由しない)

### 2. `@hono/node-server` v2

- v2.0.0 リリースノート: "**The public API stays the same** — the headline of this release is the large performance improvement"
- スループットは v1 比で最大 2.3x
- API 互換のためテンプレートコード変更不要、定数バンプのみ

### 3. `relay-compiler` v20 + `rescript-relay` 4.4.1

- `rescript-relay` 3.5.0 で内部の Relay compiler を 20.1.1 に移行済み
- 現行 `^4.1.0` の peer dep は `react-relay: 20.1.1`、`relay-runtime: 20.1.1`
- `relay-compiler ^19.0.0` は **既存の不整合** (npm install 時に警告は出るが運良くビルドが通っていた)
- 最新 `rescript-relay@4.4.1` も同じ peer dep 要件
- バンプは整合性修正としても価値がある

### 4. `bun` 1.3.13

- bun 1.3.13 は最新リリース
- 現行コメント: 「v1.2+ で `bun.lock`/`--filter` 導入のため最低バージョン」
- 1.3 系も両機能をサポートしており、フロア引き上げで失うものはない
- 生成された `package.json` の `packageManager: bun@x.y.z` は corepack 経由でこのバージョンを固定する。最新化することでユーザーが新規プロジェクトを作った際に古いバンドル bun を使わない

### 5. patch/minor バンドル

`TemplateVersions.kt` の以下定数を最新値に更新する。すべて API 互換のため API 影響なし:

| 定数 | 旧 | 新 |
|---|---|---|
| `VITE` | `^8.0.9` | `^8.0.10` |
| `VITE_PLUS` | `^0.1.18` | `^0.1.19` |
| `VITE_PLUS_CORE` | `^0.1.18` | `^0.1.19` |
| `VITEST` | `^4.1.4` | `^4.1.5` |
| `VITEST_COVERAGE_V8` | `^4.1.4` | `^4.1.5` |
| `HONO` | `^4.12.14` | `^4.12.15` |
| `ELECTRON` | `^41.2.1` | `^41.3.0` |
| `EXPO` | `^55.0.15` | `^55.0.17` |
| `REACT_NATIVE` | `^0.85.1` | `^0.85.2` |
| `RN_METRO_CONFIG` | `^0.85.1` | `^0.85.2` |
| `RN_BABEL_PRESET` | `^0.85.1` | `^0.85.2` |
| `RN_TYPESCRIPT_CONFIG` | `^0.85.1` | `^0.85.2` |
| `WRANGLER` | `^4.83.0` | `^4.85.0` |
| `CF_WORKERS_TYPES` | `^4.20260420.1` | `^4.20260426.1` |
| `LIBSQL_CLIENT` | `^0.17.2` | `^0.17.3` |
| `SCALAR_HONO_API_REFERENCE` | `^0.10.9` | `^0.10.10` |
| `PNPM` | `10.33.0` | `10.33.2` |
| `NPM` | `11.12.1` | `11.13.0` |
| `HTMX_CDN` | `2.0.7` | `2.0.10` |

## テスト戦略

- `TemplateVersions.kt` は単一の責務 (定数定義) で、ロジックを持たない
- 既存の `TemplateVersionsTest` (semver 形式チェック)、`TemplateDependencyVersionsTest` (`assertMinVersion`) が回帰検出を担保する
- バンプはすべて単調増加なので `assertMinVersion` 系のテストは緑のまま
- 新規テストは不要

## ドキュメント更新範囲

- `CLAUDE.md`: 機能追加ではないので更新不要
- `README.md`: 機能追加ではないので更新不要
- `sphinx-docs/user/templates/*.md` (16 件): "Node.js 22" → "Node.js 24" 系の文言を更新
- `sphinx-docs/locale/ja/LC_MESSAGES/user/templates/*.po`: 対応 `msgstr` を更新

## 実装順序とブランチ運用

- worktree 名: `template-deps-bump`
- 各コミットは独立かつビルド成功を保つ
- すべて完了後 `./gradlew ktlintCheck clean buildPlugin test` で最終検証
- `AskUserQuestion` でマージ承認 → main マージ
