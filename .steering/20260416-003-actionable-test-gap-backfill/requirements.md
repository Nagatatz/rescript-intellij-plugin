# Requirements — ACTIONABLE Test Gap Backfill

## 背景

現在、本番コード 304 ファイルに対しテストファイル 264 ファイル。`<Name>Test.kt` が存在しない 53 ファイルのうち 41 は `.claude/rules/testing.md` の免除対象（UI / LSP 結合 / IDE ライフサイクル / 純データクラス）。

残る 12 ファイルが ACTIONABLE — 非自明なロジックを持ち、テスト可能で、`testing.md` の免除基準にも該当しない。これらを Workstream B として一括でバックフィルする。

参考: `/Users/ngtz/.claude/plans/peaceful-swinging-emerson.md` の Workstream B セクション。

## 対象ファイル

### Group 1 — 純ロジック / PSI スタブで完結 (9件)

| # | ファイル | テスト方針 |
|---|---|---|
| 1 | `RescriptIcons.kt` | 全 icon フィールド非 null + classpath にアイコン実体が存在 |
| 2 | `config/RescriptJsonSchemaProviderFactory.kt` | `isAvailable(VirtualFile)` の名前判定（`rescript.json` / `bsconfig.json` / 他） |
| 3 | `diagram/RescriptDependencyDiagramProvider.kt` | `extractDependencies` は既存テストでカバー済み。`buildDiagram` は Project が必要なので fixture テスト |
| 4 | `hierarchy/RescriptModuleHierarchyNodeDescriptor.kt` | `update()` のラベル整形 / icon 設定 — PSI スタブで検証 |
| 5 | `hierarchy/RescriptModuleHierarchyTreeStructure.kt` (および `RescriptModuleDependencyTreeStructure`) | `buildChildren` の MODULE_DECLARATION フィルタ / RescriptFile ガード — PSI スタブ |
| 6 | `hierarchy/call/RescriptCallHierarchyNodeDescriptor.kt` | 同 4 (call 系) |
| 7 | `hierarchy/call/RescriptCalleeTreeStructure.kt` | `buildChildren` が `findCallees` 結果を子ノード化 — PSI スタブ |
| 8 | `hierarchy/call/RescriptCallerTreeStructure.kt` | 同 7 (caller 系) |
| 9 | `lang/psi/RescriptDeclarationPsiElement.kt` | `getDeclarationName()` の stub→fallback 委譲 / `toString()` |

### Group 2 — Fixture / 重め (3件)

| # | ファイル | テスト方針 |
|---|---|---|
| 10 | `formatter/RescriptFormattingService.kt` | `BasePlatformTestCase` で `canFormat(PsiFile)` のファイルタイプ判定 / `getName()` / `getNotificationGroupId()` を検証。外部プロセス起動 (`createFormattingTask`) は `RescriptCliDetector` が null を返すケースのみ検証 |
| 11 | `hierarchy/RescriptModuleHierarchyBrowser.kt` | `BasePlatformTestCase` で `isApplicableElement` (RescriptFile / MODULE_DECLARATION) と `createHierarchyTreeStructure` (typeName routing) を検証 |
| 12 | `hierarchy/call/RescriptCallHierarchyBrowser.kt` | 同 11 (call 系) |

### 当初プラン記載で再判定するもの

| ファイル | 判定 |
|---|---|
| `paste/RescriptBasePasteProcessor.kt` | **免除**: `abstract` 基底クラスで、すべてのテストはサブクラス側 (`RescriptPasteAsRescriptProcessor`, `RescriptPasteAsJsxProcessor`) でカバー済み。`extractTransferableData` / `processTransferableData` のフローは Editor / Document / WriteCommandAction / DataFlavor を含むため抽象クラス単体での意味のあるテストが困難 |
| `imports/RescriptAutoImportOptionsProvider.kt` | **免除**: `AutoImportOptionsProvider` 実装で純粋な Swing UI（JCheckBox / JTextField / JPanel / FlowLayout）。`testing.md` 免除カテゴリ「Swing UI コンポーネント」に該当 |

## 受け入れ条件

- [ ] Group 1 の 9 ファイルそれぞれに対応する `<ClassName>Test.kt` が存在する
- [ ] Group 2 の 3 ファイルそれぞれに対応する `<ClassName>Test.kt` が存在する
- [ ] 免除対象 (`RescriptBasePasteProcessor`, `RescriptAutoImportOptionsProvider`) は tasklist に理由を明記
- [ ] `./gradlew ktlintCheck` が成功する
- [ ] `./gradlew test` が成功する（既存テストに加えて新規テストもすべて通る）
- [ ] `./gradlew clean buildPlugin` が成功する
- [ ] `./gradlew koverHtmlReport` で新規テストが反映されたカバレッジレポートが生成される
- [ ] 新規テスト追加によりカバー対象になったクラス/パッケージは `build.gradle.kts` の kover excludes から外す
- [ ] `kover.minBound` を実測 - 3% に引き上げる（一方向ラチェット）
- [ ] tasklist.md の全タスクが `[x]` になっている

## 範囲外

- 本番コードの改修 — テスト追加のみ。バグ発見時は別ステアリングで対応
- Wizard テンプレートの resource 化 (Workstream A — 完了済)
- 41 件の EXEMPT ファイルへのテスト追加 — `testing.md` 免除対象
- ドキュメント整理 — 別 console で対応中
