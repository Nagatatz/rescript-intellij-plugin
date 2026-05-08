# タスクリスト: AWS Lambda テンプレート ローカル実行

## 実装

- [x] 新規リソース `src/main/resources/templates/aws-lambda/src/Local.res` を作成
- [x] 新規リソース `src/main/resources/templates/aws-lambda/readme/local.md` を作成
- [x] `AwsLambdaTemplateFiles.kt` を更新:
  - `dev` / `start` script 追加
  - `@hono/node-server` を devDependencies に追加
  - `src/HonoNodeServer.res` と `src/Local.res` を files map に追加
  - README scripts 列と extraSections に "Local development" を追加
- [x] `AwsLambdaTemplateFilesTest.kt` にテストを追加（dev script / devDep 配置 / Local.res 存在 / README セクション）

## 検証

- [x] `./gradlew ktlintCheck` が成功する
- [x] `./gradlew clean buildPlugin` が成功する
- [x] `./gradlew test --tests '*AwsLambdaTemplateFilesTest*'` が成功する
- [x] フル `./gradlew test` が成功する（`TemplateResourcesSmokeTest` の `knownPlaceholders` に `cmdDev` を追加して通過）

## ドキュメント同期

- [x] CLAUDE.md の AWS Lambda テンプレート言及箇所を確認（言及はサーバー系 10 テンプレートの一文のみで、ローカル実行追加は内部的変更のため更新不要）
- [x] `sphinx-docs/user/templates/aws-lambda.md` を更新（ディレクトリ図、Scripts テーブル、devDeps、`Try It Locally` を 2 ターミナルフローに刷新、Bundling Notes 補足）
- [x] sphinx の `.po` 訳語を同一コミットで更新（`make build-ja` 通過）

## コミット・マージ

- [x] 機能単位でコミット（テスト + 実装 + ドキュメントを 1 コミットにまとめてよい）
- [x] tasklist.md のすべての項目を `[x]` に更新する最終コミット
- [ ] AskUserQuestion でユーザーにマージ可否を確認
- [ ] 承認後、`main` にマージしてブランチ削除
