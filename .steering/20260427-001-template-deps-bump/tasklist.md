# タスクリスト

## Phase 1: ステアリング

- [x] requirements.md 作成 / 承認 (auto モードで自動承認扱い)
- [x] design.md 作成
- [x] tasklist.md 作成

## Phase 2: 実装 (worktree 内)

### コミット 1: Node 24 LTS

- [ ] worktree 作成 (`EnterWorktree name=template-deps-bump`)
- [ ] `TemplateVersions.NODE_ENGINE = ">=24"`、`NODE_MAJOR = "24"` に更新
- [ ] `ProjectTemplate.kt` の "Node.js 22+" を "Node.js 24+" に置換 (16 箇所程度)
- [ ] `sphinx-docs/user/templates/*.md` の Node 22 言及を 24 に更新
- [ ] 対応 `.po` の `msgstr` を更新
- [ ] `cd sphinx-docs && make build-ja` が通ることを確認
- [ ] `🔧 Bump templates to Node 24 LTS (Krypton)` でコミット

### コミット 2: @hono/node-server v2

- [ ] `TemplateVersions.HONO_NODE_SERVER = "^2.0.0"`
- [ ] `🔧 Bump @hono/node-server to v2.0.0` でコミット

### コミット 3: relay-compiler v20

- [ ] `TemplateVersions.RELAY_COMPILER = "^20.1.1"`
- [ ] `TemplateVersions.RESCRIPT_RELAY = "^4.4.1"` (peer dep 整合)
- [ ] `🔧 Align relay-compiler with rescript-relay 4.x peer deps` でコミット

### コミット 4: bun floor

- [ ] `TemplateVersions.BUN = "1.3.13"`
- [ ] `🔧 Bump bun packageManager floor to 1.3.13` でコミット

### コミット 5: patch/minor バンドル

- [ ] design.md の表に従い `TemplateVersions.kt` の各定数を更新
- [ ] `🔧 Bump template dependencies to latest patches` でコミット

## Phase 3: 検証

- [ ] `./gradlew ktlintCheck` 成功
- [ ] `./gradlew clean buildPlugin` 成功
- [ ] `./gradlew test` 成功
- [ ] kover カバレッジが 86% を割っていないこと (実測 = ラチェット用)

## Phase 4: マージ

- [ ] tasklist.md のすべてのタスクが `[x]` (本タスク含む)
- [ ] `AskUserQuestion` でマージ可否確認
- [ ] 承認後、worktree 内で `git checkout main && git merge <branch>`
- [ ] worktree ブランチ削除 (`git branch -d worktree-template-deps-bump`)
- [ ] セッション終了 (worktree 自動クリーンアップ)
