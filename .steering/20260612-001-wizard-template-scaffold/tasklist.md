# タスクリスト: wizard テンプレートの scaffold 化 (Phase 5)

依存: 1 (golden) → 2 (scaffold) → 3〜6 (バッチ、順不同だが直列実施) → 7 (docs/マージ)。
各セクション = 1 コミット = 独立にビルド・テスト通過可能な単位。
体制: Fable = 設計/レビュー/検収、subagent (opus/sonnet) = golden 実装・バッチ移行。

## セクション 0: セットアップ

- [x] go/no-go 調査 (sonnet ×3 並列) + Fable 判定「go (scaffold 形式)」+ ユーザー承認 (2026-06-12)
- [x] `git fetch origin` + main の ahead/behind 確認 (0/0)
- [x] `EnterWorktree` で worktree 作成、編集パス確認
- [x] `docs/product-requirements.md` に #131 (wizard scaffold 化) を 🚧 付きで追補 (最初のコミットに含める)

## セクション 1: golden テスト整備 (opus subagent → Fable 検収)

- [x] `TemplateGoldenTest.kt` + golden 生成モードの実装 (74 combo: 22×2 PM + 18 sury + 5×2 DB + graphql + npm-workspace。opus 実装、enum プロパティからマトリクスをプログラム的に構築)
- [x] golden ファイル群 74 件を `src/test/testData/wizard-golden/` に生成
- [x] 2 回連続実行 + Fable 検収再実行で golden 不変を確認 (year は 2026 固定、他の非決定値なしを grep 確認)
- [x] `./gradlew ktlintCheck test` green
- [x] コミット: `✅ Add golden characterization tests for all 22 wizard templates`

## セクション 2: scaffold 基盤 (Fable 実装)

- [x] `wizard/templates/TemplateScaffold.kt` (commonTail / resourceFiles / validationVariant / validationDependency / standardDependencies、KDoc 付き)
- [x] 書き出し順依存なしを確認: 消費側は RescriptProjectGenerator → RescriptModuleBuilder の VFS 書き込みのみで、Map 順序は成果物に影響しない。golden もソート比較
- [x] `TemplateScaffoldTest.kt` 単体テスト (tail キー順 / フラグ転送 / リソース解決 / variant / 依存切替の 6 ケース)
- [x] `./gradlew ktlintCheck test` green
- [x] コミット: `✨ Add TemplateScaffold shared frame for wizard templates`

## セクション 3: バッチ A 移行 (sonnet subagent → Fable 検収)

- [x] basic / vite-react / electron / cli-tool / npm-library / react-native を scaffold 化 (sonnet。+479/-551。cli-tool の vars 混在 load と react 系の依存順は適切に個別残し)
- [x] golden テスト + 既存 *TemplateFilesTest 無変更 green (Fable 再実行で確認)
- [x] Fable diff レビュー (BasicTemplateFiles 代表確認 + golden 0 差分)
- [x] コミット: `♻️ Migrate batch A templates to TemplateScaffold`

## セクション 4: バッチ B 移行 (sonnet subagent → Fable 検収)

- [x] nextjs / react-native-cli / aws-lambda / cloudflare-workers / google-cloud-run を scaffold 化 (sonnet。+499/-511。Hono バインディング混在部と CommonFiles ビルダー由来は適切に個別残し)
- [x] golden + 既存テスト green (Fable 再実行確認)、Fable diff レビュー
- [x] コミット: `♻️ Migrate batch B templates to TemplateScaffold`

## セクション 5: バッチ C 移行 (sonnet subagent → Fable 検収)

- [x] tanstack-start / remix-v7 / astro / waku / res-x / tauri を scaffold 化 (sonnet。初回は命令形書き換えで modern 4 種が +9〜+11 行純増 → Fable レビューで差し戻し、式スタイル (`mapOf + scaffold + commonTail`) に手直しして +3〜+8 に収束。ResX の不要な README 順序トリックも除去 (-10)。tauri の Rust 連携・remix の appSources・res-x の rescript.json 特殊形は維持)
- [x] golden + 既存テスト green (Fable 再実行確認)、Fable diff レビュー (1 回差し戻し)
- [x] コミット: `♻️ Migrate batch C templates to TemplateScaffold`

## セクション 6: バッチ D 移行 (opus subagent → Fable 検収)

- [x] hono / hono-graphql / hono-inertia / monorepo / full-stack の標準フレーム部分のみ scaffold 化 (opus。+106/-137。FullStack は README/.gitignore がブランチ側のため commonTail を規約どおり不適用、validationVariant/validationDependency のみ。interleave した load 群は現状維持を選択)
- [x] golden (DB/GraphQL combo 含む全 74 件) + 既存テスト green (Fable 再実行確認)、Fable diff レビュー
- [x] コミット: `♻️ Migrate batch D templates to TemplateScaffold`

## セクション 7: docs + マージ前検証

- [x] repository-structure.md の wizard/templates/ 行に `TemplateScaffold` 追記
- [x] product-requirements.md の #131 を削除 (候補テーブルは再び空のため見出しごと削除)
- [ ] コミット: `📝 Sync docs for Phase 5 wizard scaffold`
- [ ] `./gradlew ktlintCheck clean buildPlugin test koverVerify verifyPluginStructure` green + `test --rerun` 実実行
- [x] 純減行数の実測: **+39 行で目標未達** (既存 22 クラス −111 / scaffold +150)。requirements.md「実装結果の評価」に分析を追記し、マージ確認で明示する
- [ ] `./gradlew runIde` スモーク: New Project から Basic / Hono / Tauri を実生成
- [ ] tasklist 全項目 `[x]` 更新をマージ前最終コミットに含める
- [ ] `AskUserQuestion` でマージ可否確認
- [ ] main へマージ → ブランチ削除 → push

## テスト免除の記載

免除対象なし (TemplateScaffold はテスト必須 + golden が全体を覆う。wizard は kover 除外パッケージだが golden/単体テストは test タスクで実行される)。
