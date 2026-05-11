# タスクリスト: Hono テンプレートへの CORS プレ配線

## 実装タスク

- [x] ステアリングドキュメント作成 (requirements / design / tasklist)
- [x] `ProjectFileBuilders.honoBindings()` に `cors` external を追加
- [x] `full-stack/src/server/Server.res` にコメントアウト済み CORS ブロック (Vite proxy の補完) を追加
- [x] `monorepo/packages/server/src/Server.res` にコメントアウト済み CORS ブロックを追加
- [x] `hono/src/Server.res` に `Hono.cors` コメント例を追加
- [x] `hono-graphql/src/Server.res` にコメント例を追加
- [x] `cloudflare-workers/src/Server.res` にコメント例を追加
- [x] `aws-lambda/src/Server.res` にコメント例を追加
- [x] `google-cloud-run/src/Server.res` にコメント例を追加

## ドキュメントタスク

- [x] `extending-bindings.md` の middleware レシピを `hono/jwt` 例に差し替え
- [x] `full-stack/readme/architecture.md` に「コメントアウト済み CORS ブロック」の案内を追記
- [x] `monorepo/readme/networking.md` (新規) で CORS 事情を説明、Monorepo README に "Networking" セクションとして追加

## テストタスク

- [x] `shared Hono bindings expose the hono cors middleware factory` を追加
- [x] `FULL_STACK server ships a commented-out CORS block referencing Vite dev origin` を追加 (コメントアウト状態も明示検証)
- [x] `MONOREPO server ships a commented-out CORS block referencing Vite dev origin` を追加
- [x] `all Hono server templates surface the cors binding in their Server res` を追加 (7 テンプレート一括検証)

## 検証タスク

- [x] `./gradlew ktlintCheck` が成功する
- [x] `./gradlew clean buildPlugin` が成功する (jar 内に `templates/monorepo/readme/networking.md` と `templates/common/readme/extending-bindings.md` を確認)
- [x] `./gradlew test --tests ProjectTemplateTest --tests CommonFilesTest` が成功 (`tests=36/14`, `failures=0`)

## コミット・マージタスク

- [ ] 変更ファイル + steering ディレクトリを個別ファイル指定でステージング
- [ ] `✨ Expose hono/cors binding and document CORS across Hono templates` で単一コミット
- [ ] tasklist の全タスクを `[x]` に更新してコミットに含める
- [x] ユーザーに push 可否を確認 (前コミット `ad268fb` と合わせて 2 コミットを push)
- [x] 承認後 `origin/main` に push
