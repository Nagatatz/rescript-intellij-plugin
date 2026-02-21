# IntelliJ Platform 拡張能力ガイド — ReScript プラグイン開発者向け

## 1. IntelliJ Platform の言語プラグインアーキテクチャ

IntelliJ Platform は**Extension Point パターン**を採用しており、プラグインは特定のインターフェースを実装して `plugin.xml` に登録することで IDE の機能を拡張する。

### レイヤー構成

```
┌──────────────────────────────────────┐
│         ユーザーインターフェース         │  Settings, Tool Windows, Dialogs
├──────────────────────────────────────┤
│           IDE 統合レイヤー             │  Actions, Menus, Keymaps, Notifications
├──────────────────────────────────────┤
│         コード洞察 (Code Insight)       │  Completion, Navigation, Refactoring
├──────────────────────────────────────┤
│            PSI (Program Structure)     │  AST, Resolve, Type System
├──────────────────────────────────────┤
│           レキシカル解析               │  Lexer, Parser, Tokens
├──────────────────────────────────────┤
│         VFS / Document / Editor        │  ファイルシステム, テキストモデル
└──────────────────────────────────────┘
```

ReScript プラグインの場合:
- **レキシカル解析**: JFlex レクサー + 軽量パーサー（プラグイン内蔵）
- **PSI**: トップレベル宣言 + JSX の限定的な PSI ツリー
- **コード洞察**: LSP サーバーに委譲（補完、定義ジャンプ、診断等）
- **IDE 統合**: Extension Point 登録によるネイティブ機能

## 2. Extension Point カテゴリ別の能力

### 2-1. 言語基盤（Language Foundation）

言語のコアとなる登録・解析・表示機能。

| Extension Point | インターフェース | 何ができるか | ReScript での活用状況 |
|---|---|---|---|
| `com.intellij.fileType` | `LanguageFileType` | ファイルタイプの登録、アイコン設定 | ✅ 実装済み |
| `com.intellij.lang.parserDefinition` | `ParserDefinition` | パーサー・PSI ファクトリの登録 | ✅ 実装済み |
| `com.intellij.lang.syntaxHighlighterFactory` | `SyntaxHighlighterFactory` | レクサーベースのハイライト | ✅ 実装済み |
| `com.intellij.colorSettingsPage` | `ColorSettingsPage` | Settings > Editor > Color Scheme にカスタム色設定 | ✅ 実装済み |
| `com.intellij.lang.braceMatcher` | `PairedBraceMatcher` | 括弧のペアマッチング | ✅ 実装済み |
| `com.intellij.lang.commenter` | `Commenter` | コメントの挿入/除去 | ✅ 実装済み |
| `com.intellij.lang.foldingBuilder` | `FoldingBuilder` | コード折りたたみ | ✅ 実装済み |
| `com.intellij.lang.psiStructureViewFactory` | `PsiStructureViewFactory` | Structure ビュー（ファイル内シンボル一覧） | ✅ 実装済み |

### 2-2. コード編集支援（Editor Enhancements）

入力中のテキスト操作を拡張する機能群。

| Extension Point | インターフェース | 何ができるか | ReScript での活用 |
|---|---|---|---|
| `com.intellij.typedHandler` | `TypedHandlerDelegate` | 特定文字入力時のカスタム動作（`>` で JSX 閉じタグ挿入等） | ✅ 実装済み |
| `com.intellij.enterHandlerDelegate` | `EnterHandlerDelegate` | Enter 時のカスタム動作（コメント継続等） | ✅ 実装済み |
| `com.intellij.backspaceHandlerDelegate` | `BackspaceHandlerDelegate` | Backspace のカスタム動作（ペア削除等） | 未実装 |
| `com.intellij.joinLinesHandler` | `JoinLinesHandlerDelegate` | Ctrl+Shift+J のスマート行結合 | ✅ 実装済み |
| `com.intellij.statementUpDownMover` | `StatementUpDownMover` | Alt+Shift+Up/Down の宣言移動 | ✅ 実装済み |
| `com.intellij.lang.smartEnterProcessor` | `SmartEnterProcessor` | Shift+Enter のスマート補完 | ✅ 実装済み |
| `com.intellij.extendWordSelectionHandler` | `ExtendWordSelectionHandler` | Ctrl+W の選択拡大カスタマイズ | ✅ 実装済み |
| `com.intellij.lang.unwrapDescriptor` | `UnwrapDescriptor` | Ctrl+Shift+Delete の囲み除去 | ✅ 実装済み |
| `com.intellij.lang.surroundDescriptor` | `SurroundDescriptor` | Ctrl+Alt+T の囲み挿入 | ✅ 実装済み |
| `com.intellij.moveLeftRightHandler` | `MoveElementLeftRightHandler` | Alt+Shift+Left/Right の要素入れ替え | 未実装 |
| `com.intellij.listSplitJoinContext` | `ListSplitJoinContext` | 引数/配列の1行⇔複数行変換 | 未実装 |
| `com.intellij.codeBlockSupportHandler` | `CodeBlockSupportHandler` | Ctrl+Shift+M のブロック間ナビ | 未実装 |

### 2-3. コード補完（Completion）

補完候補の表示・制御・拡張。

| Extension Point | インターフェース | 何ができるか | ReScript での活用 |
|---|---|---|---|
| `com.intellij.completion.contributor` | `CompletionContributor` | カスタム補完候補の提供 | LSP 経由で対応 |
| `com.intellij.completion.confidence` | `CompletionConfidence` | 自動補完ポップアップの表示制御 | ✅ 実装済み |
| `com.intellij.codeInsight.template.postfixTemplateProvider` | `PostfixTemplateProvider` | `.switch`, `.pipe` 等のポスト補完 | ✅ 実装済み |
| `com.intellij.liveTemplateContext` | `TemplateContextType` | Live Template の有効コンテキスト制御 | ✅ 実装済み |
| `com.intellij.liveTemplateMacro` | `Macro` | Live Template のカスタム変数マクロ | ✅ 実装済み |
| `com.intellij.lookup.charFilter` | `CharFilter` | 補完ポップアップ中の文字入力制御 | 未実装 |
| `com.intellij.weigher` (key=completion) | `CompletionWeigher` | 補完候補の優先順位付け | 未実装 |

### 2-4. ナビゲーション（Navigation）

コード内の移動・検索・ジャンプ。

| Extension Point | インターフェース | 何ができるか | ReScript での活用 |
|---|---|---|---|
| `com.intellij.gotoSymbolContributor` | `ChooseByNameContributorEx` | Cmd+Option+O のシンボル検索 | ✅ 実装済み |
| `com.intellij.gotoRelatedProvider` | `GotoRelatedProvider` | Navigate > Related Symbol | ✅ 実装済み |
| `com.intellij.codeInsight.gotoSuper` | `CodeInsightActionHandler` | Ctrl+U で親/インターフェースへジャンプ | ✅ 実装済み |
| `com.intellij.testCreator` | `TestCreator` | Ctrl+Shift+T で実装⇔テスト移動 | ✅ 実装済み |
| `com.intellij.qualifiedNameProvider` | `QualifiedNameProvider` | 完全修飾名コピー | ✅ 実装済み |
| `com.intellij.breadcrumbsInfoProvider` | `BreadcrumbsProvider` | パンくずナビゲーション | ✅ 実装済み |
| `com.intellij.declarationRangeHandler` | `DeclarationRangeHandler` | Context Info（スティッキーヘッダー） | ✅ 実装済み |
| `com.intellij.callHierarchyProvider` | `HierarchyProvider` | 呼び出し階層ツリー | 未実装 |
| `com.intellij.searchEverywhereContributor` | `SearchEverywhereContributorFactory` | Shift+Shift にカスタムタブ追加 | 未実装 |
| `com.intellij.runAnythingProvider` | `RunAnythingProvider` | Ctrl+Ctrl でコマンド実行 | ✅ 実装済み |

### 2-5. コード分析・インスペクション（Analysis & Inspection）

コード品質の検証・改善。

| Extension Point | インターフェース | 何ができるか | ReScript での活用 |
|---|---|---|---|
| `com.intellij.localInspection` | `LocalInspectionTool` | ファイル内のコード品質検査 | ✅ 実装済み (3件) |
| `com.intellij.globalInspection` | `GlobalInspectionTool` | プロジェクト全体の検査 | ✅ 実装済み |
| `com.intellij.externalAnnotator` | `ExternalAnnotator` | 外部ツール結果のアノテーション | ✅ 実装済み (reanalyze) |
| `com.intellij.problemHighlightFilter` | `ProblemHighlightFilter` | 特定ファイルのエラー抑制 | ✅ 実装済み |
| `com.intellij.highlightUsagesHandlerFactory` | `HighlightUsagesHandlerFactory` | カスタムハイライト（switch/if/try） | ✅ 実装済み |
| `com.intellij.codeInsight.expressionTypeProvider` | `ExpressionTypeProvider` | Ctrl+Shift+P で式の型表示 | ✅ 実装済み |
| `com.intellij.lang.inspectionSuppressor` | `InspectionSuppressor` | コメントによるインスペクション抑制 | 未実装 |
| `com.intellij.usageTypeProvider` | `UsageTypeProvider` | Find Usages 結果のグルーピング | 未実装 |

### 2-6. リファクタリング（Refactoring）

コード構造の安全な変更。

| Extension Point | インターフェース | 何ができるか | ReScript での活用 |
|---|---|---|---|
| `com.intellij.renameHandler` | `RenameHandler` | カスタムリネーム処理 | ✅ 実装済み (LSP) |
| `com.intellij.lang.namesValidator` | `NamesValidator` | 識別子名の妥当性検証 | ✅ 実装済み |
| `com.intellij.lang.importOptimizer` | `ImportOptimizer` | Ctrl+Alt+O の import 最適化 | ✅ 実装済み |
| `com.intellij.lang.refactoringSupport` | `RefactoringSupportProvider` | Extract/Inline/Safe Delete 等 | 未実装 |
| `com.intellij.nameSuggestionProvider` | `NameSuggestionProvider` | 名前候補の提示 | 未実装 |

### 2-7. 意図アクション・Quick Fix（Intentions & Quick Fixes）

Alt+Enter で利用可能なコード変換・修正。

| Extension Point | インターフェース | 何ができるか | ReScript での活用 |
|---|---|---|---|
| `com.intellij.intentionAction` | `IntentionAction` | Alt+Enter のコード変換アクション | ✅ 実装済み (5件) |
| Quick Fix (LSP Code Action) | — | LSP サーバーの Code Action | ✅ LSP API で自動対応 |

**拡張可能な Intention の例**:
- Pipe ⇔ 関数呼び出し変換
- 分割代入の導入/解除
- 未使用結果の `->ignore` 追加
- 冗長ブロック削除
- 識別子ケース修正
- インターフェース公開/非公開

### 2-8. コード生成（Code Generation）

Generate メニュー (Cmd+N / Alt+Insert) で利用可能なコード自動生成。

| Extension Point | インターフェース | 何ができるか | ReScript での活用 |
|---|---|---|---|
| `com.intellij.codeInsight.generation` | `CodeInsightAction` | Generate メニューへのアクション追加 | ✅ 実装済み (2件) |
| `com.intellij.codeInsight.implementMethod` | `LanguageImplementMethodsHandler` | インターフェースの実装スタブ生成 | 未実装 |
| `com.intellij.codeInsight.delegateMethods` | `LanguageDelegateMethods` | 委譲メソッドの生成 | 未実装 |

### 2-9. プロジェクトビュー（Project View）

IDE のプロジェクトツリー表示カスタマイズ。

| Extension Point | インターフェース | 何ができるか | ReScript での活用 |
|---|---|---|---|
| `com.intellij.treeStructureProvider` | `TreeStructureProvider` | ツリー構造のカスタマイズ | ✅ 実装済み (.resi ネスト) |
| `com.intellij.projectViewNodeDecorator` | `ProjectViewNodeDecorator` | ノードの装飾 | ✅ 実装済み (灰色化) |
| `com.intellij.projectViewNestingRulesProvider` | `ProjectViewNestingRulesProvider` | ファイルネストルール | ✅ 実装済み (.res.js) |
| `com.intellij.testSourcesFilter` | `TestSourcesFilter` | テストファイル認識 | ✅ 実装済み |

### 2-10. 実行・デバッグ（Run & Debug）

実行構成、テスト実行、デバッグ。

| Extension Point | インターフェース | 何ができるか | ReScript での活用 |
|---|---|---|---|
| `com.intellij.configurationType` | `ConfigurationType` | 実行構成タイプの定義 | ✅ 実装済み (3タイプ) |
| `com.intellij.runLineMarkerContributor` | `RunLineMarkerContributor` | ガター実行アイコン | ✅ 実装済み |
| `com.intellij.runAnythingProvider` | `RunAnythingProvider` | Ctrl+Ctrl コマンド実行 | ✅ 実装済み |
| `com.intellij.testFinder` | `TestFinder` | テストファイルの自動検出 | ✅ 実装済み |

### 2-11. ツールウィンドウ・UI（Tool Windows & UI）

IDE のパネル・ウィジェット・通知。

| Extension Point | インターフェース | 何ができるか | ReScript での活用 |
|---|---|---|---|
| `com.intellij.toolWindow` | `ToolWindowFactory` | カスタムツールウィンドウ | ✅ 実装済み (JS Preview) |
| `com.intellij.statusBarWidgetFactory` | `StatusBarWidgetFactory` | ステータスバーウィジェット | ✅ 実装済み |
| `com.intellij.editorNotificationProvider` | `EditorNotificationProvider` | エディタ上部の通知バー | ✅ 実装済み |
| `com.intellij.notificationGroup` | — | 通知グループの登録 | ✅ 実装済み |

### 2-12. 言語インジェクション（Language Injection）

埋め込み言語のサポート。

| Extension Point | インターフェース | 何ができるか | ReScript での活用 |
|---|---|---|---|
| `com.intellij.languageInjectionContributor` | `LanguageInjectionContributor` | 言語インジェクションの登録 | ✅ 実装済み (%raw JS) |
| `com.intellij.markdown.fencedCodeLanguageProvider` | `CodeFenceLanguageProvider` | Markdown コードフェンス | ✅ 実装済み |
| `com.intellij.multiLangCommenter` | `MultipleLangCommentProvider` | 多言語ファイルのコメント制御 | 未実装 |

### 2-13. インレイヒント（Inlay Hints）

エディタ上のインライン情報表示。

| Extension Point | インターフェース | 何ができるか | ReScript での活用 |
|---|---|---|---|
| LSP Inlay Hints | — | LSP の `textDocument/inlayHint` | ✅ 自動対応 |
| `com.intellij.codeInsight.inlayProvider` | `InlayHintsProvider` | カスタムインレイヒント | 未実装 (パイプチェーン型ヒント候補) |
| `com.intellij.codeInsight.daemonBoundCodeVisionProvider` | `CodeVisionProvider` | Code Vision (Code Lens 相当) | ✅ 実装済み |

### 2-14. ドキュメント（Documentation）

ドキュメントの表示・外部リンク。

| Extension Point | インターフェース | 何ができるか | ReScript での活用 |
|---|---|---|---|
| `com.intellij.lang.documentationProvider` | `DocumentationProvider` | Ctrl+Q ドキュメントポップアップ + Shift+F1 外部 URL | ✅ 実装済み (外部 URL) |
| `com.intellij.platform.backend.documentation.psiTargetProvider` | `PsiDocumentationTargetProvider` | 新 API ドキュメントターゲット | 未実装 |

### 2-15. スペルチェック（Spellchecking）

スペルチェック統合。

| Extension Point | インターフェース | 何ができるか | ReScript での活用 |
|---|---|---|---|
| `com.intellij.spellchecker.support` | `SpellcheckingStrategy` | スペルチェック対象の制御 | ✅ 実装済み |
| `com.intellij.spellchecker.bundledDictionaryProvider` | `BundledDictionaryProvider` | バンドル辞書の登録 | ✅ 実装済み |

## 3. LSP API と Extension Point の使い分け

IntelliJ Platform 2025.3+ では LSP API が大幅に強化されており、多くの機能が LSP サーバー経由で自動的に動作する。

### LSP が自動提供する機能（Extension Point 不要）

| 機能 | LSP メソッド | 追加実装 |
|------|-------------|---------|
| コード補完 | `textDocument/completion` | 不要 |
| 定義ジャンプ | `textDocument/definition` | 不要 |
| ホバードキュメント | `textDocument/hover` | 不要 |
| 参照検索 | `textDocument/references` | 不要 |
| 診断 | `textDocument/publishDiagnostics` | 不要 |
| インレイヒント | `textDocument/inlayHint` | 不要 |
| Signature Help | `textDocument/signatureHelp` | 不要 (2025.3+) |
| Code Actions | `textDocument/codeAction` | 不要 |
| リネーム | `textDocument/rename` | カスタムハンドラで拡張 |

### Extension Point で補強すべき機能

LSP では提供されない or 品質が不十分な機能:

| 機能 | 理由 | Extension Point |
|------|------|----------------|
| シンタックスハイライト | LSP セマンティックトークンだけでは初回表示が遅い | `syntaxHighlighterFactory` |
| コード折りたたみ | LSP にフォールディング機能はあるが PSI ベースが高品質 | `lang.foldingBuilder` |
| 選択拡大 | LSP に `textDocument/selectionRange` はあるが拡張性が限定的 | `extendWordSelectionHandler` |
| Intention Actions | LSP Code Action 以外のプラグイン固有アクション | `intentionAction` |
| Surround With | LSP では提供されない | `lang.surroundDescriptor` |
| Live Templates | LSP では提供されない | XML + `liveTemplateContext` |
| Postfix Completion | LSP では提供されない | `postfixTemplateProvider` |
| Generate アクション | LSP では提供されない | `codeInsight.generation` |
| 実行構成 | LSP では提供されない | `configurationType` |
| ツールウィンドウ | LSP では提供されない | `toolWindow` |

## 4. 実装パターン

### パターン 1: レクサーベース（パーサー変更不要）

**適用範囲**: トークン列のパターンマッチで判定可能な機能

```
レクサー出力のトークン列 → パターンマッチ → 結果
```

**例**: Completion Confidence, Join Lines, Enter Handler, Highlight Usages, Word Selection, Brace Matcher

**メリット**: 実装が簡単、パフォーマンスが良い、パーサー変更不要
**デメリット**: 複雑な構文構造は正確に判定できない

### パターン 2: PSI ベース（既存パーサーの PSI を活用）

**適用範囲**: トップレベル宣言の構造を利用する機能

```
PSI ツリー → 宣言ノードの走査 → 結果
```

**例**: Structure View, Code Folding, Context Info, Breadcrumbs, Statement Mover

**メリット**: 構造化されたアクセス、IDE のキャッシュを活用
**デメリット**: 式レベルの詳細構造にはアクセスできない

### パターン 3: LSP ベース（LSP リクエストの結果を活用）

**適用範囲**: 意味解析が必要な機能

```
LSP リクエスト → レスポンスの加工 → IDE API で表示
```

**例**: Expression Type, Code Vision, Goto Super (hover で型取得), パイプチェーン型ヒント

**メリット**: 正確な型情報・意味情報を利用可能
**デメリット**: LSP サーバーが動作している必要がある、非同期処理が必要

### パターン 4: テキストベース（正規表現・文字列パターン）

**適用範囲**: テキストの変換・操作

```
テキスト → 正規表現/パターンマッチ → 変換テキスト
```

**例**: Pipe ⇔ 関数呼び出し変換, Unwrap, 冗長ブロック削除, 識別子ケース修正

**メリット**: 実装が簡単、パーサー・LSP 不要
**デメリット**: エッジケースに弱い、ネスト構造の処理が困難

### パターン 5: 外部ツール連携

**適用範囲**: 外部 CLI の実行と結果の表示

```
外部ツール実行 → 出力パース → IDE API で表示
```

**例**: Formatting (rescript format), reanalyze, テスト実行 (jest/vitest)

**メリット**: ツールの既存機能を活用
**デメリット**: プロセス起動のオーバーヘッド、ツールのインストールが必要

## 5. 未活用の Extension Point と今後の展望

### 低コストで実装可能な未活用 EP

| Extension Point | 想定工数 | 効果 |
|---|---|---|
| `longLineInspectionPolicy` | 数時間 | `@module`, `%raw` での不要な警告を抑制 |
| `multiLangCommenter` | 半日 | `%raw()` 内の正しいコメント切り替え |
| `preserveIndentOnPaste` | 数時間 | ペースト時のインデント保持 |
| `basicWordSelectionFilter` | 数時間 | 既存の選択ハンドラとの連携改善 |

### 中コストで実装可能な未活用 EP

| Extension Point | 想定工数 | 効果 |
|---|---|---|
| `codeInsight.parameterInfo` | 1〜2日 | ネイティブ品質の Ctrl+P パラメータ表示 |
| `codeInsight.inlayProvider` | 2〜3日 | パイプチェーン中間型ヒント |
| `moveLeftRightHandler` | 1日 | カンマ区切り要素の入れ替え |
| `backspaceHandlerDelegate` | 半日 | JSX タグのペア削除 |
| `usageTypeProvider` | 1日 | Find Usages の用途別グルーピング |

### 高コストだが高価値な未活用 EP

| Extension Point | 想定工数 | 効果 |
|---|---|---|
| `lang.refactoringSupport` | 1〜2週間 | Extract Variable / Safe Delete |
| `stubIndex` | 1〜2週間 | 大規模プロジェクトでの高速検索 |
| `callHierarchyProvider` | 1週間 | 呼び出し階層の可視化 |
| `codeInsight.implementMethod` | 1週間 | module type からの実装生成 |
