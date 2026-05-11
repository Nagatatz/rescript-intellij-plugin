# Tasklist — ValidationLibrary を全 16 テンプレートへ展開

## フェーズ 0: 準備

- [ ] `main` から `feature/validation-library-all-templates` ブランチを切る（worktree で作業）
- [ ] `TemplateVersions` に `ZOD` / `SURY` が既に定義されていることを確認（なければ追加）

## フェーズ 1: CLI Tool テンプレート

- [x] `variants/zod/src/Validation.res` と `variants/sury/src/Validation.res` を追加（`init` オプション用）
- [x] `CliToolTemplateFiles.kt` を更新して Validation.res を同梱
- [x] `Commands.res` に `Validation.parseInitOptions` 呼び出しを追加
- [x] `package.json` dependencies に zod / sury を追加
- [x] テスト更新: `CliToolTemplateFilesTest` に zod/sury variant ケースを追加
- [x] `./gradlew ktlintCheck buildPlugin test` 成功確認
- [x] コミット: `✨ Wire ValidationLibrary into the CLI Tool template`

## フェーズ 2: npm Library テンプレート

- [x] `variants/zod/src/Validation.res` と `variants/sury/src/Validation.res` を追加（public API 引数検証）
- [x] `NpmLibraryTemplateFiles.kt` を更新
- [x] `Index.res` に `greetChecked` / `Validation.parseGreetInput` を組み込み
- [x] `package.json` dependencies 更新
- [x] テスト更新
- [x] ビルド成功確認
- [x] コミット: `✨ Wire ValidationLibrary into the npm Library template`

## フェーズ 3: Basic テンプレート

- [x] `variants/zod/src/Validation.res` と `variants/sury/src/Validation.res` を追加（config 検証）
- [x] `config.sample.json` を追加
- [x] `BasicTemplateFiles.kt` を更新
- [x] `App.res` に `--config` フラグと `Validation.parseConfig` を組み込み
- [x] `package.json` dependencies 更新
- [x] テスト更新
- [x] ビルド成功確認
- [x] コミット: `✨ Wire ValidationLibrary into the Basic template`

## フェーズ 4: Electron テンプレート

- [x] `variants/zod/src/Validation.res` と `variants/sury/src/Validation.res` を追加（IPC レスポンス検証）
- [x] `ElectronTemplateFiles.kt` を更新
- [x] renderer の `App.res` で `Validation.parseInfo` を呼び出し、エラーを UI に表示
- [x] `package.json` dependencies 更新
- [x] テスト更新
- [x] ビルド成功確認
- [x] コミット: `✨ Wire ValidationLibrary into the Electron template`

## フェーズ 5: React Native (Expo) テンプレート

- [x] `variants/zod/src/Validation.res` と `variants/sury/src/Validation.res` を追加（draft todo 検証）
- [x] `ReactNativeTemplateFiles.kt` を更新
- [x] `App.res` に `Validation.parseDraftTodo` + エラー表示を組み込み
- [x] `package.json` dependencies 更新
- [x] テスト更新
- [x] ビルド成功確認
- [x] コミット: `✨ Wire ValidationLibrary into the React Native (Expo) template`

## フェーズ 6: React Native CLI テンプレート

- [x] `variants/zod/src/Validation.res` と `variants/sury/src/Validation.res` を追加（draft todo 検証）
- [x] `ReactNativeCliTemplateFiles.kt` を更新
- [x] `App.res` に `Validation.parseDraftTodo` + エラー表示を組み込み
- [x] `package.json` dependencies 更新
- [x] テスト更新
- [x] ビルド成功確認
- [x] コミット: `✨ Wire ValidationLibrary into the React Native (Community CLI) template`

## フェーズ 7: Vite + React テンプレート

- [x] `variants/zod/src/Validation.res` と `variants/sury/src/Validation.res` を追加（greet フォーム検証）
- [x] `ViteReactTemplateFiles.kt` を更新
- [x] `App.res` に `Validation.parseGreetForm` を組み込み、エラーを UI に表示
- [x] `package.json` dependencies 更新
- [x] テスト更新
- [x] ビルド成功確認
- [x] コミット: `✨ Wire ValidationLibrary into the Vite+React template`

## フェーズ 8: ドキュメント反映

- [x] `CLAUDE.md` のテンプレート範囲記述を「全 16 テンプレート」へ更新
- [x] `docs/repository-structure.md` の「12 種類」→「16 種類」表記を修正
- [x] `sphinx-docs/user/features/advanced.md` の Validation Library 節をテンプレート別の検証対象表に刷新
- [x] `.po` を `make gettext` + `make update-po` で再生成し日本語翻訳を追記
- [x] `make build-ja` で日本語サイトがビルドできることを確認
- [x] `main` をマージして pre-existing な drizzle `and` 修正等を取り込み、コンフリクト（CLAUDE.md / TemplateResourcesSmokeTest.kt）を手動解決
- [x] コミット: `📝 Document ValidationLibrary coverage across all 16 templates` + merge commit

## フェーズ 9: マージ

- [x] 全タスクが `[x]` になっていることを確認
- [x] `./gradlew ktlintCheck test buildPlugin koverVerify` 実行（全 grean、カバレッジ閾値 OK、integration test もパス）
- [x] `tasklist.md` の最終更新コミット
- [x] ユーザーに `main` マージ可否を `AskUserQuestion` で確認
- [x] 承認後、`git checkout main && git merge worktree-validation-library-all-templates` → `git branch -d`
- [x] セッション終了（worktree 自動クリーンアップ）

## テスト省略判断

今回追加する新規 `.kt` ファイルはない（既存 `*TemplateFiles.kt` の修正のみ）ため、テスト免除対象は存在しない。ロジック変更は `RescriptProjectGeneratorTest` で横断的にカバーする。
