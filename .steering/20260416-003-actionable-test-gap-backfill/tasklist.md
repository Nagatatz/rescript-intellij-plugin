# Tasklist — ACTIONABLE Test Gap Backfill

## Phase 1: 計画 (完了済)

- [x] `.steering/20260416-003-actionable-test-gap-backfill/` 作成
- [x] `requirements.md` 作成
- [x] `design.md` 作成
- [x] `tasklist.md` 作成
- [x] ユーザー承認取得
- [x] `EnterWorktree test-gap-backfill` で worktree 作成

## Phase 2: 実装

### Group 1 — 純ロジック / Fixture (9 ファイル)

#### コミット 1: ✅ Add RescriptIconsTest
- [x] `src/test/kotlin/com/rescript/plugin/RescriptIconsTest.kt` 作成（FILE/INTERFACE_FILE/CONFIG_FILE 非 null + classpath にアイコンリソース存在）
- [x] `./gradlew test --tests "com.rescript.plugin.RescriptIconsTest"` パス確認
- [x] tasklist 更新 + コミット

#### コミット 2: ✅ Add RescriptJsonSchemaProviderFactoryTest
- [x] `src/test/kotlin/com/rescript/plugin/config/RescriptJsonSchemaProviderFactoryTest.kt` 作成
- [x] テスト: `isAvailable` で `rescript.json` true, `bsconfig.json` true, `package.json` false, etc.
- [x] `./gradlew test --tests "com.rescript.plugin.config.RescriptJsonSchemaProviderFactoryTest"` パス確認
- [x] tasklist 更新 + コミット

#### コミット 3: ✅ Add RescriptDeclarationPsiElementTest
- [x] `src/test/kotlin/com/rescript/plugin/lang/psi/RescriptDeclarationPsiElementTest.kt` 作成
- [x] テスト: fixture で `let foo = 1` 解析、getDeclarationName() == "foo", toString() prefix 確認
- [x] `./gradlew test --tests "com.rescript.plugin.lang.psi.RescriptDeclarationPsiElementTest"` パス確認
- [x] tasklist 更新 + コミット

#### コミット 4: ✅ Add tests for module hierarchy descriptors and tree structures
- [x] `src/test/kotlin/com/rescript/plugin/hierarchy/RescriptModuleHierarchyNodeDescriptorTest.kt` 作成
- [x] `src/test/kotlin/com/rescript/plugin/hierarchy/RescriptModuleHierarchyTreeStructureTest.kt` 作成（両 TreeStructure を含む）
- [x] `./gradlew test --tests "com.rescript.plugin.hierarchy.*"` パス確認
- [x] tasklist 更新 + コミット

#### コミット 5: ✅ Add tests for call hierarchy descriptors and tree structures
- [x] `src/test/kotlin/com/rescript/plugin/hierarchy/call/RescriptCallHierarchyNodeDescriptorTest.kt` 作成
- [x] `src/test/kotlin/com/rescript/plugin/hierarchy/call/RescriptCalleeTreeStructureTest.kt` 作成
- [x] `src/test/kotlin/com/rescript/plugin/hierarchy/call/RescriptCallerTreeStructureTest.kt` 作成（PsiSearchHelper 依存により caller find 結果のカウント確認は構造的不変条件のみ）
- [x] `./gradlew test --tests "com.rescript.plugin.hierarchy.call.*"` パス確認
- [x] tasklist 更新 + コミット

#### コミット 6: ✅ Add RescriptDependencyDiagramProviderTest
- [x] `src/test/kotlin/com/rescript/plugin/diagram/RescriptDependencyDiagramProviderTest.kt` 作成（`buildDiagram` の smoke test。`EMPTY_PROJECT_DESCRIPTOR` には content root がないため `addFileToProject` は FileTypeIndex に反映されず、populated case は意図的に省略。理由はクラス KDoc に記載）
- [x] `./gradlew test --tests "com.rescript.plugin.diagram.*"` パス確認
- [x] tasklist 更新 + コミット

### Group 2 — Fixture / 重め (3 ファイル)

#### コミット 7: ✅ Add RescriptFormattingServiceTest
- [x] `src/test/kotlin/com/rescript/plugin/formatter/RescriptFormattingServiceTest.kt` 作成
- [x] テスト: `canFormat`/`getFeatures` (public)、`getName/getNotificationGroupId` は protected のためリフレクションで検証。`createFormattingTask` の happy path は AsyncFormattingRequest 構築と CLI バイナリが必要なため省略 (KDoc に記載)
- [x] `./gradlew test --tests "com.rescript.plugin.formatter.RescriptFormattingServiceTest"` パス確認
- [x] tasklist 更新 + コミット

#### コミット 8: ✅ Add RescriptModuleHierarchyBrowserTest
- [x] `src/test/kotlin/com/rescript/plugin/hierarchy/RescriptModuleHierarchyBrowserTest.kt` 作成
- [x] テスト: `isApplicableElement`, `createHierarchyTreeStructure` の typeName ルーティング、`getContentDisplayName`, getActionPlace 等の定数（protected メソッドはリフレクション経由）
- [x] `./gradlew test --tests "com.rescript.plugin.hierarchy.RescriptModuleHierarchyBrowserTest"` パス確認
- [x] tasklist 更新 + コミット

#### コミット 9: ✅ Add RescriptCallHierarchyBrowserTest
- [x] `src/test/kotlin/com/rescript/plugin/hierarchy/call/RescriptCallHierarchyBrowserTest.kt` 作成
- [x] テスト: 同 8 (call 系: applicability, typeName ルーティング, getContentDisplayName, 定数)
- [x] `./gradlew test --tests "com.rescript.plugin.hierarchy.call.RescriptCallHierarchyBrowserTest"` パス確認
- [x] tasklist 更新 + コミット

### Group 3 — kover 設定更新

#### コミット 10: 🔧 Update kover excludes and ratchet minBound
- [ ] `./gradlew test koverHtmlReport` で実測カバレッジを確認
- [ ] `build.gradle.kts` の class excludes から、新規テストで完全カバーされたクラスを削除を試みる:
  - `com.rescript.plugin.RescriptIcons` (削除を試行)
  - `com.rescript.plugin.lang.psi.RescriptDeclarationPsiElement*` (削除を試行)
- [ ] 実測値を確認し、`minBound` を「実測 - 3%」に引き上げる
- [ ] `./gradlew koverHtmlReport` で `minBound` 違反がないことを確認
- [ ] tasklist 更新 + コミット

## 免除対象 (テスト作成省略)

以下の 2 クラスは `.claude/rules/testing.md` の免除基準に該当するためテスト作成を省略する:

| クラス | 免除理由 |
|---|---|
| `paste/RescriptBasePasteProcessor.kt` | abstract 基底クラス。Editor / Document / DataFlavor / WriteCommandAction を伴う処理は派生クラス (`RescriptPasteAsRescriptProcessor` / `RescriptPasteAsJsxProcessor`) のテストで完全にカバーされる |
| `imports/RescriptAutoImportOptionsProvider.kt` | Swing UI コンポーネント (`AutoImportOptionsProvider`)。JCheckBox / JTextField / JPanel / FlowLayout のみ。「Swing UI コンポーネント」免除カテゴリ該当 |

## Phase 3: コミット前検証 (各コミットで実行)

各コミットで以下を確認:

- [ ] `./gradlew ktlintCheck` パス
- [ ] 該当テストパス
- [ ] 新規テストファイルにクラスレベル KDoc (英語) が付与されている
- [ ] tasklist 更新がコミットに含まれている

最終コミット (#10) 前に追加で:

- [ ] `./gradlew clean buildPlugin` パス
- [ ] `./gradlew test` 全件パス

## Phase 4: マージ前

- [ ] tasklist の全タスクが `[x]` (本タスク含む)
- [ ] requirements.md の受け入れ条件全充足
- [ ] `AskUserQuestion` でユーザーにマージ可否確認
- [ ] ビルド確認 + tasklist 完了確認 + main マージ

## Phase 5: マージ後

- [ ] worktree 内で `git checkout main && git merge worktree-test-gap-backfill`
- [ ] `git branch -d worktree-test-gap-backfill`
- [ ] セッション終了で worktree 自動クリーンアップ

## ドキュメント更新

本ワークストリームは「テスト追加のみ」で本番コード・機能変更がないため、`.claude/rules/documentation.md` の同期対象は **CLAUDE.md / README.md / sphinx-docs に変更不要**。`docs/product-requirements.md` のロードマップ機能にも該当しない。
