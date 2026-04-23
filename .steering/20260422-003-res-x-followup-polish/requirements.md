# requirements.md — res-x テンプレート フォローアップ仕上げ

## 背景

`20260422-002-improve-res-x-template` で res-x テンプレートのコンパイル失敗と Bun 前提条件の抜けを潰したが、事後レビューで残った質の改善点があった。ユーザーがまとめた 8 項目のうち、優先度の高い以下を本ステアリングで一括対応する。

- (C)「Validation の飾り化」解消（10: zod, 11: sury）
- (D-1)「`bun build --compile` のプロダクション物語」(12)
- (D-2) Dockerfile + Deploy セクション (13)
- (D-3) SQLite 永続化の day-two ドキュメント (14, ドキュメントのみ)
- (E-3) テストの拡充 (17, プレースホルダ残留検出)

`#15`（rescriptJson builder 拡張）は YAGNI で見送り、`#16`（`PackageManager.BUN` の追加）はブラスト半径が大きいため**別ステアリング**で独立 PR として扱う。

## スコープ

### 対象

- **#17**: `TemplateResourcesSmokeTest` に「全テンプレート × 全生成ファイルで `{{` が残っていないこと」を検証するアサーションを追加する
- **#12**: res-x `package.json` の scripts に `compile` を追加（`bun build --compile src/App.res.mjs --outfile dist/app`）。README のスクリプト表に 1 行増やす
- **#10**: res-x の zod variant `Validation.res` を `z.string().trim().min(1, "...").max(80, "...")` + `safeParse` 姿に書き換え、手書き if/else を撤去。エラーメッセージは英語カスタム（既存 UX を維持）
- **#11**: 同じく sury variant を `S.string->S.trim->S.min(1, ~message="...")->S.max(80, ~message="...")` に書き換え、`S.parseOrThrow` の結果をそのまま利用する。`Obj.magic` を介する JS オブジェクト中継も不要に
- **#13**: `res-x/Dockerfile` を `oven/bun:1` ベースで追加。`res-x/readme/deploy.md` を新規作成し README の extra セクションに挟む
- **#14**: `res-x/readme/persistence.md` を新規作成し、`Bun.SQLite` で todos 配列を永続化する day-two ロードマップをドキュメントとして提示。コード変更なし

### スコープ外

- `#15` `ProjectFileBuilders.rescriptJson` の `jsxModule`/`extraBscFlags` 引数追加（2 つ目の Bun 系テンプレが来るまで YAGNI）
- `#16` `PackageManager.BUN` の追加（Wizard UI / 全テンプレ / Corepack / CI / ドキュメントに波及、別ステアリング）
- 他テンプレート（Hono, Vite+React など）の Validation 見直し（Hono の zod variant も飾りだが本タスクの射程外）
- sphinx-docs 英語/日本語の機能解説の再編

## 受け入れ条件

- [ ] `TemplateResourcesSmokeTest` に `{{` 残留を全テンプレ × 全ファイルで 0 件確認する新テストが通る
- [ ] res-x の `package.json` に `"compile": "bun build --compile src/App.res.mjs --outfile dist/app"` が含まれ、README の scripts 表にも 1 行追加されている
- [ ] res-x zod variant の `Validation.res` で、`z.string().trim().min(...).max(...)` と `safeParse` を使い、長さチェックがスキーマ側に移動している。既存の "Name must not be empty" 等のエラー文言は維持する
- [ ] res-x sury variant の `Validation.res` で、`S.trim`/`S.min`/`S.max` を使い、長さチェックがスキーマ側に移動している。既存のエラー文言は維持する
- [ ] 両 variant とも、ReScript コンパイルが `./gradlew integrationTest` で通る
- [ ] res-x に `Dockerfile`（`oven/bun:1` ベース）と `readme/deploy.md` が追加され、README の extra セクションに Deploy が並ぶ
- [ ] res-x に `readme/persistence.md` が追加され、README の extra セクションに Persistence が並ぶ（コード変更なし）
- [ ] `./gradlew ktlintCheck clean buildPlugin test integrationTest verifyPluginStructure` すべて成功
- [ ] 既存の他 17 テンプレートの `generate()` 出力と既存テストに変化がない（res-x と共通ヘルパーにしか触らない）
- [ ] `tasklist.md` の全タスクが `[x]` になる
