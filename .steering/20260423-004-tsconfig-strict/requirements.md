# Requirements — TypeScript strict tsconfig を TS 消費テンプレートに追加

## 背景

調査の結果、現在 16 個のテンプレートは **どれも `tsconfig.json` を生成していない**。実際には以下のテンプレートで `.ts` / `.tsx` を扱う:

| テンプレート | TypeScript 入り口 |
|---|---|
| Next.js | `app/page.tsx`, `app/greet/GreetForm.tsx`, `app/api/greet/route.ts` |
| npm Library | `src/Index.gen.d.ts`（genType 生成、consumer 向け） |
| React Native (Expo) | `App.tsx` |
| React Native (CLI) | `App.tsx` |

Next.js は `next dev` 実行時に自動で `tsconfig.json` を生成するが、strict オプションは既定で `false`。React Native のバンドル自体は Babel + Metro で通るが、IDE 側の型チェック / `tsc --noEmit` には `tsconfig.json` が必須。npm Library は `devDependencies` に `typescript` を含みつつ configuration が欠落している。

Hono / Monorepo / Full-Stack の `drizzle.config.ts` は node-by-tsx で実行される設定ファイルで、型チェック対象には含まれないため本スコープ外。Vite + React / Electron は `.tsx` を含まないため対象外。

## ゴール

4 つのテンプレート（Next.js / npm Library / React Native Expo / React Native CLI）に **strict 設定の `tsconfig.json`** を同梱し、TypeScript 側の型安全性を既定で高い状態から始められるようにする。

## 非ゴール

- Hono / Monorepo / Full-Stack の `drizzle.config.ts` を型チェック対象にする（別スコープ）
- Vite + React / Electron への `tsconfig.json` 追加（現状 `.tsx` を含まないため）
- `tsc` を実行する `type-check` スクリプトの追加（将来の改善トピック）

## 受け入れ条件

- [ ] Next.js テンプレートが Next.js 16 互換の `tsconfig.json` を生成する（`"strict": true`, App Router の `paths` エイリアス, Next プラグイン設定を含む）
- [ ] npm Library テンプレートが strict な `tsconfig.json` を生成する（consumer に適した型定義公開設定、`declaration: true` 等）
- [ ] React Native (Expo) テンプレートが Expo 55 向けの strict `tsconfig.json` を生成する（`expo/tsconfig.base` を extends）
- [ ] React Native (CLI) テンプレートが `@react-native/typescript-config` または同等の strict tsconfig を生成する
- [ ] 各テンプレートに対応する `TemplateFilesTest` が `tsconfig.json` の存在と主要設定（`"strict": true` 等）をアサートする
- [ ] `./gradlew ktlintCheck buildPlugin test koverVerify` が成功する
- [ ] 該当テンプレートの `npm-library` / `react-native` / `react-native-cli` に strict による既存コードへの型エラーがない（既存 `.tsx` を strict で通す）

## 影響範囲

- `src/main/resources/templates/{nextjs,npm-library,react-native,react-native-cli}/tsconfig.json` を新規作成（4 ファイル）
- `src/main/kotlin/com/rescript/plugin/wizard/templates/{Nextjs,NpmLibrary,ReactNative,ReactNativeCli}TemplateFiles.kt` を更新（`tsconfig.json` 同梱ロジック追加）
- 各テンプレートのテストに `tsconfig.json` アサーション追加
- 必要に応じて `@types/react` / `@types/react-native` 等の devDependency 追加

## コミット粒度

テンプレート 1 つにつき 1 コミット（4 コミット）を予定。

1. Next.js tsconfig
2. npm Library tsconfig
3. React Native (Expo) tsconfig
4. React Native (CLI) tsconfig
