# タスクリスト: テンプレート test スクリプト追加

## Step 1: シンプル 6 テンプレート

- [x] `BasicTemplateFiles.kt` に test 追加 + test 更新
- [x] `ElectronTemplateFiles.kt` に test 追加 + test 更新
- [x] `CloudflareWorkersTemplateFiles.kt` に test 追加 + test 更新
- [x] `AwsLambdaTemplateFiles.kt` に test 追加 + test 更新
- [x] `GoogleCloudRunTemplateFiles.kt` に test 追加 + test 更新
- [x] `ReactNativeTemplateFiles.kt` に test 追加 + test 更新
- [x] ktlint + gradle test で確認
- [x] コミット: `✨ Add vitest smoke tests to 6 simpler templates`

## Step 2: Monorepo

- [x] root `package.json` に `"test"` スクリプト追加 (PM 別)
- [x] `packages/server` に vitest devDep + test スクリプト + スモークテスト
- [x] `packages/client` に vitest devDep + test スクリプト + スモークテスト
- [x] `MonorepoTemplateFilesTest.kt` 更新
- [x] コミット: `✨ Add vitest smoke tests to Monorepo server and client packages`

## Step 3: Full-Stack

- [x] `FullStackTemplateFiles.kt` に vitest devDep + test スクリプト + server/client スモークテスト
- [x] `FullStackTemplateFilesTest.kt` 更新
- [x] コミット: `✨ Add vitest smoke tests to Full-Stack template`

## Step 4: ドキュメント

- [x] `CLAUDE.md` wizard 記述で「Vitest 雛形 (該当テンプレ)」→「全テンプレート」に更新
- [x] `README.md` wizard 記述を同様に更新
- [x] `docs/product-requirements.md` の wizard エントリを同様に更新
- [x] コミット: `📝 Update wizard docs to reflect test coverage across all templates`

## Step 5: 検証 + マージ

- [x] `./gradlew ktlintCheck buildPlugin test` 成功
- [x] ユーザーにマージ可否確認
- [x] main にマージ + ブランチ削除 + セッション終了
