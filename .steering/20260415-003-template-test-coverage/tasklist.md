# タスクリスト: テンプレート test スクリプト追加

## Step 1: シンプル 6 テンプレート

- [ ] `BasicTemplateFiles.kt` に test 追加 + test 更新
- [ ] `ElectronTemplateFiles.kt` に test 追加 + test 更新
- [ ] `CloudflareWorkersTemplateFiles.kt` に test 追加 + test 更新
- [ ] `AwsLambdaTemplateFiles.kt` に test 追加 + test 更新
- [ ] `GoogleCloudRunTemplateFiles.kt` に test 追加 + test 更新
- [ ] `ReactNativeTemplateFiles.kt` に test 追加 + test 更新
- [ ] ktlint + gradle test で確認
- [ ] コミット: `✨ Add vitest smoke tests to 6 simpler templates`

## Step 2: Monorepo

- [ ] root `package.json` に `"test"` スクリプト追加 (PM 別)
- [ ] `packages/server` に vitest devDep + test スクリプト + スモークテスト
- [ ] `packages/client` に vitest devDep + test スクリプト + スモークテスト
- [ ] `MonorepoTemplateFilesTest.kt` 更新
- [ ] コミット: `✨ Add vitest smoke tests to Monorepo server and client packages`

## Step 3: Full-Stack

- [ ] `FullStackTemplateFiles.kt` に vitest devDep + test スクリプト + server/client スモークテスト
- [ ] `FullStackTemplateFilesTest.kt` 更新
- [ ] コミット: `✨ Add vitest smoke tests to Full-Stack template`

## Step 4: ドキュメント

- [ ] `CLAUDE.md` wizard 記述で「Vitest 雛形 (該当テンプレ)」→「全テンプレート」に更新
- [ ] `README.md` wizard 記述を同様に更新
- [ ] `docs/product-requirements.md` の wizard エントリを同様に更新
- [ ] コミット: `📝 Update wizard docs to reflect test coverage across all templates`

## Step 5: 検証 + マージ

- [ ] `./gradlew ktlintCheck buildPlugin test` 成功
- [ ] ユーザーにマージ可否確認
- [ ] main にマージ + ブランチ削除 + セッション終了
