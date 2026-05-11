# Tasklist — strict tsconfig の 4 テンプレート展開

## フェーズ 0: 準備

- [x] `main` から worktree `tsconfig-strict` を作成（`EnterWorktree` 経由、HEAD を main に reset --hard）
- [x] `TemplateVersions` に `REACT_DOM_TYPES` / `RN_TYPESCRIPT_CONFIG` を追加

## フェーズ 1: Next.js

- [x] `src/main/resources/templates/nextjs/tsconfig.json` 追加（strict + noUncheckedIndexedAccess + Next plugin + paths）
- [x] `src/main/resources/templates/nextjs/rescript-modules.d.ts` 追加（`*.res.mjs` ambient 宣言）
- [x] `NextjsTemplateFiles.kt` を更新して `tsconfig.json` と `.d.ts` を同梱
- [x] `package.json` devDependencies に `typescript` / `@types/react` / `@types/react-dom` / `@types/node` を追加
- [x] `NextjsTemplateFilesTest` に tsconfig / devDeps assertion 追加
- [x] ktlint / test 成功確認
- [x] コミット: `✨ Ship a strict tsconfig for the Next.js template`

## フェーズ 2: npm Library

- [x] `src/main/resources/templates/npm-library/tsconfig.json` 追加（strict + declaration / declarationMap）
- [x] `NpmLibraryTemplateFiles.kt` を更新
- [x] `NpmLibraryTemplateFilesTest` にアサーション追加
- [x] ktlint / test 成功確認
- [x] コミット: `✨ Ship a strict tsconfig for the npm Library template`

## フェーズ 3: React Native (Expo)

- [x] `src/main/resources/templates/react-native/tsconfig.json` 追加（`expo/tsconfig.base` extends）
- [x] `ReactNativeTemplateFiles.kt` を更新
- [x] `package.json` devDependencies に `typescript` / `@types/react` 追加
- [x] `ReactNativeTemplateFilesTest` にアサーション追加
- [x] ktlint / test 成功確認
- [x] コミット: `✨ Ship a strict tsconfig for the React Native (Expo) template`

## フェーズ 4: React Native (CLI)

- [x] `src/main/resources/templates/react-native-cli/tsconfig.json` 追加（`@react-native/typescript-config` extends）
- [x] `ReactNativeCliTemplateFiles.kt` を更新
- [x] `package.json` devDependencies に `typescript` / `@react-native/typescript-config` / `@types/react` を追加
- [x] `ReactNativeCliTemplateFilesTest` にアサーション追加
- [x] ktlint / test 成功確認
- [x] コミット: `✨ Ship a strict tsconfig for the React Native (CLI) template`

## フェーズ 5: 最終検証とマージ

- [x] `./gradlew clean buildPlugin ktlintCheck test koverVerify integrationTest` 成功確認（integration test で `@types/react-dom@^19.2.14` が npm 上に存在しないことを検出 → `^19.2.3` に修正する follow-up コミットを追加）
- [x] `tasklist.md` を `[x]` に更新してコミット
- [x] ユーザーに `main` マージ可否を `AskUserQuestion` で確認
- [x] 承認後、`main` へマージ → ブランチ削除 → worktree クリーンアップ

## テスト省略の判断

新規 `.kt` ファイルは追加しない。既存 `*TemplateFiles.kt` の修正 + 既存テストへのアサーション追加で十分にカバーされる。
