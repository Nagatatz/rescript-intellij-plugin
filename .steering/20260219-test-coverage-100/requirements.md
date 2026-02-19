# Requirements: テストカバレッジ 100% 到達

## 概要

現在の Line カバレッジ 39.1% を 100% に近づけるため、未テスト・低カバレッジのソースファイルに対して包括的なユニットテストを追加する。git worktree を使用した並列実装で効率的に進める。

## 背景

- 現在の全体カバレッジ: Line 39.1% / Instruction 36.9%
- 0% カバレッジのパッケージが 15 存在（completion, config, formatter, hierarchy, indexing, inspection, preview, refactor, spellcheck, structure, template, generate 等）
- 既存テスト 37 ファイルの多くがプロパティ検証のみで、メイン動作ロジックが未カバー

## 要求事項

### テスト対象（4 グループに分割して並列実装）

#### Group 1: 純粋ユニットテスト（新規 12 ファイル）
IDE 依存なし、直接インスタンス化でテスト可能なクラス:

| # | ファイル | テスト対象 |
|---|---------|----------|
| 1 | `RescriptNamesValidator` | `isIdentifier()`, `isKeyword()` — 正規表現 + キーワード判定 |
| 2 | `RescriptCommand` | `fromId()`, enum プロパティ |
| 3 | `RescriptCliDetector` | `findCli()` — ファイルシステム探索ロジック |
| 4 | `RescriptSemanticTokensSupport` | `getTextAttributesKey()` — トークンタイプマッピング |
| 5 | `RescriptCompilationStatusService` | `updateStatus()`, `addListener()` — 状態管理 + リスナー |
| 6 | `RescriptSpellcheckingStrategy` | `getTokenizer()` — トークンタイプ → トークナイザーマッピング |
| 7 | `RescriptPsiUtils` | `extractName()`, `getIcon()`, `getElementDescription()` |
| 8 | `RescriptTypeDeclarationParser` | `parse()`, `extractTypeName()` — テキストベースパーサー |
| 9 | `RescriptRunConfigurationType` | ID, displayName, factory 検証 |
| 10 | `RescriptConfigurationFactory` | ID, optionsClass 検証 |
| 11 | `RescriptTestRunConfigurationType` | ID, displayName, factory 検証 |
| 12 | `RescriptTestConfigurationFactory` | ID, optionsClass 検証 |

#### Group 2: Inspection & Analysis テスト（新規 3 + 改善 4 ファイル）
PSI スタブを使用した検査・分析ロジックのテスト:

| # | ファイル | 状態 | テスト対象 |
|---|---------|------|----------|
| 1 | `RescriptEmptyModuleInspection` | 新規 | 空モジュール検出 + QuickFix |
| 2 | `RescriptDuplicateOpenInspection` | 新規 | 重複 open 検出 + QuickFix |
| 3 | `RescriptMissingConfigInspection` | 新規 | 設定ファイル存在チェック |
| 4 | `RescriptReanalyzeAnnotator` | 改善 | `parseJsonOutput()` の未カバー分岐、`parseAllDiagnostics()` |
| 5 | `RescriptReanalyzeQuickFix` | 改善 | `findWordStart/End` エッジケース、`isAvailable()` |
| 6 | `RescriptUnusedCodeInspection` | 改善 | ファイル解決ロジック抽出 + テスト |
| 7 | `RescriptDependencyAnalyzer` | 改善 | `extractModulePath()`, `extractModuleReferences()` PSI テスト |

#### Group 3: Editor & Intention テスト（新規 3 + 改善 8 ファイル）
エディタ操作・インテンション・折りたたみのテスト:

| # | ファイル | 状態 | テスト対象 |
|---|---------|------|----------|
| 1 | `RescriptBreadcrumbsProvider` | 新規 | `acceptElement()`, `getElementInfo()` |
| 2 | `RescriptStructureViewElement` | 新規 | `getAlphaSortKey()`, `getPresentation()`, `getChildren()` |
| 3 | `RescriptPostfixTemplateProvider` | 新規 | テンプレート一覧、`isTerminalSymbol()` |
| 4 | `RescriptSmartEnterProcessor` | 改善 | `analyzeLine()` 追加分岐 |
| 5 | `RescriptStatementUpDownMover` | 改善 | `findDeclaration()`, `findNext/PreviousDeclaration()` |
| 6 | `RescriptWrapWithIntention` | 改善 | `isAvailable()`, `invoke()` 動作検証 |
| 7 | `RescriptAddGenTypeIntention` | 改善 | `findParentDeclaration()`, `hasGenTypeAnnotation()` |
| 8 | `RescriptSurroundDescriptor` | 改善 | `isApplicable()`, `getElementsToSurround()` |
| 9 | `RescriptFoldingBuilder` | 改善 | `extractJsxTagName()`, JSX プレースホルダー |
| 10 | `RescriptCustomFoldingProvider` | 改善 | エッジケース追加 |
| 11 | `RescriptLineIndentProvider` | 改善 | `isSuitableFor()`, 追加分岐 |

#### Group 4: Navigation & Misc テスト（新規 1 + 改善 7 ファイル）
ナビゲーション、インポート、その他のテスト:

| # | ファイル | 状態 | テスト対象 |
|---|---------|------|----------|
| 1 | `RescriptRawJsInjector` | 新規 | `getInjectionRange()` — テキスト範囲計算 |
| 2 | `RescriptQualifiedNameProvider` | 改善 | `getQualifiedName()`, `buildModulePath()` |
| 3 | `RescriptGotoRelatedProvider` | 改善 | `.mjs`/`.js` バリアント |
| 4 | `RescriptImportOptimizer` | 改善 | `processFile()`, `supports(true)` |
| 5 | `RescriptPasteAsJsonAction` | 改善 | `escapeString()` 追加ケース、`getActionUpdateThread()` |
| 6 | `RescriptTestFrameworkDetector` | 改善 | 未カバー分岐 |
| 7 | `RescriptTestLocator` | 改善 | 未カバー分岐 |
| 8 | `RescriptCompilerStatusWidgetFactory` | 改善 | 未カバー分岐 |

### テスト省略対象（理由付き）

| ファイル | 理由 |
|---------|------|
| `RescriptFormattingService` | 外部プロセス（`rescript format` CLI）依存 |
| `RescriptRenameHandler` | LSP サーバー依存 |
| `RescriptLspServerDescriptor` | LSP サーバー依存 |
| `RescriptLspServerSupportProvider` | LSP サーバー依存 |
| `RescriptLsp4jClient` | LSP サーバー依存 |
| `RescriptLanguageServer` | LSP インターフェース定義のみ |
| `RescriptSettingsEditor` | Swing UI |
| `RescriptTestSettingsEditor` | Swing UI |
| `RescriptConfigurable` | Swing UI |
| `RescriptCodeStyleSettingsProvider` | Swing UI |
| `RescriptProjectWizardStep` | Swing UI |
| `RescriptEditorNotificationProvider` | EditorNotification + LSP 結合 |
| `RescriptModuleHierarchyBrowser` | Swing HierarchyBrowser |
| `RescriptModuleHierarchyProvider` | DataContext + PSI 深い結合 |
| `RescriptModuleHierarchyNodeDescriptor` | UI 描画のみ |
| `RescriptCreateFileAction` | ダイアログ UI |
| `RescriptJsonIconProvider` | ロジック 1 行（`in` チェック） |
| `RescriptJsonSchemaProviderFactory` | 設定ワイヤリングのみ |
| `RescriptTodoIndexer` | フレームワーク委譲のみ |
| `RescriptStructureViewModel` | 設定値のみ |
| `RescriptStructureViewFactory` | ファクトリ委譲のみ |
| `RescriptPsi` | 定数定義のみ |
| `RescriptAstFactory` | 分岐 1 つのみ |
| `RescriptQuoteHandler` | フレームワーク拡張のみ |
| `RescriptRunConfiguration` | IDE Run 基盤依存 |
| `RescriptTestRunConfiguration` | IDE Run 基盤依存 |
| `RescriptTestConfigurationProducer` | PSI + RunManager 深い結合 |
| `RescriptTestConsoleProperties` | SMTRunner 依存 |
| `RescriptGenerateGroup` | ActionEvent 依存 |
| `RescriptGenerateSwitchAction` | Editor + PSI 深い結合 |
| `RescriptGenerateModuleTypeAction` | Editor + PSI 深い結合 |

## 実装アプローチ

git worktree を使用して 4 グループを並列実装する（バッチブランチ方式）。

## 受け入れ条件

- [ ] 新規テスト 19 ファイルが追加されている
- [ ] 既存テスト 19 ファイルのカバレッジが改善されている
- [ ] 全テストが `./gradlew test` で通過する
- [ ] `./gradlew buildPlugin` が成功する
- [ ] Line カバレッジが大幅に向上している（目標: 80%+）

## 制約事項

- 既存テストのパターン（JUnit 4、スタブベース PSI モック、`RescriptTestUtils`）に従う
- プロダクションコードへの変更は最小限にする（テスタビリティ向上のためのメソッド抽出は許可）
- worktree 間でファイル競合が発生しないよう、グループ分けを維持する
