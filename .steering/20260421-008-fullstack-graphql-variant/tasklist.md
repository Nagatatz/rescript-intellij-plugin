# タスクリスト: FULL_STACK GraphQL バリアント (Phase 1)

## 実装タスク (Kotlin)

- [x] ステアリング作成
- [x] `ApiStrategy.kt` 作成
- [x] `TemplateContext.kt` に `apiStrategy` 追加
- [x] `RescriptModuleBuilder.kt` に `apiStrategy` var 追加 + TemplateContext 構築時に渡す
- [x] `RescriptProjectWizardStep.kt` に API strategy ComboBox 追加
- [x] `TemplateVersions.kt` に RESCRIPT_RELAY / RELAY_COMPILER 定数追加
- [x] `ProjectFileBuilders.rescriptJson()` に `ppxFlags` 引数追加
- [x] `FullStackTemplateFiles.kt` を REST / GRAPHQL 分岐するようリファクタ
- [x] `HonoGraphqlTemplateFiles.kt` を共有 Yoga.res パスに切り替え

## 実装タスク (リソース)

- [x] `common/graphql/Yoga.res` 作成 (旧 hono-graphql/src/Yoga.res 内容をリフト)
- [x] `full-stack/api/graphql/src/server/schema.graphql` 作成
- [x] `full-stack/api/graphql/src/server/GraphqlSchema.res` 作成
- [x] `full-stack/api/graphql/src/server/Resolvers.res` 作成 (共有 Db ヘルパー利用)
- [x] `full-stack/api/graphql/src/server/Server.res` 作成 (yoga マウント版)
- [x] `full-stack/api/graphql/src/client/RelayEnvironment.res` 作成
- [x] `full-stack/api/graphql/src/client/UsersListQuery.res` 作成
- [x] `full-stack/api/graphql/src/client/App.res` 作成 (Relay 版)
- [x] `full-stack/api/graphql/src/client/ClientMain.res` 作成 (Suspense + Relay Provider)
- [x] `full-stack/api/graphql/relay.config.js` 作成
- [x] `full-stack/api/graphql/readme/architecture.md` 作成
- [x] `full-stack/api/graphql/readme/graphql.md` 作成
- [x] 旧 `hono-graphql/src/Yoga.res` 削除 (`git rm`)

## ドキュメントタスク

- [x] `common/readme/extending-bindings.md` の GraphQL パッケージ案内に `rescript-relay` 追記 (full-stack/readme/architecture.md は REST-only ガイドで GraphQL 分岐には読まれないため差分不要)

## テストタスク

- [x] `ApiStrategyTest.kt` 作成 (enum 基本検証、4 テスト)
- [x] `FullStackTemplateFilesTest.kt` に 14 テスト追加 (REST / GRAPHQL / 4-combo / Yoga.res 一致性など)

## 検証タスク

- [x] `./gradlew ktlintCheck` 成功 (auto-format で自動修正済)
- [x] `./gradlew clean buildPlugin` 成功 (jar 内 `templates/common/graphql/Yoga.res`、`templates/full-stack/api/graphql/**` 計 9 ファイル確認)
- [x] `./gradlew test` 成功 (ApiStrategyTest 4/4、FullStackTemplateFilesTest 27/27、ProjectTemplateTest 39/39、HonoGraphqlTemplateFilesTest 16/16、CommonFilesTest 14/14)

## コミット・マージタスク

- [x] 変更ファイル + 削除ファイル + 新規ファイル + steering を個別ファイル指定でステージング
- [x] `✨ Add FULL_STACK GraphQL variant (graphql-yoga + rescript-relay)` で単一コミット
- [x] tasklist 全更新をコミットに含める
- [x] ユーザーに push 可否を確認 (累計 unpushed 15+ コミット)
- [x] 承認後 `origin/main` に push
