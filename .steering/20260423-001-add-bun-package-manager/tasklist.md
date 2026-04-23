# tasklist.md — `PackageManager.BUN` の追加

## Phase 1: セットアップ

- [x] `.steering/20260423-001-add-bun-package-manager/` + requirements.md / design.md / tasklist.md 作成
- [x] `feature/add-bun-package-manager` ブランチ作成

## Phase 2: 項目ごとの実装

計画上の #3・#4 はコミット #1 に統合した（enum 追加で `when` の網羅性を壊さないため、同一コミットで全ての分岐を埋める必要があった）。

- [x] #1 Core enum + TemplateContext + TemplateVersions + CommonFiles.packageManagerName/readme + **MonorepoTemplateFiles 4 ヘルパー + GoogleCloudRunTemplateFiles.dockerfile() の BUN 分岐** を一括 → `🔧 Add PackageManager.BUN entry and thread it through TemplateContext`
- [x] #2 CommonFiles.ciWorkflow 自動 Bun セットアップ → `🔧 Auto-enable setup-bun in CI workflow when the PackageManager is BUN`
- [x] #5 TemplateResourcesSmokeTest に BUN コンテキスト追加 → `✅ Cover BUN contexts in the placeholder residue smoke test`
- [x] 追加: `RescriptProjectGeneratorTest` の enum サイズアサーションを BUN 対応に更新 → `✅ Extend RescriptProjectGeneratorTest to cover PackageManager.BUN`

## Phase 3: コミット前検証（DoD Phase 3）

- [x] `./gradlew ktlintCheck` 成功
- [x] `./gradlew clean buildPlugin` 成功
- [x] `./gradlew test` 成功
- [x] `./gradlew integrationTest` 成功（16 テンプレート PNPM 系全パス）
- [x] `./gradlew verifyPluginStructure` 成功

## Phase 4: ステアリング同期

- [x] tasklist.md の全タスクを `[x]` に更新
- [x] ステアリング最終状態をコミット `📝 Add steering docs for PackageManager.BUN`

## Phase 5: マージ

- [x] `AskUserQuestion` でマージ可否確認
- [x] `main` にマージ、`feature/add-bun-package-manager` ブランチを削除
