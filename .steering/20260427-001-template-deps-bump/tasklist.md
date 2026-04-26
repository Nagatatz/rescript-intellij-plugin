# タスクリスト

## Phase 1: ステアリング

- [x] requirements.md 作成 / 承認 (auto モードで自動承認扱い)
- [x] design.md 作成
- [x] tasklist.md 作成

## Phase 2: 実装 (worktree 内)

### コミット 1: Node 24 LTS

- [x] worktree 作成 (`EnterWorktree name=template-deps-bump`)
- [x] `TemplateVersions.NODE_ENGINE = ">=24"`、`NODE_MAJOR = "24"` に更新
- [x] `ProjectTemplate.kt` の "Node.js 22+" を "Node.js 24+" に置換 (16 箇所)
- [x] `sphinx-docs/user/templates/*.md` の Node 22 言及を 24 に更新
- [x] 対応 `.po` の `msgstr` を更新
- [x] `🔧 Bump templates to Node 24 LTS (Krypton)` でコミット (5366ddc)

### コミット 2: @hono/node-server v2

- [x] `TemplateVersions.HONO_NODE_SERVER = "^2.0.0"`
- [x] `🔧 Bump @hono/node-server to v2.0.0` でコミット (a6ac59f)

### コミット 3: relay-compiler v20

- [x] `TemplateVersions.RELAY_COMPILER = "^20.1.1"`
- [x] `TemplateVersions.RESCRIPT_RELAY = "^4.4.1"` (peer dep 整合)
- [x] `🔧 Align relay-compiler with rescript-relay 4.x peer deps` でコミット (b65bce5)

### コミット 4: bun floor

- [x] `TemplateVersions.BUN = "1.3.13"`
- [x] `ProjectTemplate.kt` の "Bun 1.1+" を "Bun 1.3+" に更新
- [x] `🔧 Bump bun packageManager floor to 1.3.13` でコミット (9381c55)

### コミット 5: patch/minor バンドル

- [x] design.md の表に従い `TemplateVersions.kt` の各定数を更新
- [x] `🔧 Bump template dependencies to latest patches` でコミット (38c9171)

## Phase 3: 検証

- [x] `./gradlew ktlintCheck` 成功
- [x] `./gradlew clean buildPlugin` 成功
- [x] `./gradlew test` 成功 — 初回は Kotlin `const val` インライン値が build cache から復元されたため fail。`--rerun-tasks --no-build-cache` で再実行し全 3517 件グリーン
- [x] `./gradlew verifyPluginStructure` 成功

## Phase 4: マージ

- [x] tasklist.md のすべてのタスクが `[x]` (本タスク含む)
- [x] `AskUserQuestion` でマージ可否確認 → 承認
- [x] 承認後、`git -C <main> merge worktree-template-deps-bump --no-ff` でマージ
- [x] worktree ブランチ削除 (`git branch -d worktree-template-deps-bump`)
- [ ] セッション終了 (worktree 自動クリーンアップ)
