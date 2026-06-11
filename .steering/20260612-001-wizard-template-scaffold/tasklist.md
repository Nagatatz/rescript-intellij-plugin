# タスクリスト: wizard テンプレートの scaffold 化 (Phase 5)

依存: 1 (golden) → 2 (scaffold) → 3〜6 (バッチ、順不同だが直列実施) → 7 (docs/マージ)。
各セクション = 1 コミット = 独立にビルド・テスト通過可能な単位。
体制: Fable = 設計/レビュー/検収、subagent (opus/sonnet) = golden 実装・バッチ移行。

## セクション 0: セットアップ

- [x] go/no-go 調査 (sonnet ×3 並列) + Fable 判定「go (scaffold 形式)」+ ユーザー承認 (2026-06-12)
- [ ] `git fetch origin` + main の ahead/behind 確認
- [ ] `EnterWorktree` で worktree 作成、編集パス確認
- [ ] `docs/product-requirements.md` に #131 (wizard scaffold 化) を 🚧 付きで追補 (最初のコミットに含める)

## セクション 1: golden テスト整備 (opus subagent → Fable 検収)

- [ ] `TemplateGoldenTest.kt` + golden 生成モードの実装 (design.md の ctx マトリクス ~74 combo)
- [ ] golden ファイル群を `src/test/testData/wizard-golden/` に生成・コミット
- [ ] 2 回連続実行で golden が揺れないことを確認 (非決定値の排除確認)
- [ ] `./gradlew ktlintCheck test` green
- [ ] コミット: `✅ Add golden characterization tests for all 22 wizard templates`

## セクション 2: scaffold 基盤 (Fable 実装)

- [ ] `wizard/templates/TemplateScaffold.kt` (commonTail / resourceFiles / validationVariant / standardDependencies、KDoc 付き)
- [ ] common tail の書き出し順依存の有無を確認 (ProjectTemplate 利用箇所)
- [ ] `TemplateScaffoldTest.kt` 単体テスト
- [ ] `./gradlew ktlintCheck test` green
- [ ] コミット: `✨ Add TemplateScaffold shared frame for wizard templates`

## セクション 3: バッチ A 移行 (sonnet subagent → Fable 検収)

- [ ] basic / vite-react / electron / cli-tool / npm-library / react-native を scaffold 化
- [ ] golden テスト + 既存 *TemplateFilesTest 無変更 green
- [ ] Fable diff レビュー
- [ ] コミット: `♻️ Migrate batch A templates to TemplateScaffold`

## セクション 4: バッチ B 移行 (sonnet subagent → Fable 検収)

- [ ] nextjs / react-native-cli / aws-lambda / cloudflare-workers / google-cloud-run を scaffold 化
- [ ] golden + 既存テスト green、Fable diff レビュー
- [ ] コミット: `♻️ Migrate batch B templates to TemplateScaffold`

## セクション 5: バッチ C 移行 (sonnet subagent → Fable 検収)

- [ ] tanstack-start / remix-v7 / astro / waku / res-x / tauri を scaffold 化
- [ ] golden + 既存テスト green、Fable diff レビュー
- [ ] コミット: `♻️ Migrate batch C templates to TemplateScaffold`

## セクション 6: バッチ D 移行 (opus subagent → Fable 検収)

- [ ] hono / hono-graphql / hono-inertia / monorepo / full-stack の標準フレーム部分のみ scaffold 化 (DB 分岐 / apiStrategy / PM ヘルパは現状維持)
- [ ] golden + 既存テスト green、Fable diff レビュー
- [ ] コミット: `♻️ Migrate batch D templates to TemplateScaffold`

## セクション 7: docs + マージ前検証

- [ ] repository-structure.md の wizard/templates/ 行に `TemplateScaffold` 追記
- [ ] product-requirements.md の #131 を削除
- [ ] コミット: `📝 Sync docs for Phase 5 wizard scaffold`
- [ ] `./gradlew ktlintCheck clean buildPlugin test koverVerify verifyPluginStructure` green + `test --rerun` 実実行
- [ ] 純減行数の実測記録 (目標 ~800 行以上)
- [ ] `./gradlew runIde` スモーク: New Project から Basic / Hono / Tauri を実生成
- [ ] tasklist 全項目 `[x]` 更新をマージ前最終コミットに含める
- [ ] `AskUserQuestion` でマージ可否確認
- [ ] main へマージ → ブランチ削除 → push

## テスト免除の記載

免除対象なし (TemplateScaffold はテスト必須 + golden が全体を覆う。wizard は kover 除外パッケージだが golden/単体テストは test タスクで実行される)。
