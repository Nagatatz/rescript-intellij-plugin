# tasklist.md — res-x テンプレート フォローアップ仕上げ

## Phase 1: セットアップ

- [x] `.steering/20260422-003-res-x-followup-polish/` + requirements.md / design.md / tasklist.md 作成
- [x] `feature/res-x-followup-polish` ブランチ作成

## Phase 2: 項目ごとの実装（各 1 コミット）

- [x] #17: `TemplateResourcesSmokeTest` に `{{` 残留 0 件検証テスト追加 → `✅ Detect unresolved template placeholders in smoke test`
- [x] #12: ResX の `package.json` scripts に `compile` 追加 + README 反映 + テスト → `✨ Add bun build --compile script to res-x template`
- [x] #10: res-x zod variant を schema-driven に書き換え + テスト → `♻️ Move length rules into the res-x zod schema`
- [x] #11: res-x sury variant を schema-driven に書き換え + テスト → `♻️ Move length rules into the res-x sury schema`
- [x] #13: res-x に Dockerfile と readme/deploy.md 追加 + extra セクション追加 + テスト → `✨ Ship a Dockerfile and deploy guide for the res-x template`
- [x] #14: res-x に readme/persistence.md 追加 + extra セクション追加 + テスト → `📝 Outline Bun.SQLite persistence as a res-x day-two guide`

## Phase 3: コミット前検証（DoD Phase 3）

- [x] `./gradlew ktlintCheck` 成功
- [x] `./gradlew clean buildPlugin` 成功
- [x] `./gradlew test` 成功
- [x] `./gradlew integrationTest` 成功（16 テンプレート全パス）
- [x] `./gradlew verifyPluginStructure` 成功

## Phase 4: ステアリング同期

- [x] tasklist.md の全タスクを `[x]` に更新
- [x] `.steering/20260422-003-res-x-followup-polish/` の最終状態をコミット `📝 Add steering docs for res-x follow-up polish`

## Phase 5: マージ

- [x] `AskUserQuestion` でマージ可否確認
- [x] `main` にマージ、`feature/res-x-followup-polish` ブランチを削除
