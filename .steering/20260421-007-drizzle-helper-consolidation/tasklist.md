# タスクリスト: drizzle-orm ヘルパーの共通化と拡充

## 実装タスク

- [x] ステアリングドキュメント作成 (requirements / design / tasklist)
- [x] `templates/common/db/Db.res` 作成 (正準 Db.res、全ヘルパー付き)
- [x] `HonoTemplateFiles.kt` の Db.res パスを `common/db/Db.res` に変更
- [x] `HonoGraphqlTemplateFiles.kt` の Db.res パスを変更
- [x] `FullStackTemplateFiles.kt` の Db.res パスを変更
- [x] `MonorepoTemplateFiles.kt` の Db.res パスを変更
- [x] 4 つの per-template Db.res ファイル削除 (`git rm`)
- [x] `hono-graphql/src/Resolvers.res` の userById / deleteUser を実装 (TODO/Placeholder 除去)

## ドキュメントタスク

- [x] `extending-bindings.md` の drizzle レシピを stock helper 利用例に書き換え
- [x] `extending-bindings.md` に "If you need fuller type safety" サブセクション追加 (pgtyped-rescript / rescript-edgedb)

## テストタスク

- [x] `drizzle Db res is shared across all four drizzle-backed templates` 追加
- [x] `shared Db res exposes the new drizzle helpers` 追加
- [x] `hono-graphql resolvers use the new helpers and carry no TODO placeholders` 追加

## 検証タスク

- [x] `./gradlew ktlintCheck` 成功
- [x] `./gradlew clean buildPlugin` 成功 (jar 内 `templates/common/db/Db.res` 3,050 bytes を確認)
- [x] `./gradlew test --tests ProjectTemplateTest --tests CommonFilesTest` 成功 (`tests=39/14`, `failures=0`)

## コミット・マージタスク

- [x] 変更ファイル + 削除ファイル + 新規ファイル + steering ディレクトリを個別ファイル指定でステージング
- [x] `♻️ Consolidate drizzle Db.res into a shared resource with richer helpers` で単一コミット
- [x] tasklist の全タスクを `[x]` に更新してコミットに含める
- [x] ユーザーに push 可否を確認 (前 2 コミット + 当コミット、積み上がっている既存 unpushed 分を含む)
- [x] 承認後 `origin/main` に push
