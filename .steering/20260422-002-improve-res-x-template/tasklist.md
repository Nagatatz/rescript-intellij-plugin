# tasklist.md — res-x テンプレートの堅牢化

## Phase 1: セットアップ

- [x] `.steering/20260422-002-improve-res-x-template/` 作成 + requirements.md / design.md / tasklist.md 執筆
- [x] `feature/res-x-template-harden` ブランチを `main` から作成

## Phase 2: (ウ) integrationTest 統合

- [x] `.github/workflows/integration-tests.yml` に `oven-sh/setup-bun@v2` ステップを追加
- [x] ローカルで `./gradlew integrationTest --tests "*TemplateIntegrationTest*"` を実行し RES_X のエラーを確認

## Phase 3: (A) correctness バグ修正

- [x] `src/main/resources/templates/res-x/src/TodoForm.res`: 相互参照を ref で解消 + `renderForm` に `~error=?` 引数で統合 + `key` 属性削除 + `maybeString` を `getString` に置換 + `style` を `JsxDOMStyle.t` 化
- [x] `src/main/resources/templates/res-x/src/Layout.res`: `<main>{children}</main>` に修正
- [x] `src/main/resources/templates/res-x/variants/sury/src/Validation.res`: `%raw` を廃止してオブジェクトリテラル化
- [x] `src/main/resources/templates/res-x/vite.config.js`: `clientDirs: []` に変更
- [x] `src/main/resources/templates/res-x/rescript.json`: `bs-dependencies`/`bsc-flags` → `dependencies`/`compiler-flags`
- [x] `src/main/resources/templates/common/db/Db.res`: ReScript 12 の予約語対応のため `\"and"` エスケープ（drizzle 系 4 テンプレのリグレッション修正）
- [x] integrationTest が 16 テンプレート全て成功することを確認

## Phase 4: (B) Bun UX 改善

- [x] `CommonFiles.readme` に `extraPrerequisites: List<String> = emptyList()` 引数を追加
- [x] `CommonFiles.ciWorkflow` に `setupBun: Boolean = false` 引数を追加
- [x] `ResXTemplateFiles` の `readme()` 呼び出しに Bun Prerequisites を渡す
- [x] `ResXTemplateFiles` の `ciWorkflow()` 呼び出しに `setupBun = true` を渡す
- [x] `ResXTemplateFiles` の `package.json` の `dev` を `concurrently "rescript -w" "bun --watch run src/App.res.mjs"` に変更
- [x] `ResXTemplateFiles` の `devDependencies` に `concurrently` を追加

## Phase 5: テスト

- [x] `CommonFilesTest`: `extraPrerequisites` テスト 2 件、`setupBun` テスト 2 件
- [x] `ResXTemplateFilesTest`: README Bun prerequisite / CI setup-bun / dev concurrently / concurrently devDep / Layout children / TodoForm key 無し / TodoForm getString / sury 非 %raw / vite clientDirs empty / rescript.json 非 deprecated fields 合計 10 件の追加・更新
- [x] `ProjectTemplateTest`: drizzle `\"and"` エスケープへの整合更新

## Phase 6: コミット前検証（DoD Phase 3）

- [x] `./gradlew ktlintCheck` 成功
- [x] `./gradlew clean buildPlugin` 成功
- [x] `./gradlew test` 成功
- [x] `./gradlew integrationTest` 成功（16 テンプレート全パス）
- [x] `./gradlew verifyPluginStructure` 成功
- [x] 変更箇所 `.kt` ファイルに KDoc が付与されている
- [x] 変更箇所 `.kt` ファイルに対応する `*Test.kt` が存在する

## Phase 7: コミット

- [x] コミット 1: `🔧 Add Bun setup step to integration tests workflow`
- [x] コミット 2: `🐛 Escape drizzle \`and\` binding to satisfy ReScript 12`
- [x] コミット 3: `✨ Harden res-x template and surface its Bun prerequisite`
- [x] コミット 4: `📝 Add steering docs for res-x template hardening`

## Phase 8: マージ

- [x] `AskUserQuestion` でマージ可否をユーザー確認
- [x] `main` にマージ、`feature/res-x-template-harden` ブランチを削除
